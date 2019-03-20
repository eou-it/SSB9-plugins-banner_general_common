/*********************************************************************************
 Copyright 2015-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm.v2

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.overall.IntegrationPartnerSystemRule
import net.hedtech.banner.general.system.ldm.v1.Metadata

/**
 * Decorator for Instructional Platforms HeDM
 */
//@EqualsAndHashCode(includeFields = true)
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof InstructionalPlatform)) return false

        InstructionalPlatform that = (InstructionalPlatform) o

        if (guid != that.guid) return false
        if (integrationPartnerSystemRule != that.integrationPartnerSystemRule) return false
        if (metadata != that.metadata) return false

        return true
    }

    int hashCode() {
        int result
        result = integrationPartnerSystemRule.hashCode()
        result = 31 * result + metadata.hashCode()
        result = 31 * result + guid.hashCode()
        return result
    }

    @Override
    public String toString() {
        return "InstructionalPlatform{" +
                "integrationPartnerSystemRule=" + integrationPartnerSystemRule +
                ", metadata=" + metadata +
                ", guid='" + guid + '\'' +
                '}';
    }
}
