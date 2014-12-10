/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous
/**
 * Utility to generate cron expressions as used by the JobSubmission service
 * from java Date instances.
 * @author Shane Riddell
 */
public class CronExpressionUtil {

    /**
     * Converts the specified date to a cron expression for the job submission service.
     * @param date the date to create a cron expression for
     * @return String the cron expression corresponding to the date
     */
    public static String toCronString( Date date ) {
        StringBuffer cronExpression = new StringBuffer();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime( date );

        cronExpression.append( calendar.get( Calendar.SECOND ) ).append( " " );
        cronExpression.append( calendar.get( Calendar.MINUTE ) ).append( " " );
        cronExpression.append( calendar.get( Calendar.HOUR_OF_DAY ) ).append( " " );
        cronExpression.append( calendar.get( Calendar.DAY_OF_MONTH ) ).append( " " );
        cronExpression.append( calendar.get( Calendar.MONTH ) + 1 ).append( " " );
        cronExpression.append( "?" ).append( " " );
        cronExpression.append( calendar.get( Calendar.YEAR ) );

        return cronExpression.toString();
    }

}
