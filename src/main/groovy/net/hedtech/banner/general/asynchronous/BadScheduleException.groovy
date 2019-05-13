/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous;

import net.hedtech.banner.exceptions.ApplicationException;

/**
 * An exception thrown when the underlying scheduler rejects a schedule for reasons
 * that may be correctable by the user. Note: Causes of an exception from the
 * underlying that are unlikely to be resolvable by a user should not result in
 * this exception but instead a SystemException.
 *
 * @author charlie hardt
 */
@SuppressWarnings("serial")
public class BadScheduleException extends ApplicationException {


    private static final String RESOURCE_CODE = "exception.BadScheduleException";
    private static final String CRON_ONLY_RESOURCE_CODE = "exception.BadScheduleException.cron";


    /**
     * Constructor for a bad schedule exception.
     */
    public BadScheduleException( AsynchronousActionExecutionSchedule executionSchedule, Throwable cause ) {
        super( RESOURCE_CODE );
        /**
         new Serializable[] { executionSchedule.beginDateAsString(),
                                                            executionSchedule.endDateAsString(),
                                                            executionSchedule.getTimeZone(),
                                                            executionSchedule.getScheduledCronExpression(),
                                                            ExecutionSchedule.dateAsString( new Date(), executionSchedule.getTimeZone() ) }
         */
    }


    /**
     * Constructor for a bad schedule exception.
     */
    public BadScheduleException( String cronString, Throwable cause ) {
        super( CRON_ONLY_RESOURCE_CODE );
        /*

, new Serializable[] { cronString, ExecutionSchedule.dateAsString( new Date(), TimeZone.getDefault() ) }

         */
    }

}
