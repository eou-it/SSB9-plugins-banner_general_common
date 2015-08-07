/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

@Entity
@Table(name = "GCBMNTL")
@PrimaryKeyJoinColumn(name = "GCBMNTL_SURROGATE_ID")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationMobileNotificationTemplate.fetchByTemplateNameAndFolderName",
                query = """ FROM CommunicationMobileNotificationTemplate a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:templateName)"""),
        @NamedQuery(name = "CommunicationMobileNotificationTemplate.existsAnotherNameFolder",
                query = """ FROM CommunicationMobileNotificationTemplate a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:templateName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationMobileNotificationTemplate.fetchPublishedActivePublicByFolderId",
                query = """ FROM CommunicationMobileNotificationTemplate a
                    WHERE a.folder.id = :folderId
                    AND a.published = 'Y'
                    AND SYSDATE between NVL(validFrom,SYSDATE) and NVL(validTo, SYSDATE)
                    AND personal = 'N'""")
])
class CommunicationMobileNotificationTemplate extends CommunicationTemplate implements Serializable {

    @Column(name = "GCBMNTL_MOBILE_HEADLINE", nullable = false)
    String mobileHeadline // 250 max

    @Column(name = "GCBMNTL_HEADLINE", nullable = true)
    String headline // 250 max

    @Column(name = "GCBMNTL_BODY", nullable = true)
    String body // 2500 max

    @Column(name = "GCBMNTL_DESTINATION_LINK", nullable = true)
    String destinationLink // 250 max

    @Column(name = "GCBMNTL_DESTINATION_LABEL", nullable = true)
    String destinationLabel // 250 max

    @Column(name = "GCBMNTL_EXPIRATION_POLICY", nullable = false)
    String expirationPolicy // enum NO_EXPIRATION, ELAPSED_TIME, DATE_TIME // varchar(64)

    @Column(name = "GCBMNTL_ELAPSED_TIME_SECS", nullable = true)
    Long elapsedTimeSeconds

    @Column(name = "GCBMNTL_EXPIRATION_DATE_TIME", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    Date expirationDateTime

    @Column(name = "GCBMNTL_PUSH", nullable = false)
    @Type(type="yes_no")
    boolean push

    @Column(name = "GCBMNTL_STICKY", nullable = false)
    @Type(type="yes_no")
    boolean sticky

    static constraints = {
        mobileHeadline(nullable: false, maxSize: 250)
        headline(nullable: true, maxSize: 250)
        body(nullable: true, maxSize: 2500)
        destinationLink(nullable: true, maxSize: 250)
        destinationLabel(nullable: true, maxSize: 250)
        expirationPolicy(nullable: true)
        elapsedTimeSeconds(nullable: true)
        expirationDateTime(nullable: true)
        push(nullable: false)
        sticky(nullable: false)
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

    public static List<CommunicationEmailTemplate> fetchPublishedActivePublicByFolderId(Long id) {

        def templateList
        CommunicationEmailTemplate.withSession { session ->
            templateList = session.getNamedQuery('CommunicationEmailTemplate.fetchPublishedActivePublicByFolderId')
                    .setLong('folderId', id)
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
/**
 * Return list of templates along with count for display on list page
 * will return all shared templates and personal templates belonging to the current user
 * @param filterData
 * @param pagingAndSortParams
 * @return
 */
    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationEmailTemplate.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            and {
                or {
                    eq("personal",false)
                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName().toLowerCase(),[ignoreCase: true])
                }
            }
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    /**
     * Returns a list of templates to be used when sending messages to a population
     * will return all templates that are active, shared and personal templates belonging to the user will be returned
     * @param filterData
     * @return
     */

    public static findByFolderForSend(filterData) {

        def currentDate = new Date()
        def queryCriteria = CommunicationEmailTemplate.createCriteria()

        def results = queryCriteria.list {
            folder {
                eq("name", filterData?.params?.folderName?.toLowerCase(), [ignoreCase: true])
            }
            eq("published",true)
            and {
                or {
                    eq("personal",false)
                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName().toLowerCase(),[ignoreCase: true])
                }
            }
            le("validFrom",currentDate)
            and {
                    or {
                        isNull("validTo")
                        ge("validTo",currentDate)
                    }
                }
            order( Order.asc("name").ignoreCase())
        }
        return results
    }

}
