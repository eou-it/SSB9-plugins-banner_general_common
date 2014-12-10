/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.asynchronous
/**
 * An enumeration for executable job states.
 *
 * @author charlie hardt
 */
public enum AsynchronousActionState {

    READY,
    SCHEDULED,
    STARTED,
    COMPLETED,
    CANCELLED,
    FAILED;

public Set<AsynchronousActionState> set() {
        return EnumSet.range( AsynchronousActionState.READY,
                              AsynchronousActionState.FAILED );
}

}
