/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.item

public enum CommunicationChannel implements Serializable {

    EMAIL,
    MOBILE_NOTIFICATION,
    LETTER,
    ERROR, // reserved
    MANUAL_INTERACTION

    /**
     * Returns a Set of all predefined CommunicationChannel.
     * @return Set&lt;CommunicationChannel&gt; the set of all CommunicationChannel.
     */
    public Set<CommunicationChannel> set() {
        return EnumSet.range( CommunicationChannel.EMAIL, CommunicationChannel.MANUAL_INTERACTION );
    }
}