/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous.task;


/**
 * A holder of a failed job along with an error string.
 *
 * @author charlie hardt
 */
public class FailedAsynchronousTask implements Serializable {


    private AsynchronousTask job;

    private String errorString;



    /**
     * Default constructor for a failed job.
     */
    public FailedAsynchronousTask() {
        super();
    }


    /**
     * Constructor for a failed job.
     * @param job the job that failed
     * @param errorString the error string
     */
    public FailedAsynchronousTask( AsynchronousTask job, String errorString ) {
        super();
        this.job = job;
        this.errorString = errorString;
    }


    /**
     * @return the job
     */
    public AsynchronousTask getJob() {
        return job;
    }

    /**
     * @param job the job to set
     */
    public void setJob( AsynchronousTask job ) {
        this.job = job;
    }

    /**
     * @return the errorString
     */
    public String getErrorString() {
        return errorString;
    }

    /**
     * @param errorString the errorString to set
     */
    public void setErrorString( String errorString ) {
        this.errorString = errorString;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "FailedJob[job pk=" ).append( job.getId() );
        sb.append( ", error=" ).append( errorString ).append( "] " );
        return sb.toString();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errorString == null) ? 0 : errorString.hashCode());
        result = prime * result + ((job == null) ? 0 : job.hashCode());
        return result;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
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
        final FailedAsynchronousTask other = (FailedAsynchronousTask) obj;
        if (errorString == null) {
            if (other.errorString != null) {
                return false;
            }
        } else if (!errorString.equals(other.errorString)) {
            return false;
        }
        if (job == null) {
            if (other.job != null) {
                return false;
            }
        } else if (!job.equals(other.job)) {
            return false;
        }
        return true;
    }


}
