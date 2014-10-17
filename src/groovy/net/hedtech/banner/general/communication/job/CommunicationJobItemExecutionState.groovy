/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.job

/**
 * CommunicationJobItemExecutionState describes the state of executing communication job item.
 */
enum CommunicationJobItemExecutionState implements Serializable {

	Ready(false),
    Complete(true),
    Stopped(true),
    Failed(true);

    boolean terminal;

    CommunicationJobItemExecutionState( boolean terminal ) {
        this.terminal = terminal;
    }

    Set<CommunicationJobItemExecutionState> set() {
    	return EnumSet.range( Ready, Failed );
    }

    String toString() {
        return this.name();
    }

    static CommunicationJobItemExecutionState valueOf(int ordinal) {
        for(CommunicationJobItemExecutionState ds: values()) {
            if (ds.ordinal() == ordinal) return ds;
        }
        throw new IllegalArgumentException( "ordinal out of range:" + ordinal );
    }

    boolean isTerminal() {
        return terminal;
    }

    boolean isRunning() {
        return !terminal;
    }

}
