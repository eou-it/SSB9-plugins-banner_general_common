/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

/**
 * CommunicationJobItemExecutionState describes the state of executing communication job item.
 */
enum CommunicationGroupSendItemExecutionState implements Serializable {

	Ready(false),
    Complete(true),
    Stopped(true),
    Failed(true);

    boolean terminal;

    CommunicationGroupSendItemExecutionState( boolean terminal ) {
        this.terminal = terminal;
    }

    Set<CommunicationGroupSendItemExecutionState> set() {
    	return EnumSet.range( Ready, Failed );
    }

    String toString() {
        return this.name();
    }

    static CommunicationGroupSendItemExecutionState valueOf(int ordinal) {
        for(CommunicationGroupSendItemExecutionState ds: values()) {
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
