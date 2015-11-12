/*******************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

import net.hedtech.banner.general.crossproduct.BankRoutingInfo
 
/**
 * Direct Deposit Account Table entity.
 */
@Entity
@Table(name = "GXRDIRD")
@ToString(includeNames = true, ignoreNulls = true)
@NamedQueries(value = [
    @NamedQuery(name = "DirectDepositAccount.fetchByPidm",
        query = """ FROM DirectDepositAccount a WHERE a.pidm = :pidm """),
    @NamedQuery(name = "DirectDepositAccount.fetchActiveApAccountsByPidm",
        query = """ FROM DirectDepositAccount a
                   WHERE a.pidm = :pidm
                     AND a.apIndicator = 'A'
                     AND a.status != 'I'"""),
    @NamedQuery(name = "DirectDepositAccount.fetchActiveHrAccountsByPidm",
        query = """ FROM DirectDepositAccount a
                   WHERE a.pidm = :pidm
                     AND a.hrIndicator = 'A'
                     AND a.status != 'I'"""),
    @NamedQuery(name = "DirectDepositAccount.fetchByPidmAndAccountInfo",
        query = """ FROM DirectDepositAccount a
                   WHERE a.pidm = :pidm
                     AND a.bankRoutingInfo.bankRoutingNum = :bankRoutingNum
                     AND a.bankAccountNum = :bankAccountNum
                     AND a.accountType = :accountType""")
])
@EqualsAndHashCode(includeFields = true)
class DirectDepositAccount implements Serializable {
    
    /**
     * SURROGATE ID: Immutable unique key
     */
    @Id
    @Column(name = "GXRDIRD_SURROGATE_ID")
    @SequenceGenerator(name = "GXRDIRD_SEQ_GEN", allocationSize = 1, sequenceName = "GXRDIRD_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GXRDIRD_SEQ_GEN")
    Long id
    
    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GXRDIRD_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GXRDIRD_DATA_ORIGIN")
    String dataOrigin
    
    /**
     * PIDM: Personal Identification Number of the recipient as set in SPRIDEN.
     */
    @Column(name = "GXRDIRD_PIDM")
    Integer pidm
    
    /**
     * STATUS: Direct Deposit Status of recipient.  (A)ctive, (I)nactive, (P)renote.
     */
    @Column(name = "GXRDIRD_STATUS")
    String status
    
    /**
     * DOC_TYPE: The document type - (C)heck or (D)irect Deposit.
     */
    @Column(name = "GXRDIRD_DOC_TYPE")
    String documentType
    
    /**
     * PRIORITY: The priority in which direct deposits are to be distributed.
     */
    @Column(name = "GXRDIRD_PRIORITY")
    Integer priority
    
    /**
     * ACCOUNTS PAYABLE INDICATOR: This indicator is to designate whether the bank information is (A)ctive for
     *  Finance direct deposits or (I)nactive for Finance direct deposits.
     */
    @Column(name = "GXRDIRD_AP_IND")
    String apIndicator
    
    /**
     * PAYROLL INDICATOR: This indicator is to designate whether the bank information is (A)ctive for Payroll direct
     * deposits or (I)nactive for Payroll direct deposits.
     */
    @Column(name = "GXRDIRD_HR_IND")
    String hrIndicator
    
