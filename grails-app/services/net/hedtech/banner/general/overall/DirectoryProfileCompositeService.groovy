/*********************************************************************************
Copyright 2017 Ellucian Company L.P. and its affiliates.
**********************************************************************************/
 package net.hedtech.banner.general.overall

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import net.hedtech.banner.general.person.PersonAddress
import net.hedtech.banner.general.person.PersonAddressUtility
import net.hedtech.banner.general.person.PersonTelephone
import net.hedtech.banner.general.system.DirectoryOption
import net.hedtech.banner.general.system.InstitutionalDescription
import net.hedtech.banner.person.PersonTelephoneDecorator
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.web.context.request.RequestContextHolder

import java.sql.Timestamp

@Transactional
class DirectoryProfileCompositeService {

    private static final String DIRECTORY_PROFILE_SESSION_CACHE_NAME = 'directoryProfileSessionCache'
    private static final String COLLEGE_DESCRIPTION = 'collegeDescription'
    private static final String JOB_DESCRIPTION = 'jobDescription'


    def sessionFactory
    def preferredNameService
    def personEmailService
    def directoryProfilePreferenceService


    def fetchDirectoryProfileItemsForUser(pidm, addrMaskingRule = null) {
        def userDirProfileList = []
        def fullDirProfileList = fetchAllDirectoryProfileItems()
        def userPreferences = fetchAllDirectoryProfilePreferencesForUser(pidm)

        fullDirProfileList.each { item ->
            if (item.displayProfileIndicator && isMatchingRoleForDirectoryType(pidm, item.directoryType)) {
                def userSelection = userPreferences.find {it.code == item.code}

                userDirProfileList.addAll(getItemProperties(pidm, item, userSelection, addrMaskingRule))
            }
        }

        userDirProfileList
    }

    def static List fetchAllDirectoryProfileItems() {
        def directoryProfileItems

        DirectoryProfileItem.withSession { session ->
            directoryProfileItems = session.getNamedQuery('DirectoryProfileItem.fetchAllOrderBySeqNo').list()
        }

        return directoryProfileItems
    }

    def static List fetchAllDirectoryProfilePreferencesForUser(Integer pidm) {
        def directoryProfilePrefs

        DirectoryProfilePreference.withSession { session ->
            directoryProfilePrefs = session.getNamedQuery('DirectoryProfilePreference.fetchByPidm').setInteger('pidm', pidm).list()
        }

        return directoryProfilePrefs
    }

    def isMatchingRoleForDirectoryType(pidm, directoryType) {
        def STUDENT_DIRECTORY_TYPES =  ['S','A','B','F','T']
        def EMPLOYEE_DIRECTORY_TYPES = ['E','A','C','F','P']
        def ALUMNI_DIRECTORY_TYPES =   ['D','A','B','C']
        def FRIEND_DIRECTORY_TYPES =   ['R','T','P','A','S','E']
        def BSAC_DIRECTORY_TYPES =     ['K','A','T','B','F']

        def isMatchingRole = false
        def userRole = UserRole.fetchByPidm(pidm)

        if (userRole) {
            if (userRole.studentIndicator && STUDENT_DIRECTORY_TYPES.contains(directoryType) ||
                userRole.employeeIndicator && EMPLOYEE_DIRECTORY_TYPES.contains(directoryType) ||
                userRole.alumniIndicator && ALUMNI_DIRECTORY_TYPES.contains(directoryType) ||
                userRole.friendIndicator && FRIEND_DIRECTORY_TYPES.contains(directoryType)||
                userRole.studentAidForCanadaIndicator && BSAC_DIRECTORY_TYPES.contains(directoryType)) {

                isMatchingRole = true
            }
        }

        return isMatchingRole
    }

    def getItemProperties(pidm, DirectoryProfileItem profileItem, userSelection = null, addrMaskingRule = null) {
        def allItemProperties = []
        def description = fetchDirectoryOptionItem(profileItem.code)?.description?.find{true}
        def checked = userSelection ? userSelection.displayInDirectoryIndicator : profileItem.nonProfileDefaultIndicator
        def changeable = profileItem.updateProfileIndicator

        if (profileItem) {
            def currentListings = getCurrentListingForDirectoryItem(pidm, profileItem, addrMaskingRule)

            currentListings.each {
                def itemProperties = [
                        code: profileItem.code,
                        description: description,
                        checked: checked,
                        changeable: changeable,
                        currentListing: it
                ]

                allItemProperties << itemProperties
            }
        }

        allItemProperties
    }

