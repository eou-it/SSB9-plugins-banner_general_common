package net.hedtech.banner.general.overall.ldm.v2

import net.hedtech.banner.general.overall.IntegrationPartnerSystemRule
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * Decorator for Instructional Platforms HeDM
 */
class InstructionalPlatform {

    @Delegate
    private final IntegrationPartnerSystemRule integrationPartnerSystemRule
    Metadata metadata
    String guid


    InstructionalPlatform( IntegrationPartnerSystemRule integrationPartnerSystemRule, Metadata metadata, String guid ) {
        this.integrationPartnerSystemRule = integrationPartnerSystemRule
        this.metadata = metadata
        this.guid = guid
    }


    boolean equals( o ) {
        if (this.is( o )) return true
        if (getClass() != o.class) return false
        IntegrationPartnerSystemRule that = (IntegrationPartnerSystemRule) o
        if (integrationPartnerSystemRule != that.integrationPartnerSystemRule) return false
        if (metadata != that.metadata) return false
        if (guid != that.guid) return false
        return true
    }


    int hashCode() {
        int result
        result = (integrationPartnerSystemRule != null ? integrationPartnerSystemRule.hashCode() : 0)
        result = 31 * result + (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0)
        return result
    }


    public String toString() {
        """InstructionalPlatform[
                    integrationPartnerSystemRule=$integrationPartnerSystemRule,
                    guid=$guid,
                    metadata=$metadata]"""
    }
}