    /**
     * ACTIVITY DATE: The date when the information for this record on the table was entered or last updated.
     */
    @Column(name = "GXRDIRD_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified
    
    /**
     * USER ID: The user id of the person who updates this record.
     */
    @Column(name = "GXRDIRD_USER_ID")
    String lastModifiedBy
    
    /**
     * BANK ACCOUNT NO: The account number of the recipients account.
     */
    @Column(name = "GXRDIRD_BANK_ACCT_NUM")
    String bankAccountNum

    /**
     * BANK ROUTING INFO JOIN: The bank routing number and bank name of the recipients bank via join to GXVDIRD.
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="GXRDIRD_BANK_ROUT_NUM",referencedColumnName="GXVDIRD_CODE_BANK_ROUT_NUM")
    BankRoutingInfo bankRoutingInfo;

    /**
     * AMOUNT: The amount of the direct deposit.
     */
    @Column(name = "GXRDIRD_AMOUNT")
    Double amount
    
    /**
     * PERCENT: The percent of net to be direct deposited.
     */
    @Column(name = "GXRDIRD_PERCENT")
    Double percent
    
    /**
     * ACCOUNT TYPE: The type of account for direct deposit (checking or savings).
     */
    @Column(name = "GXRDIRD_ACCT_TYPE")
    String accountType
    
    /**
     * ADDRESS TYPE CODE: The user defined code that describes the type of address, i.e., Purchase Order, Remit To,
     * Mailing, etc.  Must exist on SATURN.STVATYP.
     */
    @Column(name = "GXRDIRD_ATYP_CODE")
    String addressTypeCode
    
    /**
     * ADDRESS SEQUENCE NO: The address sequence number of the recipient.
     */
    @Column(name = "GXRDIRD_ADDR_SEQNO")
    Integer addressSequenceNum
    
    /**
     * INTERNATIONAL ACH TRANSACTION INDICATOR:  Indicator to identify whether the Direct Deposit payment is
     * classified as International ACH Transaction (IAT). Valid values are (Y)es, (N)o.  Default value is (N)o.
     */
    @Column(name = "GXRDIRD_ACH_IAT_IND")
    String intlAchTransactionIndicator
    
    /**
     * ISO CODE:  The two characters International Standards Organization (ISO) Standard Code associated with the
     * International ACH Transaction (IAT).
     */
    @Column(name = "GXRDIRD_SCOD_CODE_ISO")
    String isoCode
    
    /**
     * ACCOUNT PAYABLE ACH TRANSACTION TYPE:  Transaction type for use with Account Payable ACH direct deposit.
     */
    @Column(name = "GXRDIRD_ACHT_CODE")
    String apAchTransactionTypeCode
    
    /**
     * IAT ADDRESS TYPE:  Address Type override for foreign receiver of the IAT payment.
     */
    @Column(name = "GXRDIRD_ATYP_CODE_IAT")
    String iatAddressTypeCode
    
    /**
     * IAT ADDRESS SEQUENCE:  Address sequence for foreign IAT receiver with Accounts Payable.
     */
    @Column(name = "GXRDIRD_ADDR_SEQNO_IAT")
    Integer iatAddessSequenceNum

    static constraints = {
        dataOrigin(nullable: true, maxSize: 30)
        pidm(nullable: false, min:-99999999, max:99999999)
        status(nullable: false, maxSize: 1)
        documentType(nullable: false, maxSize: 1)
        priority(nullable: false, maxSize: 99)
        apIndicator(nullable: false, maxSize: 1)
        hrIndicator(nullable: false, scale: 1)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        bankAccountNum(nullable: true, maxSize: 34)
        amount(nullable: true, scale: 2)
        percent(nullable: true, scale: 2)
        accountType(nullable: true, maxSize: 1)
        addressTypeCode(nullable: true, maxSize: 2)
        addressSequenceNum(nullable: true, maxSize: 99)
        intlAchTransactionIndicator(nullable: false, maxSize: 8)
        isoCode(nullable: true, maxSize: 8)
        apAchTransactionTypeCode(nullable: true, maxSize: 8)
        iatAddressTypeCode(nullable: true, maxSize: 2)
        iatAddessSequenceNum(nullable: true, maxSize: 99)
    }
    
    public static fetchByPidm(Integer pidm) {
        def dirdAccounts

        DirectDepositAccount.withSession { session ->
            dirdAccounts = session.getNamedQuery(
                    'DirectDepositAccount.fetchByPidm')
                    .setInteger('pidm', pidm).list()
        }
        return dirdAccounts
    }
    
    public static fetchActiveApAccountsByPidm(Integer pidm) {
        def dirdAccounts

        DirectDepositAccount.withSession { session ->
            dirdAccounts = session.getNamedQuery(
                    'DirectDepositAccount.fetchActiveApAccountsByPidm')
                    .setInteger('pidm', pidm).list()
        }
        return dirdAccounts
    }
    
    public static fetchActiveHrAccountsByPidm(Integer pidm) {
        def dirdAccounts

        DirectDepositAccount.withSession { session ->
            dirdAccounts = session.getNamedQuery(
                    'DirectDepositAccount.fetchActiveHrAccountsByPidm')
                    .setInteger('pidm', pidm).list()
        }
        return dirdAccounts
    }

    public static fetchByPidmAndAccountInfo(Integer pidm, String bankRoutingNum, String bankAccountNum, String accountType) {
        def dirdAccounts
        
        DirectDepositAccount.withSession { session ->
            dirdAccounts = session.getNamedQuery(
                    'DirectDepositAccount.fetchByPidmAndAccountInfo')
                    .setInteger('pidm', pidm)
                    .setString('bankRoutingNum', bankRoutingNum)
                    .setString('bankAccountNum', bankAccountNum)
                    .setString('accountType', accountType)
                    .list()
        }
        return dirdAccounts
    }
}