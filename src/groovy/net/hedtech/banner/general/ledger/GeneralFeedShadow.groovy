/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

@Entity
@Table(name = "GURTRNH")
@EqualsAndHashCode
@ToString(includeFields = true, includeNames = true)
@NamedQueries([
        @NamedQuery(name = "GeneralFeedShadow.fetchByGuid",
        query = """FROM GeneralFeedShadow a where a.guid = :guid""")
])
class GeneralFeedShadow implements Serializable {

    @Id
    @SequenceGenerator(name = "GURTRNH_SEQ_GEN", allocationSize = 1, sequenceName = "GURTRNH_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GURTRNH_SEQ_GEN")
    @Column(name = "GURTRNH_SURROGATE_ID")
    Long id

    @Version
    @Column(name = "GURTRNH_VERSION")
    Long version

    @Column(name = "GURTRNH_GUID")
    String guid

    @Column(name = "GURTRNH_DOC_REF_NUM")
    String referenceNumber

    @Column(name = "GURTRNH_DOC_CODE")
    String transactionNumber

    @Column(name = "GURTRNH_TYPE")
    String transactionType

    @Column(name = "GURTRNH_TRANS_DATE")
    @Temporal(TemporalType.DATE)
    Date ledgerDate

    @Column(name = "GURTRNH_VENDOR_PIDM")
    Long referencePerson

    @Column(name = "GURTRNH_ONE_TIME_VEND_CODE")
    String referenceOrganization

    @Column(name = "GURTRNH_TRANS_TYPE_REF_DATE")
    @Temporal(TemporalType.DATE)
    Date transactionTypeReferenceDate

    @Column(name = "GURTRNH_ENCD_NUM")
    String encumbranceNumber

    @Column(name = "GURTRNH_ENCD_ITEM_NUM")
    Long encumbranceItemNumber

    @Column(name = "GURTRNH_ENCD_SEQ_NUM")
    Long encumbranceSequenceNumber

    @Type(type = "yes_no")
    @Column(name = "GURTRNH_ABAL_OVERRIDE")
    Boolean budgetOverride

    @Column(name = "GURTRNH_BUDGET_PERIOD")
    String budgetPeriod

    @Column(name = "GURTRNH_SEQ_NUM")
    Long sequenceNumber

    @Column(name = "GURTRNH_ACCOUNT")
    String accountingString

    @Column(name = "GURTRNH_TRANS_DESC")
    String description

    @Column(name = "GURTRNH_DR_CR_IND")
    String type

    @Column(name = "GURTRNH_TRANS_AMT")
    BigDecimal amount

    @Column(name = "GURTRNH_CURR_CODE")
    String currencyCode

    @Column(name = "GURTRNH_SYSTEM_ID")
    String systemId

    @Column(name = "GURTRNH_SYSTEM_TIME_STAMP")
    String systemTimestamp

    @Column(name = "GURTRNH_ACTIVITY_DATE")
    @Temporal(TemporalType.DATE)
    Date activityDate

    @Column(name = "GURTRNH_USER_ID")
    String userId

    @Column(name = "GURTRNH_DEP_NUM")
    String depositNumber

    @Column(name = "GURTRNH_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        guid(nullable: false, maxSize: 36)
        referenceNumber(nullable: true, maxSize: 8)
        transactionNumber(nullable: false, maxSize: 8)
        transactionType(nullable: false, maxSize: 50)
        ledgerDate(nullable: false)
        referencePerson(nullable: true, max: 99999999L)
        referenceOrganization(nullable: true, maxSize: 9)
        transactionTypeReferenceDate(nullable: true)
        encumbranceNumber(nullable: true, maxSize: 8)
        encumbranceItemNumber(nullable: true, max: 9999L)
        encumbranceSequenceNumber(nullable:true, max: 9999L)
        budgetOverride(nullable: true)
        budgetPeriod(nullable: true, maxSize: 2)
        sequenceNumber(nullable: false, max: 9999L)
        accountingString(nullable: false, maxSize: 60)
        description(nullable: false, maxSize: 35)
        type(nullable: false, maxSize: 1,
                validator: { val, obj ->
                    if ((val && val != "C" && val != "D")) {
                        return "invalid.type"
                    }
                }
        )
        amount(nullable: false, min: new BigDecimal(0), max: new BigDecimal(999999999999999.99))
        currencyCode(nullable: false, maxSize: 4)
        systemId(nullable: false, maxSize: 8)
        systemTimestamp(nullable: false, maxSize: 14)
        activityDate(nullable: false)
        userId(nullable: false, maxSize: 30)
        depositNumber(nullable: true, maxSize: 8)
        dataOrigin(nullable: true)
    }

    public static List<GeneralFeedShadow> fetchByGuid(String guid){
        List<GeneralFeedShadow> generalFeedShadowList = []
        GeneralFeedShadow.withSession{session ->
            generalFeedShadowList = session.getNamedQuery("GeneralFeedShadow.fetchByGuid").setString('guid', guid).list()
        }
        return generalFeedShadowList
    }
}
