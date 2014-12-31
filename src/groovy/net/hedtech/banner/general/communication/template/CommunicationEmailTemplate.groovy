/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.criterion.Order

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
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationEmailTemplate.fetchPublishedActivePublicByFolderName",
                query = """ FROM CommunicationEmailTemplate a
                    WHERE a.folder.name = :folderName
                    AND a.active = 'Y'
                    AND a.published = 'Y'
                    AND SYSDATE between validFrom and validTo
                    AND personal = 'N'""")
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

    public static List<CommunicationEmailTemplate> fetchPublishedActivePublicByFolderName(String folderName) {

        def templateList
        CommunicationEmailTemplate.withSession { session ->
            templateList = session.getNamedQuery('CommunicationEmailTemplate.fetchPublishedActivePublicByFolderName')
                    .setString('folderName', folderName)
                    .list()
        }
        return templateList
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

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationEmailTemplate.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }


}
