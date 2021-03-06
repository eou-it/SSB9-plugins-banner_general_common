/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.general.communication.template.CommunicationTemplateVisitor

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Lob
import javax.persistence.PrimaryKeyJoinColumn
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "GCBLTPL")
@PrimaryKeyJoinColumn(name = "GCBLTPL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
class CommunicationLetterTemplate extends CommunicationTemplate implements Serializable {

    @Lob
    @Column(name = "GCBLTPL_TOADDRESS")
    String toAddress

    @Lob
    @Column(name = "GCBLTPL_STYLE")
    String style

    @Lob
    @Column(name = "GCBLTPL_CONTENT")
    String content

    @Transient
    Long parentVersion

    static constraints = {
        toAddress(nullable: true)
        style(nullable: true)
        content(nullable: true)
    }

    public CommunicationLetterTemplate() {
        super( CommunicationChannel.LETTER )
    }

    Long getParentVersion() {
        return super.getVersion()
    }

    @Override
    final CommunicationTemplateVisitor accept(CommunicationTemplateVisitor visitor) {
        visitor.visitLetter( this )
    }
}
