/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

/**
 * Describes an object that holds fields containing final content for channel delivery.
 */
public
abstract class CommunicationMessage implements Serializable, Cloneable {
    private Date dateSent

    public Date getDateSent() {
        return dateSent
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent
    }
}