/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.ledger

import net.hedtech.banner.service.ServiceBase

class GeneralFeedShadowService extends ServiceBase{
    static transactional = true;

    public List<GeneralFeedShadow> fetchByGuid(String guid){
        return GeneralFeedShadow.fetchByGuid(guid)
    }
}
