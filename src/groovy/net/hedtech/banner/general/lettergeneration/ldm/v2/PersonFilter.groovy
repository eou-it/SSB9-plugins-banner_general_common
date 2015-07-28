/** *****************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general.lettergeneration.ldm.v2

import net.hedtech.banner.general.system.ldm.v1.Metadata


/**
 * HEDM decorator for person-filters resource
 */
class PersonFilter {

    String abbreviation
    String guid
    Metadata metadata
    String title

    PersonFilter(String abbreviation, String guid, String title, Metadata metadata) {
        this.abbreviation = abbreviation
        this.guid = guid
        this.title = title
        this.metadata = metadata
    }


    public String toString() {
        """PersonFilter[
                    abbreviation=$abbreviation,
                    guid=$guid,
                    metadata=$metadata,
                    title=$title]"""
    }

}
