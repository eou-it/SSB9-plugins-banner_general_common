/*******************************************************************************

 � 2007 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD SCT AND IS NOT TO BE COPIED,
 REPRODUCED, LENT, OR DISPOSED OF, NOR USED FOR ANY PURPOSE OTHER THAN THAT
 WHICH IT IS SPECIFICALLY PROVIDED WITHOUT THE WRITTEN PERMISSION OF THE
 SAID COMPANY
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend.automation

import net.hedtech.banner.general.asynchronous.task.AsynchronousTaskProcessingEngine
import org.springframework.beans.factory.annotation.Required

/**
 * Teardown performer for shutting down the notification manager while
 * table teardown is in progress.
 *
 * @author Michael Brzycki
 */
public class CommunicationGroupSendItemProcessingEngineStartupPerformer implements TearDownPerformer {

    private AsynchronousTaskProcessingEngine jobProcessingEngine;


    /**
     * Performs teardown activities.
     */
    public void execute() throws Exception {
        jobProcessingEngine.startRunning();
    }


    /**
     * Sets the job processing engine to shutdown.
     */
    @Required
    public void setJobProcessingEngine( AsynchronousTaskProcessingEngine jobProcessingEngine ) {
        this.jobProcessingEngine = jobProcessingEngine;
    }

}