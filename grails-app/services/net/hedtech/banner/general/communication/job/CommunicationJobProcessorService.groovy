package net.hedtech.banner.general.communication.job

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * Created by mbrzycki on 1/13/15.
 */
class CommunicationJobProcessorService {
    private final Log log = LogFactory.getLog(this.getClass());

    public void performCommunicationJob( Long jobId ) {
        log.debug( "performed communication job with job id = ${jobId}." )
    }

}
