package net.hedtech.banner.general.communication.groupsend

import net.hedtech.banner.general.communication.parameter.CommunicationParameterType

/**
 * Created by mbrzycki on 2/17/17.
 */
class CommunicationParameterValue implements Serializable {
    Serializable value
    CommunicationParameterType type

    public CommunicationParameterValue() {
    }

    public CommunicationParameterValue( String value ) {
        this.value = value
        this.type = CommunicationParameterType.TEXT
    }

    public CommunicationParameterValue( Number number ) {
        this.value = number
        this.type = CommunicationParameterType.NUMBER
    }

    public CommunicationParameterValue( Date date ) {
        this.value = date
        this.type = CommunicationParameterType.DATE
    }

    public CommunicationParameterValue( Serializable value, CommunicationParameterType type ) {
        this.value = value
        this.type = type
    }
}
