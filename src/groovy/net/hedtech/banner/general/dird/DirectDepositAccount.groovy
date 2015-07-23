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
	BigDecimal amount
	
	@Column(name = "GXRDIRD_PERCENT")
	BigDecimal percent
	
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
	
	public static fetchByPidm(Map parms) {
		def dirdAccounts

		DirectDepositAccounts.withSession { session ->
			dirdAccounts = session.getNamedQuery(
					'LeaveTitle.fetchByPidm')
					.setString('pidm', parms?.pidm).list()[0]
		}
		return dirdAccounts
	}
}