/*******************************************************************************
 Copyright 2013-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.overall.loginworkflow

import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.GeneralCommonUtility
import net.hedtech.banner.general.overall.GeneralForStoringResponsesAndPinQuestion
import net.hedtech.banner.general.overall.PinQuestion

import java.sql.SQLException

@Slf4j
@Transactional
class SecurityQAService {
    //static transactional = true
    def sessionFactory

    def generalForStoringResponsesAndPinQuestionService

    /* Dummy value for answer salt as the domain expects a not null and non empty value.
    This will be updated in domain with correct salt by the backed up triggers and api's for the domain */
    private final static String ANSWER_SALT_DUMMY = "ML3MTB80"

    public def getNumberOfQuestionsAnswered(Integer pidm) {
        return GeneralForStoringResponsesAndPinQuestion.fetchCountOfAnswersForPidm(pidm)
    }

    public Map getUserDefinedPreference() {
        def connection
        Sql sql
        try {
            connection = sessionFactory.currentSession.connection()
            sql = new Sql(connection)
            GroovyRowResult row = sql.firstRow("""select GUBPPRF_ANSR_MIN_LENGTH, GUBPPRF_QSTN_MIN_LENGTH,GUBPPRF_DISABLE_FORGET_PIN_IND,
                    GUBPPRF_NO_OF_QSTNS,GUBPPRF_EDITQSTN_IND from GUBPPRF""")
            return row
        } catch (SQLException ae) {
            log.debug ae.stackTrace
            throw ae
        }
        catch (Exception ae) {
            log.debug ae.stackTrace
            throw ae
        } finally {
            //connection.close()
        }
    }

    /**   need to do following validations
     *   1. if question 1 and qs 2 is not null -> error  "Please enter only one question."
     *   2. if ans 1 and ans2 is not null -> error  "Please enter only one answer."   -> not applicable
     *   3. if ans1 is not null and ( q1 is null and q2 is null) -> error "Please enter Security Question and Answer."
     *   4. if ans1 is null and ( q1 is not null or q2 is not null) -> error "Please enter Security Question and Answer."
     *   5. if ans1 is null and ans2 is null -> error "Please enter Security Question and Answer." -> not applicable
     *   6. q's should not have "<" / ">"  -> error   "Question may not contain the < or > characters."
     *   7. ans should not have "<" / ">" -> error    "Answer may not contain the < or > characters."
     *   8. if ans1 is not NULL and length of ans1 < GUBPPRF_ANSR_MIN_LENGTH and GUBPPRF_ANSR_MIN_LENGTH > 0 -> error  "Answer has to be %01% characters or more."
     *   9. if ans2 is not NULL and length of ans1 < GUBPPRF_ANSR_MIN_LENGTH and GUBPPRF_ANSR_MIN_LENGTH > 0 ->
     *       error  "Answer has to be %01% characters or more."  -> not applicable
     *   10. if q2 is not NULL and length of ans1 < GUBPPRF_QSTN_MIN_LENGTH and GUBPPRF_QSTN_MIN_LENGTH > 0 -> error  "Question has to be %01% characters or more."
     *   */
    public void saveSecurityQAResponse(String pidm, List questionList, String pin) {

        boolean isValidPin = GeneralCommonUtility.validatePin(pin, pidm)
        if (!isValidPin) {
            throw new ApplicationException("", "securityQA.invaild.pin")
        }

        if (questionList.size() <= 0) {
            throw new ApplicationException("", "securityQA.error")
        }

        int cnt = 1
        questionList.each {

            String dropDownQuestion = it["question"]
            String editableQuestion = it["userDefinedQuestion"]
            String answer = it["answer"]
            String dropDownQuestionId = it["questionNo"]
            String questionNumber = cnt++

            validateValueConstraints(dropDownQuestion, editableQuestion, answer)

            if (isDropDownQuestionAnswered(dropDownQuestion, answer)) {
                createDropDownQuestionAndAnswer(pidm, dropDownQuestionId, questionNumber, answer)
            } else {
                createEditableQuestionAndAnswer(pidm, editableQuestion, questionNumber, answer)
            }
        }
    }

    public void saveOrUpdateSecurityQAResponse(String pidm, List questionList, String pin) {

        boolean isValidPin = GeneralCommonUtility.validatePin(pin, pidm)
        if (!isValidPin) {
            throw new ApplicationException("", "securityQA.invaild.pin")
        }

        if (questionList.size() <= 0) {
            throw new ApplicationException("", "securityQA.error")
        }

        int cnt = 1
        questionList.each {

            String dropDownQuestion = it["question"]
            String editableQuestion = it["userDefinedQuestion"]
            String answer = it["answer"]
            String dropDownQuestionId = it["questionNo"]
            String questionNumber = cnt++

            validateValueConstraints(dropDownQuestion, editableQuestion, answer)

            if (isDropDownQuestionAnswered(dropDownQuestion, answer)) {
                if(it.id) {
                    updateDropDownQuestionAndAnswer(pidm, dropDownQuestionId, answer, it.id, it.version)
                }
                else {
                    createDropDownQuestionAndAnswer(pidm, dropDownQuestionId, questionNumber, answer)
                }
            } else {
                if(it.id) {
                    updateEditableQuestionAndAnswer(pidm, editableQuestion, answer, it.id, it.version)
                }
                else {
                    createEditableQuestionAndAnswer(pidm, editableQuestion, questionNumber, answer)
                }
            }
        }
    }

    private void validateValueConstraints(String question1, String question2, String answer) {
        checkAndRaiseExceptionIfMoreThanOneQuestionEntered(question1, question2)
        checkAndRaiseExceptionIfQuestionAnswerNotEntered(question1, question2, answer)
        checkAndRaiseExceptionForInvalidCharacter(answer, question2)
        checkAndRaiseExceptionForInvalidLength(answer, question2)
    }

    private void checkAndRaiseExceptionIfMoreThanOneQuestionEntered(String question1, String question2) {
        if (isValueEntered(question1) && isValueEntered(question2)) {
            log.debug("Each question can be selected or defined, not both. Please choose a question from the drop down or define your own question.")
            throw new ApplicationException("", "securityQA.invalid.number.question")
        }
    }

    private void checkAndRaiseExceptionIfQuestionAnswerNotEntered(String question1, String question2, String answer) {
        if (isValueEntered(answer) && !isValueEntered(question1) && !isValueEntered(question2)) {
            log.debug("Please enter Security Question and Answer.")
            throw new ApplicationException("", "securityQA.error")
        } else if (!isValueEntered(answer) && (isValueEntered(question1) || isValueEntered(question2))) {
            log.debug("Please enter Security Question and Answer.")
            throw new ApplicationException("", "securityQA.error")
        }
    }

    private void checkAndRaiseExceptionForInvalidCharacter(String answer, String question) {
        if (isValueEntered(question) && isInvalidCharacterEntered(question)) {
            log.debug("Question may not contain the < or > characters.")
            throw new ApplicationException("", "securityQA.invalid.question")
        }

        if (isValueEntered(answer) && isInvalidCharacterEntered(answer)) {
            log.debug("Answer may not contain the < or > characters.")
            throw new ApplicationException("", "securityQA.invalid.answer")
        }
    }

    private boolean isInvalidCharacterEntered(String value) {
        List invalidCharList = ['<', '>']
        boolean invalidCharacterExists = false
        invalidCharList.each {
            if (value.contains(it)) {
                invalidCharacterExists = true
            }
        }
        return invalidCharacterExists
    }

    private void checkAndRaiseExceptionForInvalidLength(String answer, String question) {
        def gubprfAnsrMinLength = getUserDefinedPreference()?.GUBPPRF_ANSR_MIN_LENGTH
        if (isValueEntered(answer) && answer.length() < gubprfAnsrMinLength && gubprfAnsrMinLength > 0) {
            log.debug("Answer has to be " + gubprfAnsrMinLength + " characters or more.")
            throw new ApplicationException("", "securityQA.invalid.length.answer")
        }

        def gubprfQstnMinLength = getUserDefinedPreference()?.GUBPPRF_QSTN_MIN_LENGTH
        if (isValueEntered(question) && question.length() < gubprfQstnMinLength && gubprfQstnMinLength > 0) {
            log.debug("Question has to be " + gubprfQstnMinLength + " characters or more.")
            throw new ApplicationException("", "securityQA.invalid.length.question")
        }
    }

    private boolean isDropDownQuestionAnswered(String question1, String answer) {
        if (isValueEntered(question1) && isValueEntered(answer)) {
            return true
        }
        return false
    }

    private boolean isValueEntered(String value) {
        boolean isValueEntered = false;
        if (value != null && !value.equals("")) {
            isValueEntered = true;
        }
        return isValueEntered;
    }

    private void createDropDownQuestionAndAnswer(pidm, questionId, question_num, answer) {
        def pinQuestion = PinQuestion.fetchQuestionOnId(questionId)
        GeneralForStoringResponsesAndPinQuestion generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: Integer.valueOf(pidm),
                number: question_num,
                questionDescription: null,
                answerDescription: answer,
                answerSalt: ANSWER_SALT_DUMMY,
                pinQuestion: pinQuestion
        )
        generalForStoringResponsesAndPinQuestionService.create(generalForStoringResponsesAndPinQuestion)
    }

    private void createEditableQuestionAndAnswer(pidm, question, question_num, answer) {
        if (GeneralForStoringResponsesAndPinQuestion.fetchCountOfSameQuestionForPidm([pidm: Integer.valueOf(pidm), questionDescription: question]) > 0) {
            log.warn("Question has to be Unique")
            throw new ApplicationException("", "securityQA.unique.question")
        }
        GeneralForStoringResponsesAndPinQuestion generalForStoringResponsesAndPinQuestion = new GeneralForStoringResponsesAndPinQuestion(
                pidm: Integer.valueOf(pidm),
                number: question_num,
                questionDescription: question,
                answerDescription: answer,
                answerSalt: ANSWER_SALT_DUMMY,
                pinQuestion: null
        )
        generalForStoringResponsesAndPinQuestionService.create(generalForStoringResponsesAndPinQuestion)
    }

    private void updateDropDownQuestionAndAnswer(pidm, questionId, answer, id, version) {
        def pinQuestion = PinQuestion.fetchQuestionOnId(questionId)
        Map generalForStoringResponsesAndPinQuestion = [
               id: id,
               version: version,
               pidm: Integer.valueOf(pidm),
               questionDescription: null,
               answerDescription: answer,
               answerSalt: ANSWER_SALT_DUMMY,
               pinQuestion: pinQuestion
        ]
        generalForStoringResponsesAndPinQuestionService.update(generalForStoringResponsesAndPinQuestion)
    }

    private void updateEditableQuestionAndAnswer(pidm, question, answer, id, version) {
        if (generalForStoringResponsesAndPinQuestionService.fetchCountOfSameQuestionForPidmById(Integer.valueOf(pidm), question, id as int) > 0) {
            log.warn("Question has to be Unique")
            throw new ApplicationException("", "securityQA.unique.question")
        }
        Map generalForStoringResponsesAndPinQuestion = [
                id: id,
                version: version,
                pidm: Integer.valueOf(pidm),
                questionDescription: question,
                answerDescription: answer,
                answerSalt: ANSWER_SALT_DUMMY,
                pinQuestion: null
        ]
        generalForStoringResponsesAndPinQuestionService.update(generalForStoringResponsesAndPinQuestion)
    }
}
