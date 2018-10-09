/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.textmessage

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table

@Entity
@Table(name = "GCBTMTL")
@PrimaryKeyJoinColumn(name = "GCBTMTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationTextMessageTemplate extends CommunicationTemplate implements Serializable {

    @Column(name = "GCBTMTL_TOLIST")
    String toList

    @Column(name = "GCBTMTL_FOOTER", nullable = true)
    String footer

    @Column(name = "GCBTMTL_MESSAGE", nullable = true)
    String message

    @Column(name = "GCBTMTL_DESTINATION_LINK", nullable = true)
    String destinationLink

    @Column(name = "GCBTMTL_DESTINATION_LABEL", nullable = true)
    String destinationLabel

    public CommunicationTextMessageTemplate() {
        super( CommunicationChannel.TEXT_MESSAGE )
    }

    static constraints = {
        toList(nullable: true, maxSize: 1020)
        footer(nullable: true)
        message(nullable: true)
        destinationLink(nullable: true)
        destinationLabel(nullable: true)
    }

    @Override
    final CommunicationTemplateVisitor accept(CommunicationTemplateVisitor visitor) {
        visitor.visitTextMessage( this )
    }
}
