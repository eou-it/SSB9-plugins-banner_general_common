/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.query.DynamicFinder

import javax.persistence.*

@Entity
@Table(name = "GCBEMTL")
@PrimaryKeyJoinColumn(name = "GCBEMTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString

@NamedQueries(value = [
        @NamedQuery(name = "CommunicationEmailTemplate.fetchByTemplateNameAndFolderName",
                query = """ FROM CommunicationEmailTemplate a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:templateName)"""),
        @NamedQuery(name = "CommunicationEmailTemplate.existsAnotherNameFolder",
                query = """ FROM CommunicationEmailTemplate a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:templateName)
                    AND   a.id <> :id""")
])
class CommunicationEmailTemplate extends CommunicationTemplate implements Serializable {


    /**
     * The BCC (blind carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_BCCLIST")
    String bccList

    /**
     * The CC (carbon copy) attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_CCLIST")
    String ccList

    /**
     * The message body of the email with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_CONTENT")
    String content

    /**
     * The FROM attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_FROMLIST")
    String fromList

    /**
     * The SUBJECT attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_SUBJECT")
    String subject

    /**
     * The TO attribute of an email message with placeholders in raw format.
     */
    @Column(name = "GCBEMTL_TOLIST")
    String toList

    static constraints = {
        bccList(nullable: true, maxSize: 1020)
        ccList(nullable: true, maxSize: 1020)
        content(nullable: true)
        fromList(nullable: true, maxSize: 1020)
        subject(nullable: true, maxSize: 1020)
        toList(nullable: true, maxSize: 1020)

    }
    public static CommunicationEmailTemplate fetchByTemplateNameAndFolderName(String templateName, String folderName) {

        def query
        CommunicationEmailTemplate.withSession { session ->
            query = session.getNamedQuery('CommunicationEmailTemplate.fetchByTemplateNameAndFolderName')
                    .setString('folderName', folderName)
                    .setString('templateName', templateName)
                    .list()[0]
        }
        return query
    }

    public static Boolean existsAnotherNameFolder(Long templateId, String templateName, String folderName) {

        def query
        CommunicationEmailTemplate.withSession { session ->
            query = session.getNamedQuery('CommunicationEmailTemplate.existsAnotherNameFolder')
                    .setString('folderName', folderName)
                    .setString('templateName', templateName)
                    .setLong('id', templateId).list()[0]

        }
        return (query != null)
    }
}
