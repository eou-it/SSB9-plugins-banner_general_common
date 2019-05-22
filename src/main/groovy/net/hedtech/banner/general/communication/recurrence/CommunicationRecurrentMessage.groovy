/*******************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.DateUtility
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.groupsend.CommunicationParameterValue
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType
import net.hedtech.banner.general.system.LetterProcessLetter
import net.hedtech.banner.service.DatabaseModifiesState
import org.apache.commons.lang.NotImplementedException
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

/**
 * Communication Recurrent Message
 *
 */
@Entity
@Table(name = "GCBCREC")
@EqualsAndHashCode
@ToString
@DatabaseModifiesState
class CommunicationRecurrentMessage implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBCREC_SURROGATE_ID")
    @SequenceGenerator(name = "gcbcrec_SEQ_GEN", allocationSize = 1, sequenceName = "gcbcrec_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gcbcrec_SEQ_GEN")
    Long id

    @Column(name = "GCBCREC_JOB_NAME")
    String name

    @Column(name = "GCBCREC_JOB_DESCRIPTION")
    String description
    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBCREC_VERSION")
    Long version

    /** The oracle user name of the person that submitted the group send. **/
    @Column(name = "GCBCREC_CREATOR_ID")
    String createdBy

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBCREC_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBCREC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBCREC_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCBCREC_VPDI_CODE")
    String mepCode

    @Column(name = "GCBCREC_ORGANIZATION_ID")
    Long organizationId

    @Column(name = "GCBCREC_POPL_ID")
    Long populationId;

    @Column(name = "GCBCREC_TEMPLATE_ID")
    Long templateId;

    @Column(name = "GCBCREC_EVENT_ID")
    Long eventId;

    @Column(name = "GCBCREC_START_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date startDate;

    @Column(name = "GCBCREC_CREATIONDATETIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    Date creationDateTime;

    @Column(name = "GCBCREC_CURRENT_STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    CommunicationGroupSendExecutionState currentExecutionState = CommunicationGroupSendExecutionState.New;

    @Column(name = "GCBCREC_STOPPED_DATE", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date stoppedDate;

    @Type(type = "yes_no")
    @Column(name = "GCBCREC_RECALC_ON_SEND")
    Boolean recalculateOnSend

    /**
     * Error Code: The error code for the error scenario that failed
     */
    @Column(name = "GCBCREC_ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    @Lob
    @Column(name = "GCBCREC_ERROR_TEXT")
    String errorText

    /**
     * Job ID : job id of a quartz scheduled task for this group send
     */
    @Column(name = "GCBCREC_JOB_ID")
    String jobId

    /**
     * Group ID : group id of the quartz job and/or trigger for a group send scheduled task
     */
    @Column(name = "GCBCREC_GROUP_ID")
    String groupId

    /**
     * Parameter Values : the values entered by the user for the parameters in a chosen template for the given group send
     */
    @Lob
    @Column(name = "GCBCREC_PARAMETER_VALUES")
    String parameterValues

    @Column(name = "GCBCREC_CRON_STRING")
    String cronExpression

    @Column(name = "GCBCREC_CRON_TIMEZONE")
    String cronTimezone

    @Column(name = "GCBCREC_END_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date endDate

    @Column(name = "GCBCREC_NUM_OCCURRENCES")
    Long noOfOccurrences;

    @Column(name = "GCBCREC_TOTAL_COUNT")
    Long totalCount;

    @Column(name = "GCBCREC_SUCCESS_COUNT")
    Long successCount;

    @Column(name = "GCBCREC_FAILURE_COUNT")
    Long failureCount;

    @Column(name = "GCBCREC_LETR_ID")
    Long communicationCodeId

    @Transient
    private Map parameterNameValueMap


    static constraints = {
        mepCode(nullable: true)
        name(nullable: false)
        populationId(nullable: false)
        organizationId(nullable: false)
        templateId(nullable: false)
        eventId(nullable:true)
        createdBy(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        startDate(nullable: true)
        endDate(nullable: true)
        stoppedDate(nullable: true)
        creationDateTime(nullable: false)
        currentExecutionState(nullable: false)
        recalculateOnSend(nullable:false)
        errorCode(nullable:true)
        errorText(nullable:true)
        jobId(nullable:true)
        groupId(nullable:true)
        parameterValues(nullable:true)
        cronExpression(nullable:false)
        cronTimezone(nullable:false)
        noOfOccurrences(nullable:true)
        totalCount(nullable:false)
        successCount(nullable:false)
        failureCount(nullable:false)
        communicationCodeId(nullable: true, maxSize: 15)
    }

    public Map getParameterNameValueMap() {
        if (parameterNameValueMap == null) {
            if (parameterValues == null || parameterValues.trim().length() == 0 ) {
                parameterNameValueMap = [:]
            } else {
                JsonSlurper jsonSlurper = new JsonSlurper()
                try {
                    Map rawNameValueMap = jsonSlurper.parseText( parameterValues )
                    if (rawNameValueMap != null) {
                        rawNameValueMap.keySet().each { String name ->
                            Map valueType = rawNameValueMap.get( name )
                            CommunicationParameterType type = CommunicationParameterType.valueOf( (String) valueType.type )
                            if (type == CommunicationParameterType.DATE) {
                                Date d = DateUtility.parseDateString( valueType.value, 'yyyy-MM-dd' )
                                rawNameValueMap.put( name, new CommunicationParameterValue( [value: d, type: type ] ) )
                            } else if (type == CommunicationParameterType.NUMBER) {
                                Number number = null
                                try {
                                    number = Long.parseLong( valueType.value )
                                } catch (NumberFormatException e) {
                                    number = Double.parseDouble( valueType.value )
                                }
                                if (number != null) {
                                    rawNameValueMap.put( name, new CommunicationParameterValue( [ value: number, type: type ] ) )
                                }
                            } else if (type == CommunicationParameterType.TEXT) {
                                rawNameValueMap.put( name, new CommunicationParameterValue( [ value: valueType.value, type: type ] ) )
                            } else {
                                throw new NotImplementedException( "Unhandled parameter type" )
                            }
                        }
                    }
                    parameterNameValueMap = rawNameValueMap
                } catch (groovy.json.JsonException e) {
                    throw CommunicationExceptionFactory.createApplicationException( CommunicationRecurrentMessage.class, "badSyntax" )
                }
            }
        }

        return parameterNameValueMap
    }

    public void setParameterNameValueMap( Map nameParameterValueMap ) {
        assert nameParameterValueMap != null
        Map rawNameValueMap = [:]
        if (nameParameterValueMap != null) {
            nameParameterValueMap.keySet().each { String name ->
                CommunicationParameterValue parameterValue = nameParameterValueMap.get( name )
                String valueAsString
                if (parameterValue.type == CommunicationParameterType.DATE) {
                    valueAsString = DateUtility.getDateString( parameterValue.value, 'yyyy-MM-dd' )
                } else if (parameterValue.type == CommunicationParameterType.NUMBER) {
                    valueAsString = parameterValue.value.toString()
                } else if (parameterValue.type == CommunicationParameterType.TEXT) {
                    valueAsString = parameterValue.value
                } else {
                    throw new NotImplementedException( "Not handled parameter type" )
                }
                rawNameValueMap.put( name, [ value: valueAsString, type: parameterValue.type.toString() ] )
            }
        }
        parameterValues = JsonOutput.toJson( rawNameValueMap )
        parameterNameValueMap = null
    }

}