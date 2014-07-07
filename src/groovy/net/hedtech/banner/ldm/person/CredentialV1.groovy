/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.ldm.person

/**
 * LDM decorator for person resource credential.
 */
class CredentialV1 {
    String credentialType
    String credentialId
    Date effectiveStartDate
    Date effectiveEndDate

    def CredentialV1( String credentialType, String credentialId,
                    Date effectiveStartDate, Date effectiveEndDate) {
        this.credentialType = credentialType
        this.credentialId = credentialId
        this.effectiveStartDate = effectiveStartDate
        this.effectiveEndDate = effectiveEndDate
    }
}
