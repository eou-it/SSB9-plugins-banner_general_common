package net.hedtech.banner.general.asynchronous.task

/**
 * Created by mbrzycki on 1/9/15.
 */
class AsynchronousTaskHandler implements Runnable {
    @Override
    public void run() {
        System.out.println( "hello, everyone" )
    }
}
