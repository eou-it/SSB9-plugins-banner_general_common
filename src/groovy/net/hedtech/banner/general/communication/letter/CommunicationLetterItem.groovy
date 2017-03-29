/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationItem

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

/**
 * A letter item.
 */
@Entity
@Table(name = "GCRLETM")
@PrimaryKeyJoinColumn(name = "GCRLETM_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationLetterItem extends CommunicationItem implements Serializable {

    @Column(name = "GCRLETM_TOADDRESS")
    String toAddress

    @Column(name = "GCRLETM_CONTENT")
    String content

    @Column(name = "GCRLETM_STYLE")
    String style


    static constraints = {
        toAddress(nullable: false)
        content(nullable: false)
        style(nullable:true)
    }
}
