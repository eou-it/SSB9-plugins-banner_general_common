/*******************************************************************************

 � 2012 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATIION AND IS NOT
 TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF, NOR USED FOR ANY PURPOSE OTHER
 THAN THAT WHICH IT IS SPECIFICALLY PROVIDED WITHOUT THE WRITTEN PERMISSION OF
 THE SAID COMPANY
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.asynchronous.AsynchronousActionContainerContext;

/**
 * An execution context for passing the group send key to an asynchronous group send operation.
 * @author Michael Brzycki
 */
@SuppressWarnings("serial")
public class CommunicationGroupSendExecutionContext extends AsynchronousActionContainerContext {

    /**
     * The primary key of the group send to start.
     */
    public static final String GROUP_SEND_ID = "GROUP_SEND_ID";

    /**
     * Default constructor for a scheduled group send execution context.
     */
    public CommunicationGroupSendExecutionContext() {
    }

    /**
     * Constructor for a scheduled group send execution context.
     *
     * @param groupSendId the primary key of the group send to start
     */
    public CommunicationGroupSendExecutionContext(Long groupSendId) {
        super();
        setGroupSendId( groupSendKey );
    }

    /**
     * Sets the primary key of the group send to start.
     * @param groupSendKey primary key of the group send
     */
    public void setGroupSendId( Long groupSendId ) {
        addProperty( GROUP_SEND_ID, Long.toString( groupSendId ) );
    }

    /**
     * Returns the primary key of the group send to start
     * @return group sendKey the key of the group send to start
     */
    public Long getGroupSendId() {
        String s = getProperty( GROUP_SEND_ID );
        return s == null ? null : Long.valueOf( s );
    }

}
