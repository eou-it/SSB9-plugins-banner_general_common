package net.hedtech.banner.general.ledger

import net.hedtech.banner.service.ServiceBase

/**
 * Created by vijayt on 6/22/2016.
 */
class GeneralFeedService extends ServiceBase {
    static transactional = true;

    public Boolean transactionNumberExist(List<String> transactionNumbers) {
        return GeneralFeed.transactionNumberExist(transactionNumbers)
    }
}
