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
@EqualsAndHashCode(includeFields = true)
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

    @Override
    public String toString() {
        return "InstructionalPlatform{" +
                "integrationPartnerSystemRule=" + integrationPartnerSystemRule +
                ", metadata=" + metadata +
                ", guid='" + guid + '\'' +
                '}';
    }
}
