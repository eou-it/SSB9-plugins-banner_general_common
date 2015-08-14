/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

@Entity
@Table(name = "GCBTMPL")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationTemplate.fetchByTemplateNameAndFolderName",
                query = """ FROM CommunicationTemplate a
                    WHERE a.folder.name = :folderName
                      AND upper(a.name) = upper(:templateName)"""),
        @NamedQuery(name = "CommunicationTemplate.existsAnotherNameFolder",
                query = """ FROM CommunicationTemplate a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:templateName)
                    AND   a.id <> :id"""),
        @NamedQuery(name = "CommunicationTemplate.fetchPublishedActivePublicByFolderId",
                query = """ FROM CommunicationTemplate a
                    WHERE a.folder.id = :folderId
                    AND a.published = 'Y'
                    AND SYSDATE between NVL(validFrom,SYSDATE) and NVL(validTo, SYSDATE)
                    AND personal = 'N'""")
])
public abstract class CommunicationTemplate implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBTMPL_SURROGATE_ID")
    @SequenceGenerator(name = "GCBTMPL_SEQ_GEN", allocationSize = 1, sequenceName = "GCBTMPL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBTMPL_SEQ_GEN")
    Long id

    /*
     * Description of the communication template.
    */
    @Column(name = "GCBTMPL_DESCRIPTION")
    String description

    /*
     *  Name of the communication template.
    */
    @Column(name = "GCBTMPL_NAME")
    String name

    /**
     * Indicates if the template is a personal template (1=Yes or 0=No). Personal templates are available
     * only for use by the owner and are not available to other users.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_PERSONAL")
    Boolean personal = false

    /**
     * Foreign key reference to the Folder under which this template is organized.
     */

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCBTMPL_FOLDER_ID", referencedColumnName = "GCRFLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBTMPL_INV_GCRFLDR_KEY")
    CommunicationFolder folder

    /**
     * Indicates if the template can be used for communications (1=Yes or 0=No). A template can be used for communication if it is active.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_ACTIVE")
    Boolean active = true

    /**
     * Indicates if this is a one-off version of the template (1=Yes or 0=No).
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_ONEOFF")
    Boolean oneOff = false

    /**
     * Indicates if this template version is published (1=Yes or 0=No). A template is published to make it available for the BCM subsystem.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_PUBLISH")
    Boolean published = false

    /**
     * Date from which the template version is available for use.
     */
    @Column(name = "GCBTMPL_VALID_FROM")
    @Temporal(TemporalType.TIMESTAMP)
    Date validFrom

    /**
     * Date from which the template version is no longer available for use. A template version must be active and the current date must fall within the validfrom and validto dates to be used for further co
     mmunication.
     */
    @Column(name = "GCBTMPL_VALID_TO")
    @Temporal(TemporalType.TIMESTAMP)
    Date validTo

    /**
     *  The user ID of the person who inserted this record.
     */
    @Column(name = "GCBTMPL_CREATOR_ID")
    String createdBy

    /**
     *  Date record is created
     */
    @Column(name = "GCBTMPL_CREATE_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date createDate

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBTMPL_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBTMPL_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBTMPL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBTMPL_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false, maxSize: 250)
        description(nullable: true, maxSize: 2000)
        personal(nullable: false)
        folder(nullable: false)
        validFrom(nullable: true)
        validTo(nullable: true)
        createdBy(nullable: true)
        createDate(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    /**
     * Temporary method implemented by subclasses to indicate which kind of communication channel each serves.
     * Eventually templates will support more than one kind of communication channel and the choice of channel
     * between will be a runtime decision based on channels supported by the template and the parameters of
     * the communication plan.
     */
    public abstract CommunicationChannel getCommunicationChannel()

    /******************* Named Queries *******************/

    public static CommunicationTemplate fetchByTemplateNameAndFolderName(String templateName, String folderName) {

        def query
        CommunicationTemplate.withSession { session ->
            query = session.getNamedQuery('CommunicationTemplate.fetchByTemplateNameAndFolderName')
                    .setString('folderName', folderName)
                    .setString('templateName', templateName)
                    .list()[0]
        }
        return query
    }

    public static List<CommunicationTemplate> fetchPublishedActivePublicByFolderId(Long id) {

        def templateList
        CommunicationTemplate.withSession { session ->
            templateList = session.getNamedQuery('CommunicationTemplate.fetchPublishedActivePublicByFolderId')
                    .setLong('folderId', id)
                    .list()
        }
        return templateList
    }

    public static Boolean existsAnotherNameFolder(Long templateId, String templateName, String folderName) {

        def query
        CommunicationTemplate.withSession { session ->
            query = session.getNamedQuery('CommunicationTemplate.existsAnotherNameFolder')
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

        def queryCriteria = CommunicationTemplate.createCriteria()
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
        def queryCriteria = CommunicationTemplate.createCriteria()

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
