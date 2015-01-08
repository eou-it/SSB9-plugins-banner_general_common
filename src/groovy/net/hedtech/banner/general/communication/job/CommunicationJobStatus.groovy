package net.hedtech.banner.general.communication.job

/**
 * An enumeration of status codes for communication jobs, used for tracking whether
 * the job is pending processing or has been dispatched to a processing engine.
 */
public enum CommunicationJobStatus {
    PENDING,   // The job has not yet been processed.
    DISPATCHED,  // The job has been given to a thread for execution
    FAILED,    // The job has been processed, but failed
    COMPLETED; // The job has been processed successfully


    /**
     * Returns a set of all CommunicationJobStatus enum values.
     * @return Set<CommunicationJobStatus> the set of CommunicationJobStatus
     */
    public Set<CommunicationJobStatus> set() {
        return EnumSet.range( CommunicationJobStatus.PENDING, CommunicationJobStatus.COMPLETED );
    }
}
