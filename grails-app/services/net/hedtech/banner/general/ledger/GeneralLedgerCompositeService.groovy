package net.hedtech.banner.general.ledger

import net.hedtech.banner.general.ledger.v6.GeneralLedger

/**
 * Created by vijayt on 6/22/2016.
 */
class GeneralLedgerCompositeService {
    GeneralFeedService generalFeedService

    public list(Map params){
        List<GeneralFeed> generalFeedList = generalFeedService.list(params)
        List<GeneralLedger> generalLedgerList = []
        generalFeedList.each {
            GeneralLedger generalLedger = new GeneralLedger()
            generalLedger.generalFeed = it
            generalLedgerList << generalLedger
        }

        return generalLedgerList
    }

    public count(Map params){
        return generalFeedService.list(params).size()
    }
}
