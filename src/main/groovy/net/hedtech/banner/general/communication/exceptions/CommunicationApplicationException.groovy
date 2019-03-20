/* ****************************************************************************
Copyright 2015 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
package net.hedtech.banner.general.communication.exceptions

import net.hedtech.banner.exceptions.ApplicationException

class CommunicationApplicationException extends ApplicationException {

    public CommunicationApplicationException(entityClassOrName, Throwable e)
    {
        super(entityClassOrName, e)
    }

    public CommunicationApplicationException( entityClassOrName, String msg, String defaultMessage = null ) {
        super(entityClassOrName, msg, defaultMessage = null)
    }

    public String getType() {
        friendlyName
    }
}
