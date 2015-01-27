package net.hedtech.banner.general.communication.item

public enum CommunicationChannel implements Serializable {

    EMAIL,
    LETTER,
    ERROR;

    /**
     * Returns a Set of all predefined CommunicationChannel.
     * @return Set&lt;CommunicationChannel&gt; the set of all CommunicationChannel.
     */
    public Set<CommunicationChannel> set() {
        return EnumSet.range( CommunicationChannel.EMAIL, CommunicationChannel.ERROR );
    }
}