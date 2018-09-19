/*******************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.scheduler

/**
 * Scheduler Job Context contains the callback parameters
 * passed in from the scheduler needed for the callback
 * to do its task.
 */
class SchedulerJobContext implements Serializable {
    Map parameters = [:]
    private SchedulerServiceMethodHandle jobHandle
    private SchedulerServiceMethodHandle errorHandle
    private String jobId
    private String groupId
    private String bannerUser
    private String mepCode
    private Date scheduledStartDate
    private Date endDate
    private String cronSchedule
    private String cronScheduleTimezone

    public SchedulerJobContext( String jobId ) {
        this.jobId = jobId
    }

    public SchedulerJobContext( String jobId, String groupId ) {
        this.jobId = jobId
        this.groupId = groupId
    }

    public String getGroupId() {
        return groupId
    }

    public String getJobId() {
        return jobId
    }

    public SchedulerJobContext setBannerUser( String bannerUser ) {
        this.bannerUser = bannerUser
        return this
    }

    public String getBannerUser() {
        return bannerUser
    }

    public SchedulerJobContext setMepCode( String mepCode ) {
        this.mepCode = mepCode
        return this
    }

    public String getMepCode() {
        return mepCode
    }

    public SchedulerJobContext setScheduledStartDate( Date scheduledStartDate ) {
        this.scheduledStartDate = scheduledStartDate
        return this
    }

    public Date getScheduledStartDate() {
        return scheduledStartDate
    }

    public SchedulerJobContext setEndDate(Date endDate) {
        this.endDate = endDate
        return this
    }

    public Date getEndDate() {
        return endDate
    }

    public SchedulerJobContext setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule
        return this
    }

    public String getCronSchedule() {
        return cronSchedule
    }

    public SchedulerJobContext setCronScheduleTimezone(String cronScheduleTimezone) {
        this.cronScheduleTimezone = cronScheduleTimezone
        return this
    }

    public String getCronScheduleTimezone() {
        return cronScheduleTimezone
    }

    public SchedulerJobContext setJobHandle( String service, String method ) {
        this.jobHandle = new SchedulerServiceMethodHandle( service, method )
        if (groupId == null) {
            groupId = service + "." + method
        }
        return this
    }

    public SchedulerServiceMethodHandle getJobHandle() {
        return jobHandle
    }

    public SchedulerJobContext setErrorHandle( String service, String method ) {
        this.errorHandle = new SchedulerServiceMethodHandle( service, method )
        return this
    }

    public SchedulerServiceMethodHandle getErrorHandle() {
        return errorHandle
    }

    public Object getParameter( String key ) {
        return parameters.get( key )
    }

    public SchedulerJobContext setParameter( String key, Object value ) {
        parameters.put( key, value )
        return this
    }

}
