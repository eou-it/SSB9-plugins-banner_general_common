/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@Table(name = "GURFEED")
@EqualsAndHashCode
@ToString(includeFields = true, includeNames = true)
@NamedQueries([
        @NamedQuery(name = "GeneralFeed.transactionNumberExist",
        query = "SELECT distinct 'Y' from GeneralFeed where transactionNumber in (:transactionNumbers)")
])
class GeneralFeed implements Serializable{

    @Id
    @SequenceGenerator(name = "GURFEED_SEQ_GEN", allocationSize = 1, sequenceName = "GURFEED_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURFEED_SEQ_GEN")
    @Column(name = "GURFEED_SURROGATE_ID")
    Long id

    @Version
    @Column(name = "GURFEED_VERSION")
    Long version

    @Column(name = "GURFEED_DOC_REF_NUM")
    String referenceNumber

    @Column(name = "GURFEED_DOC_CODE")
    String transactionNumber

    @Column(name = "GURFEED_TRANS_DATE")
    @Temporal(TemporalType.DATE)
    Date ledgerDate

    @Column(name = "GURFEED_VENDOR_PIDM")
    Long referencePerson

    @Column(name = "GURFEED_ONE_TIME_VEND_CODE")
    String referenceOrganization

    @Column(name = "GURFEED_ENCD_NUM")
    String encumbranceNumber

    @Column(name = "GURFEED_ENCD_ITEM_NUM")
    Long encumbranceItemNumber

    @Column(name = "GURFEED_ENCD_SEQ_NUM")
    Long encumbranceSequenceNumber

    @Type(type = "yes_no")
    @Column(name = "GURFEED_ABAL_OVERRIDE")
    Boolean budgetOverride

    @Column(name = "GURFEED_BUDGET_PERIOD")
    String budgetPeriod

    @Column(name = "GURFEED_SEQ_NUM")
    Long sequenceNumber

    @Column(name = "GURFEED_ACCOUNT")
    String accountingString

    @Column(name = "GURFEED_COAS_CODE")
    String chartOfAccountsCode

    @Column(name = "GURFEED_ACCI_CODE")
    String accountIndexCode

    @Column(name = "GURFEED_FUND_CODE")
    String fundCode

    @Column(name = "GURFEED_ORGN_CODE")
    String organizationCode

    @Column(name = "GURFEED_ACCT_CODE")
    String accountCode

    @Column(name = "GURFEED_PROG_CODE")
    String programCode

    @Column(name = "GURFEED_ACTV_CODE")
    String activityCode

    @Column(name = "GURFEED_LOCN_CODE")
    String locationCode

    @Column(name = "GURFEED_PRJD_CODE")
    String projectCode

    @Column(name = "GURFEED_BANK_CODE")
    String bankCode

    @Column(name = "GURFEED_RUCL_CODE")
    String ruleClassCode

    @Column(name = "GURFEED_TRANS_DESC")
    String description

    @Column(name = "GURFEED_DR_CR_IND")
    String type

    @Column(name = "GURFEED_TRANS_AMT")
    BigDecimal amount

    @Column(name = "GURFEED_CURR_CODE")
    String currencyCode

    @Column(name = "GURFEED_SYSTEM_ID")
    String systemId

    @Column(name = "GURFEED_REC_TYPE")
    String recordTypeIndicator

    @Column(name = "GURFEED_SYSTEM_TIME_STAMP")
    String systemTimestamp

    @Column(name = "GURFEED_ACTIVITY_DATE")
    @Temporal(TemporalType.DATE)
    Date activityDate

    @Column(name = "GURFEED_USER_ID")
    String userId

    @Column(name = "GURFEED_DEP_NUM")
    String depositNumber

    @Column(name = "GURFEED_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        referenceNumber(nullable: true, maxSize: 8)
        transactionNumber(nullable: false, maxSize: 8)
        ledgerDate(nullable: false)
        referencePerson(nullable: true, max: 99999999L)
        referenceOrganization(nullable: true, maxSize: 9)
        encumbranceNumber(nullable: true, maxSize: 8)
        encumbranceItemNumber(nullable: true, max: 9999L)
        encumbranceSequenceNumber(nullable:true, max: 9999L)
        budgetOverride(nullable: true)
        budgetPeriod(nullable: true, maxSize: 2)
        sequenceNumber(nullable: true, max: 9999L)
        accountingString(nullable: true, maxSize: 60)
        chartOfAccountsCode(nullable: true, maxSize:1)
        accountIndexCode(nullable: true, maxSize: 6)
        fundCode(nullable: true, maxSize: 6)
        organizationCode(nullable: true, maxSize: 6)
        accountCode(nullable: true, maxSize: 6)
        programCode(nullable: true, maxSize: 6)
        activityCode(nullable: true, maxSize: 6)
        locationCode(nullable: true, maxSize: 6)
        projectCode(nullable: true, maxSize: 6)
        bankCode(nullable: true, maxSize: 2)
        ruleClassCode(nullable: true, maxSize: 4)
        description(nullable: true, maxSize: 35)
        type(nullable: true, maxSize: 1,
                validator: {val, obj ->
                    if ((val && val != "C" && val != "D")) {
                        return "invalid.type"
                    }
                }
            )
        amount(nullable:false, min: new BigDecimal(0), max: new BigDecimal(999999999999999.99))
        currencyCode(nullable: true, maxSize: 4)
        systemId(nullable: false, maxSize: 8)
        recordTypeIndicator(nullable: false, maxSize: 1,
                validator: {val, obj ->
                    if ((val && val != "1" && val != "2")) {
                        return "invalid.recordTypeIndicator"
                    }
                }
            )
        systemTimestamp(nullable: false, maxSize: 14)
        activityDate(nullable: false)
        userId(nullable: false, maxSize: 30)
        depositNumber(nullable: true, maxSize: 8)
        dataOrigin(nullable: true)
    }


    public static Boolean transactionNumberExist(List<String> transactionNumbers){
        Boolean exist = false
        if(transactionNumbers){
            GeneralFeed.withSession{ session ->
                if(session.getNamedQuery("GeneralFeed.transactionNumberExist").setParameterList('transactionNumbers', transactionNumbers).list().size()>0){
                    exist = true
                }
            }
        }
        return exist
    }
}
