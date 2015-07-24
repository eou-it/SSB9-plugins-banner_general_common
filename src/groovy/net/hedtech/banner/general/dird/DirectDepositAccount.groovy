/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
 package net.hedtech.banner.general.dird
 
 import java.util.Map;

import javax.persistence.*
 
 /**
  * Direct Deposit Account Table entity.
  */
 @Entity
 @Table(name = "GXRDIRD")
  @NamedQueries(value = [
  @NamedQuery(name = "DirectDepositAccount.fetchByPidm",
			  query = """ FROM DirectDepositAccount a WHERE a.pidm = :pidm """)
 ])
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
	
	@Column(name = "GXRDIRD_PIDM")
	Integer pidm
	
	@Column(name = "GXRDIRD_STATUS")
	String status
	
	@Column(name = "GXRDIRD_DOC_TYPE")
	String documentType
	
	@Column(name = "GXRDIRD_PRIORITY")
	Integer priority
	
	@Column(name = "GXRDIRD_AP_IND")
	Integer apIndicator
	
	@Column(name = "GXRDIRD_HR_IND")
	String hrIndicator
	
	@Column(name = "GXRDIRD_ACTIVITY_DATE")
	@Temporal(TemporalType.TIMESTAMP)
    Date lastModified
	
	@Column(name = "GXRDIRD_USER_ID")
	String lastModifiedBy
	
	@Column(name = "GXRDIRD_BANK_ACCT_NUM")
	String bankAccountNum
	
	@Column(name = "GXRDIRD_BANK_ROUT_NUM")
	String bankRoutingNum
	
	@Column(name = "GXRDIRD_AMOUNT")
	double amount
	
	@Column(name = "GXRDIRD_PERCENT")
	double percent
	
	@Column(name = "GXRDIRD_ACCT_TYPE")
	String accountType
	
	@Column(name = "GXRDIRD_ATYP_CODE")
	String addressTypeCode
	
	@Column(name = "GXRDIRD_ADDR_SEQNO")
	Integer addressSequenceNum
	
	@Column(name = "GXRDIRD_ACH_IAT_IND")
	String intlAchTransactionIndicator
	
	@Column(name = "GXRDIRD_SCOD_CODE_ISO")
	String isoCode
	
	@Column(name = "GXRDIRD_ACHT_CODE")
	String apAchTransactionTypeCode
	
	public String toString() {
		"""DirectDepositAccount [
			id= $id,
			version= $version,
			dataOrigin= $dataOrigin,
			pidm= $pidm,
			status= $status,
			documentType= $documentType,
			priority= $priority,
			apIndicator= $apIndicator,
			hrIndicator= $hrIndicator,
			lastModified= $lastModified,
			lastModifiedBy= $lastModifiedBy,
			bankAccountNum= $bankAccountNum,
			bankRoutingNum= $bankRoutingNum,
			amount= $amount,
			percent= $percent,
			accountType= $accountType,
			addressTypeCode= $addressTypeCode,
			addressSequenceNum= $addressSequenceNum,
			intlAchTransactionIndicator= $intlAchTransactionIndicator,
			isoCode= $isoCode,
			apAchTransactionTypeCode= $apAchTransactionTypeCode]"""
	}
	
	static constraints = {
		dataOrigin(nullable: true, maxSize: 30)
		pidm(nullable: false, min:-99999999, max:99999999)
		status(nullable: false, maxSize: 1)
		documentType(nullable: false, maxSize: 1)
		priority(nullable: false, maxSize: 99)
		apIndicator(nullable: false, maxSize: 1)
		hrIndicator(nullable: false, scale: 1)
		lastModified(nullable: false)
		lastModifiedBy(nullable: false, maxSize: 30)
		bankAccountNum(nullable: true, maxSize: 34)
		bankRoutingNum(nullable: true, maxSize: 11)
		amount(nullable: true, scale: 2)
		percent(nullable: true, scale: 2)
		accountType(nullable: true, maxSize: 1)
		addressTypeCode(nullable: true, maxSize: 2)
		addressSequenceNum(nullable: true, maxSize: 99)
		intlAchTransactionIndicator(nullable: false, maxSize: 8)
		isoCode(nullable: true, maxSize: 8)
		apAchTransactionTypeCode(nullable: true, maxSize: 8)
	}
	
	public boolean equals(object) {
		if (this.is(object)) return true
		if (!(object instanceof DirectDepositAccount)) return false
		if (!super.equals(object)) return false

		DirectDepositAccount that = (DirectDepositAccount) object

		if (pidm != that.pidm) return false
		if (bankAccountNum != that.bankAccountNum) return false
		if (bankRoutingNum != that.bankRoutingNum) return false
		
		return true
	}
	
	public int hashCode() {
		int result = super.hashCode()
		result = 31 * result + (id != null ? id.hashCode() : 0)
		result = 31 * result + (version != null ? version.hashCode() : 0)
		result = 31 * result + (pidm != null ? pidm.hashCode() : 0)
		result = 31 * result + (bankAccountNum != null ? bankAccountNum.hashCode() : 0)
		result = 31 * result + (bankRoutingNum != null ? bankRoutingNum.hashCode() : 0)
		return result
	}
	
	public static fetchByPidm(String pidm) {
		def dirdAccounts

		DirectDepositAccount.withSession { session ->
			dirdAccounts = session.getNamedQuery(
					'DirectDepositAccount.fetchByPidm')
					.setString('pidm', pidm).list()
		}
		return dirdAccounts
	}
}