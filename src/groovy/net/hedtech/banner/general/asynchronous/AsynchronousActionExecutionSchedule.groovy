/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous

import java.text.DateFormat;
import java.text.SimpleDateFormat

/**
 * An execution schedule for a job.
 *
 * @author charlie hardt
 */
@SuppressWarnings("serial")
public class AsynchronousActionExecutionSchedule implements Serializable {


    /**
     * The date the job is scheduled to begin.  This may be null, but if it is used
     * to start a job and there is an endDate present, it may be set to the current time.
     */
    private Date beginDate;

    /**
     * The date the job is scheduled to end.  This may be null, and if so will
     * not have any affect on the related object (if that object holds a endDate
     * attribute).
     */
    private Date endDate;

    /**
     * The cron expression that should be used to schedule the job.  This cannot
     * be null.
     * @see #scheduledExecutionInterval
     */
    private String scheduledCronExpression;


    /**
     * The time zone for the schedule.
     */
    private TimeZone timeZone;

//  ---------------------------- Constructor(s) --------------------------------


    /**
     * Default constructor for an execution schedule.
     */
    public AsynchronousActionExecutionSchedule() { }


    /**
     * Constructor for an execution schedule.
     * @param scheduledCronExpression a CRON expression to be used
     * @param scheduledExecutionInterval an enumeration indicating the repeating interval
     */
    public AsynchronousActionExecutionSchedule( String scheduledCronExpression) {
        super();
        this.scheduledCronExpression = scheduledCronExpression;
    }


    /**
     * Constructor for an execution schedule.
     * @param beginDate the begin date for execution
     * @param endDate the end date for execution
     * @param scheduledCronExpression a CRON expression to be used
     * @param scheduledExecutionInterval an enumeration indicating the repeating interval
     */
    public AsynchronousActionExecutionSchedule( Date beginDate, Date endDate, String scheduledCronExpression) {
        this( scheduledCronExpression);
        this.beginDate = beginDate;
        this.endDate = endDate;
    }


    /**
     * Constructor for an execution schedule.
     * @param beginDate the begin date for execution
     * @param endDate the end date for execution
     * @param scheduledCronExpression a CRON expression to be used
     * @param scheduledExecutionInterval an enumeration indicating the repeating interval
     */
    public AsynchronousActionExecutionSchedule( Date beginDate, Date endDate, String scheduledCronExpression, TimeZone timeZone ) {
        this( beginDate, endDate, scheduledCronExpression);
        this.timeZone = timeZone;
    }


//  -------------------------- Getters and Setters -----------------------------


    /**
     * Returns the begin date for this lifecycle track.
     * @return the beginDate
     */
    public Date getBeginDate() {
        return beginDate;
    }


    /**
     * Sets the begin date for this lifecycle track.
     * @param beginDate the beginDate to set
     */
    public void setBeginDate( Date beginDate ) {
        this.beginDate = beginDate;
    }


    /**
     * Returns the end date for this lifecycle track.
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }


    /**
     * Sets the end date for this lifecycle track.
     * @param endDate the endDate to set
     */
    public void setEndDate( Date endDate ) {
        this.endDate = endDate;
    }


    /**
     * Returns the cron expression used to schedule this lifecycle track.
     * @return the scheduledCronExpression
     */
    public String getScheduledCronExpression() {
        return scheduledCronExpression;
    }


    /**
     * Sets the cron expression used for this lifecycle track.
     * @param scheduledCronExpression the scheduledCronExpression to set
     */
    public void setScheduledCronExpression( String scheduledCronExpression ) {
        this.scheduledCronExpression = scheduledCronExpression;
    }

    /**
     * Returns the time zone for this schedule if one was set.  This may return
     * null (in which case the client may want to use the default TimeZone).
     * @return the timeZone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }


    /**
     * Sets a time zone for this schedule that will be used to interpret the
     * cron expression.
     * @param timeZone the timeZone to set
     */
    public void setTimeZone( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }


    /**
     * Returns the begin date as a formatted string in the time zone of this execution schedule.
     * @return String a string representation of the begin date
     */
    public String beginDateAsString() {
        return dateAsString( getBeginDate(), getTimeZone() );
    }


    /**
     * Returns the end date as a formatted string in the time zone of this execution schedule.
     * @return String a string representation of the begin date
     */
    public String endDateAsString() {
        return dateAsString( getEndDate(), getTimeZone() );
    }


    /**
     * Returns the end date as a formatted string in the time zone of this execution schedule.
     * @return String a string representation of the begin date
     */
    public static String dateAsString( Date date, TimeZone timezone ) {
        if (date == null) {
            return "";
        }
        DateFormat format = new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss zzzz");
        if (timezone != null) {
            format.setTimeZone( timezone );
        }
        return format.format( date );
    }



//  ----------------------------- Object Methods -------------------------------


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((beginDate == null) ? 0 : beginDate.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((scheduledCronExpression == null) ? 0 : scheduledCronExpression.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AsynchronousActionExecutionSchedule other = (AsynchronousActionExecutionSchedule) obj;
        if (beginDate == null) {
            if (other.beginDate != null) {
                return false;
            }
        } else if (!beginDate.equals(other.beginDate)) {
            return false;
        }
        if (endDate == null) {
            if (other.endDate != null) {
                return false;
            }
        } else if (!endDate.equals(other.endDate)) {
            return false;
        }
        if (scheduledCronExpression == null) {
            if (other.scheduledCronExpression != null) {
                return false;
            }
        } else if (!scheduledCronExpression.equals(other.scheduledCronExpression)) {
            return false;
        }
        return true;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "ExecutionSchedule(" );
        sb.append( "beginDate=" ).append( beginDate );
        sb.append( "endDate=" ).append( endDate );
        sb.append( "ScheduledCronExpression=" ).append( scheduledCronExpression );
        return sb.append( ")" ).toString();
    }

}
