package net.hedtech.banner.general.overall



import org.junit.Before
import org.junit.Test
import org.junit.After

import grails.validation.ValidationException
import groovy.sql.Sql
import net.hedtech.banner.general.overall.DirectDepositAccountService
import net.hedtech.banner.testing.BaseIntegrationTestCase

/**
 *
 */
class DirectDepositAccountServiceIntegrationTestsSpec extends BaseIntegrationTestCase {

	@Before
	public void setUp() {
		formContext = ['GUAGMNU']
		super.setUp()
	}


	@After
	public void tearDown() {
		super.tearDown()
	}

    @Test
    void testCreateDirectDepositAccount() {
        def directDepositAccount = newDirectDepositAccount()
        directDepositAccount = directDepositAccount.create([domainModel: directDepositAccount])

        // Assert domain values
        assertNotNull directDepositAccount
        assertNotNull directDepositAccount.id
        assertEquals 36948575, directDepositAccount.bankAccountNum
        assertEquals 123478902, directDepositAccount.bankRoutingNum
        def id = directDepositAccount.id

        directDepositAccount = directDepositAccount.get(id)
        assertNotNull directDepositAccount
    }
	
	private def newDirectDepositAccount() {
		def domain = new DirectDepositAccount(
			pidm: 36732,
			status: "A",
			documentType: "D",
			priority: 3,
			apIndicator: "I",
			hrIndicator: "A",
//			lastModified: $lastModified,
//			lastModifiedBy: $lastModifiedBy,
			bankAccountNum: 36948575,
			bankRoutingNum: 123478902,
			amount: null,
			percent: 11.0,
			accountType: "C",
//			addressTypeCode: $addressTypeCode,
//			addressSequenceNum: $addressSequenceNum,
			intlAchTransactionIndicator: "N"
//			isoCode: $isoCode,
//			apAchTransactionTypeCode: $apAchTransactionTypeCode
		)

		return domain
	}
}