    def static List fetchDirectoryOptionItem(String code) {

        def directoryOptionsValidationItem = DirectoryOption.withSession { session ->
            session.getNamedQuery('DirectoryOption.fetchByCode').setString('code', code).list()
        }

        return directoryOptionsValidationItem
    }

    def getCurrentListingForDirectoryItem(pidm, item, addrMaskingRule = null) {
        def resultStringList = []

        if (item.code == 'NAME') {
            resultStringList = [[preferredNameService.getPreferredName([pidm: pidm])]]
        } else if (item.code == 'EMAIL') {
            def emails = personEmailService.getDisplayableEmails(pidm)

            if (emails) {
                def formattedEmails = formatEmails(emails)
                resultStringList = [formattedEmails]
            }
        } else if (item.code == 'CLASS_YR') {
            if (InstitutionalDescription.fetchByKey()?.alumniInstalled) {
                def result = fetchClassYear(pidm)

                if (result) {
                    resultStringList << result
                }
            }
        } else if (item.code == 'COLLEGE') {
            def result = fetchCollege(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.code == 'GRD_YEAR') {
            def result = fetchGraduationYear(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.code == 'DEPT') {
            def result = fetchJobDepartment(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.code == 'TITLE') {
            def result = fetchJobTitle(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.code == 'MAIDEN') {
            def result = fetchMaidenName(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.code == 'PR_COLL') {
            def result = fetchPreferredCollege(pidm)

            if (result) {
                resultStringList << result
            }
        } else if (item.itemType == 'A') { // It's an address
            def directoryAddresses = DirectoryAddress.fetchByDirectoryOptionOrderByPriority(item.code)

            directoryAddresses.find { dirAddr ->
                def addr = PersonAddress.fetchActiveAddressByPidmAndAddressType(pidm, dirAddr.addressType)

                if (addr) {
                    resultStringList << formatAddress(addr, addrMaskingRule)
                }
            }
        } else if (item.itemType == 'T') { // It's a phone
            def directoryAddresses = DirectoryAddress.fetchByDirectoryOptionOrderByPriority(item.code)

            directoryAddresses.find { dirAddr ->
                def phonesByType = PersonTelephone.fetchActiveTelephoneWithUnlistedByPidmAndTelephoneType(pidm, dirAddr.telephoneType)

                if (phonesByType) {
                    resultStringList.addAll(formatAndSortPhones(phonesByType))
                }
            }
        }

        if (!resultStringList) {
            def notReportedStr = new ValidationTagLib().message(
                    [code: 'net.hedtech.banner.general.overall.DirectoryProfileCompositeService.notReported', default: 'Not Reported']
            )
            resultStringList << [notReportedStr]
        }

        resultStringList
    }

    private List formatEmails(emails) {
        def formattedEmails = []

        emails.each { email ->
            def lines = [
                    "${email.preferredIndicator ? 'PREFERRED ' : ''}${email.emailType?.description} -",
                    email.emailAddress
            ]

            formattedEmails.addAll(lines)
        }

        formattedEmails
    }

    private List fetchClassYear(pidm) {
        def result
        def classYearList = []
        def sqlStatement = '''SELECT APBCONS_PREF_CLAS FROM APBCONS
                                WHERE APBCONS_PIDM = ? AND APBCONS_PREF_CLAS IS NOT NULL AND APBCONS_PREF_CLAS <> \'0000\''''

        if (tableExists('APBCONS')) {
            Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

            try {
                result = sql.firstRow(sqlStatement, [pidm])

                if (result?.APBCONS_PREF_CLAS) {
                    classYearList << result.APBCONS_PREF_CLAS
                }
            } catch (e) {
                throw e
            } finally {
//                sql?.close()
            }
        }

        classYearList
    }

    private List fetchCollege(pidm) {
        def result = []
        def fullDescription = fetchCollegeDescription(pidm)

        if (fullDescription?.college) {
            result << fullDescription.college
        }

        result
    }

    private List fetchGraduationYear(pidm) {
        def result = []
        def fullDescription = fetchCollegeDescription(pidm)

        if (fullDescription?.gradYear) {
            result << fullDescription.gradYear
        }

        result
    }

    private Map fetchCollegeDescription(pidm) {
        // The query below retrieves BOTH the college description AND expected graduation date.  Doing it this way
        // saves an additional query.  After the query is executed, both values are stored in a map which in turn is
        // stored in a cache in the session.  This map is what's returned by this method.
        def sessionCache = getDirectoryProfileSessionCache();
        def collegeDescription = sessionCache[COLLEGE_DESCRIPTION]

        if (collegeDescription == null && InstitutionalDescription.fetchByKey()?.studentInstalled) {
            collegeDescription = [:]

            if (tableExists('SGBSTDN')) {
                def sqlStatement = '''select stvcoll_desc, sgbstdn_exp_grad_date from stvcoll, sgbstdn x
                                        where stvcoll_code = sgbstdn_coll_code_1
                                            and sgbstdn_pidm = ?
                                            and sgbstdn_term_code_eff =
                                                (
                                                    select max(sgbstdn_term_code_eff) from sgbstdn
                                                    where sgbstdn_pidm = x.sgbstdn_pidm
                                                )'''

                Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

                try {
                    def result = sql.firstRow(sqlStatement, [pidm])

                    if (result) {
                        collegeDescription.college = result.STVCOLL_DESC
                        collegeDescription.gradYear = getYearFromTimestamp(result.SGBSTDN_EXP_GRAD_DATE)
                    }
                } catch (e) {
                    throw e
                } finally {
//                    sql?.close()
                }
            }

            sessionCache[COLLEGE_DESCRIPTION] = collegeDescription
        }

        collegeDescription
    }

    private getYearFromTimestamp(Timestamp timestamp) {
        def year = ''

        if (timestamp) {
            Calendar cal = Calendar.getInstance()
            cal.setTime(timestamp)
            year = cal.get(Calendar.YEAR) as String
        }

        year
    }

    private List fetchJobDepartment(pidm) {
        def result = []
        def fullDescription = fetchJobDescription(pidm)

        if (fullDescription?.dept) {
            result = fullDescription.dept
        }

        result
    }

    private List fetchJobTitle(pidm) {
        def result = []
        def fullDescription = fetchJobDescription(pidm)

        if (fullDescription?.title) {
            result = fullDescription.title
        }

        result
    }

    private Map fetchJobDescription(pidm) {
        // The query below retrieves BOTH the job title AND department.  Doing it this way
        // saves an additional query.  After the query is executed, both values are stored in a map which in turn is
        // stored in a cache in the session.  This map is what's returned by this method.
        def sessionCache = getDirectoryProfileSessionCache();
        def jobDescription = sessionCache[JOB_DESCRIPTION]

        if (jobDescription == null && InstitutionalDescription.fetchByKey()?.posnctlInstalled) {
            jobDescription = [:]

            if (tableExists('NBRJOBS') && tableExists('NBRBJOB')) {
                def map = [pidm: pidm]
                def sqlStatement = """SELECT nbrjobs_desc, nbrjobs_coas_code_ts, nbrjobs_orgn_code_ts
                                            FROM NBRBJOB Y, NBRJOBS X
                                            WHERE NBRJOBS_PIDM = TO_CHAR ($map.pidm)
                                              AND NBRBJOB_PIDM = TO_CHAR ($map.pidm)
                                              AND NBRJOBS_POSN = NBRBJOB_POSN
                                              AND NBRJOBS_SUFF = NBRBJOB_SUFF
                                              AND (SYSDATE >= NBRBJOB_BEGIN_DATE OR
                                                   NBRBJOB_BEGIN_DATE IS NULL)
                                              AND (SYSDATE <= NBRBJOB_END_DATE OR
                                                   NBRBJOB_END_DATE IS NULL)
                                              AND NBRJOBS_STATUS != 'T'
                                              AND NBRJOBS_EFFECTIVE_DATE =
                                                  (SELECT MAX(NBRJOBS_EFFECTIVE_DATE)
                                                   FROM NBRJOBS
                                                   WHERE NBRJOBS_PIDM = X.NBRJOBS_PIDM
                                                     AND NBRJOBS_POSN = X.NBRJOBS_POSN
                                                     AND NBRJOBS_SUFF = X.NBRJOBS_SUFF
                                                     AND NBRJOBS_EFFECTIVE_DATE <= SYSDATE)
                                            ORDER  BY NBRJOBS_ORGN_CODE_TS"""

                Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

                try {
                    def rows = sql.rows(sqlStatement)

                    if (rows) {
                        def prevJobTitleDept = null
                        def prevJobDept = null
                        def printJobsDesc = []
                        def printJobsDept = []

                        rows.each {
                            // Get TITLE
                            def jobDept = fetchOrganizationDescription(it.NBRJOBS_ORGN_CODE_TS, it.NBRJOBS_COAS_CODE_TS)
                            def jobTitleDept = "${it.NBRJOBS_DESC}${jobDept}"

                            if (jobTitleDept != prevJobTitleDept) {
                                printJobsDesc << "${it.NBRJOBS_DESC} (${jobDept})"
                            }

                            prevJobTitleDept = jobTitleDept


                            // Get DEPT
                            if (jobDept != prevJobDept) {
                                printJobsDept << jobDept
                            }

                            prevJobDept = jobDept
                        }

                        jobDescription.title = printJobsDesc
                        jobDescription.dept = printJobsDept
                    }
                } catch (e) {
                    throw e
                } finally {
//                    sql?.close()
                }
            }

            sessionCache[JOB_DESCRIPTION] = jobDescription
        }

        jobDescription
    }

    private fetchOrganizationDescription(orgnCode, coasCode) {
        def orgnDesc = ''

        if (InstitutionalDescription.fetchByKey()?.financeInstalled) {
            if (tableExists('FTVORGN')) {
                def effDate = new Date().format('yyyyMMdd')
                def effTime = '235959'
                String effDateTime = "$effDate$effTime"

                def map = [coas: coasCode, orgn: orgnCode, effDateTime: effDateTime]
                def sqlStatement =
                        """SELECT FTVORGN_TITLE FROM FTVORGN
                           WHERE FTVORGN_COAS_CODE = $map.coas
                            AND  FTVORGN_ORGN_CODE = $map.orgn
                            AND  FTVORGN_EFF_DATE <= TO_DATE($map.effDateTime,'YYYYMMDDHH24MISS')
                            AND (FTVORGN_NCHG_DATE IS NULL OR FTVORGN_NCHG_DATE > TO_DATE($map.effDateTime,'YYYYMMDDHH24MISS'))
                            AND (FTVORGN_TERM_DATE IS NULL OR FTVORGN_TERM_DATE > TO_DATE($map.effDateTime,'YYYYMMDDHH24MISS'))"""

                Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

                try {
                    def result = sql.firstRow(sqlStatement)

                    if (result?.FTVORGN_TITLE) {
                        orgnDesc = result.FTVORGN_TITLE
                    }
                } catch (e) {
                    throw e
                } finally {
//                    sql?.close()
                }
            }
        } else if (InstitutionalDescription.fetchByKey()?.hrInstalled && tableExists('PTVORGN')) {
            def sqlStatement = '''SELECT PTVORGN_DESC FROM PTVORGN WHERE PTVORGN_CODE = ?'''

            Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

            try {
                def result = sql.firstRow(sqlStatement, [orgnCode])

                if (result?.PTVORGN_DESC) {
                    orgnDesc = result.PTVORGN_DESC
                }
            } catch (e) {
                throw e
            } finally {
//                sql?.close()
            }
        }

        return orgnDesc
    }

    private fetchMaidenName(pidm) {
        def maidenName = []

        if (InstitutionalDescription.fetchByKey()?.alumniInstalled && tableExists('APBCONS')) {
            def sqlStatement = '''SELECT APBCONS_MAIDEN_LAST_NAME FROM APBCONS WHERE APBCONS_PIDM = ?'''

            Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

            try {
                def result = sql.rows(sqlStatement, [pidm])

                if (result) {
                    result.each {
                        if (it.APBCONS_MAIDEN_LAST_NAME) {
                            maidenName << it.APBCONS_MAIDEN_LAST_NAME
                        }
                    }
                }
            } catch (e) {
                throw e
            } finally {
//                sql?.close()
            }
        }

        maidenName
    }

    private fetchPreferredCollege(pidm) {
        def preferredCollege = []

        if (InstitutionalDescription.fetchByKey()?.alumniInstalled && tableExists('APBCONS')) {
            def sqlStatement = '''SELECT STVCOLL_DESC FROM STVCOLL, APBCONS
                                    WHERE APBCONS_PIDM = ? AND APBCONS_COLL_CODE_PREF = STVCOLL_CODE'''

            Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

            try {
                def result = sql.rows(sqlStatement, [pidm])

                if (result) {
                    result.each {
                        if (it.STVCOLL_DESC) {
                            preferredCollege << it.STVCOLL_DESC
                        }
                    }
                }
            } catch (e) {
                throw e
            } finally {
//                sql?.close()
            }
        }

        preferredCollege
    }

    private List formatAddress(addr, maskingRule = null) {
        def addrLines = []

        if (addr) {
            def formattedAddr = PersonAddressUtility.formatDefaultAddress(
                    [houseNumber       : addr.houseNumber,
                     streetLine1       : addr.streetLine1,
                     streetLine2       : addr.streetLine2,
                     streetLine3       : addr.streetLine3,
                     streetLine4       : addr.streetLine4,
                     city              : addr.city,
                     state             : addr.state?.description,
                     zip               : addr.zip,
                     county            : addr.county?.description,
                     country           : addr.nation?.nation,
                     displayHouseNumber: maskingRule?.displayHouseNumber,
                     displayStreetLine4: maskingRule?.displayStreetLine4]
            )

            // Parse map values into array
            formattedAddr.each { k, v ->
                if (v) {
                    addrLines << v
                }
            }
        }

        addrLines
    }

    /**
     * Format phone number to strings and sort:  first by primary status, then second by sequence number
     * @param phones
     * @return Sorted list of formatted phone strings
     */
    private List formatAndSortPhones(phones) {
        def formattedPhones = []
        def nonPrimary = []

        phones.each { phone ->
            if (phone.primaryIndicator == 'Y') {
                formattedPhones << formatPhone(phone)
            } else {
                nonPrimary << phone
            }
        }

        nonPrimary = nonPrimary.sort {it.sequenceNumber}

        nonPrimary.each {
            formattedPhones << formatPhone(it)
        }

        [formattedPhones]
    }

    private formatPhone(phone) {
        def phoneStr
        def decorator

        if (phone.unlistIndicator == 'Y') {
            phoneStr = new ValidationTagLib().message(
                    [code: 'net.hedtech.banner.general.overall.DirectoryProfileCompositeService.phoneUnlisted', default: 'Unlisted']
            )
        } else {
            decorator = new PersonTelephoneDecorator(phone)
            phoneStr = decorator.displayPhone
        }

        phoneStr
    }

    private tableExists(tableName) {
        def result
        def sqlStatement = '''SELECT \'Y\' FROM dual
                                    WHERE EXISTS (SELECT \'X\' FROM all_tables WHERE table_name = upper(?))'''

        Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

        try {
            result = sql.firstRow(sqlStatement, [tableName])
        } catch (e) {
            throw e
        } finally {
//            sql?.close()
        }

        result != null
    }

    private getDirectoryProfileSessionCache() {
        def session = RequestContextHolder.currentRequestAttributes().request.session

        def dirProfileSessionCache = session.getAttribute(DIRECTORY_PROFILE_SESSION_CACHE_NAME)

        if (!dirProfileSessionCache) {
            dirProfileSessionCache = [
                    (COLLEGE_DESCRIPTION): null,
                    (JOB_DESCRIPTION): null
            ]

            session.setAttribute(DIRECTORY_PROFILE_SESSION_CACHE_NAME, dirProfileSessionCache)
        }

        dirProfileSessionCache
    }

    def createOrUpdate(pidm, items) {
        def allDirProfileItems = fetchAllDirectoryProfileItems()
        def userPreferences = fetchAllDirectoryProfilePreferencesForUser(pidm)
        def prefsForSave = []

        items.each { item ->
            def model
            def matchingPref = userPreferences.find {it.code == item.code}

            if (matchingPref) {
                // Matches an existing user preference

                if (matchingPref.displayInDirectoryIndicator != item.checked) {
                    // Update the existing preference with the new one
                    model = [
                            pidm: pidm,
                            id: matchingPref.id,
                            version: matchingPref.version,
                            displayInDirectoryIndicator: item.checked
                    ]
                }
            } else {
                // Find the matching system (non user modified) Directory Profile item.
                // ASSERT: there must be a match.
                matchingPref = allDirProfileItems.find {it.code == item.code}

                if (matchingPref.updateProfileIndicator && (matchingPref.nonProfileDefaultIndicator != item.checked)) {
                    // Create a new user preference
                    model = [
                            pidm: pidm,
                            code: item.code,
                            displayInDirectoryIndicator: item.checked
                    ]
                }
            }

            if (model) {
                prefsForSave << model
            }
        }

        if (prefsForSave) {
            directoryProfilePreferenceService.createOrUpdate(prefsForSave)
        }
    }

}
