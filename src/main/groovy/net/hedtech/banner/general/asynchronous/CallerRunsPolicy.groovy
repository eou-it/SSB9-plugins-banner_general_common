/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * A rejection policy that executes the job within the caller's thread if the
 * thread pool rejects the job.
 *
 * @author charlie hardt
 */
public class CallerRunsPolicy extends ThreadPoolExecutor.CallerRunsPolicy {

    // Just a wrapper so we can inject via constructor injection into the
    // thread pool.
}
