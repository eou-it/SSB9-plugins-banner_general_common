/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.groupsend

/**
 * CommunicationGroupSendExecutionState describes the state of executing communication job item.
 */
enum CommunicationGroupSendExecutionState implements Serializable {

    New (false),
    Scheduled (false),
    Queued (false),

    Calculating (false),
    Processing (false),

    Complete (true),
    Stopped (true),
    Error (true);

    boolean terminal;

    CommunicationGroupSendExecutionState( boolean terminal ) {
        this.terminal = terminal;
    }

    Set<CommunicationGroupSendExecutionState> set() {
    	return EnumSet.range( Ready, Failed );
    }

    String toString() {
        return this.name();
    }

    static CommunicationGroupSendExecutionState valueOf(int ordinal) {
        for(CommunicationGroupSendExecutionState ds: values()) {
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

    boolean isPending() {
        return this == New || this == Scheduled || this == Queued
    }

    boolean isTerminalWithoutErrors() {
        return this == Stopped || this == Complete
    }
}
