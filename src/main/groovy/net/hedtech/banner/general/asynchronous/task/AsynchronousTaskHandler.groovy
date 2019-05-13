package net.hedtech.banner.general.asynchronous.task

/**
 * Created by mbrzycki on 1/9/15.
 */
abstract
class AsynchronousTaskHandler implements Runnable {

    private AsynchronousTask job

    public AsynchronousTaskHandler( AsynchronousTask job ) {
        this.job = job
    }

    AsynchronousTask getJob() {
        return job
    }

}
