/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.query.DynamicFinder
import org.hibernate.annotations.Type

import javax.persistence.*


@Entity
@Table(name = "GCBTMPL")
@Inheritance(strategy = InheritanceType.JOINED)
@EqualsAndHashCode
@ToString

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
    Boolean personal

    /**
     * Foreign key reference to the Folder under which this template is organized.
     */

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCBTMPL_FOLDER_ID", referencedColumnName = "GCFOLDR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBTMPL_INV_GCFOLDR_KEY")
    CommunicationFolder folder

    /**
     * Indicates if the template can be used for communications (1=Yes or 0=No). A template can be used for communication if it is active.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_ACTIVE")
    Boolean active

    /**
     * Indicates if this is a one-off version of the template (1=Yes or 0=No).
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_ONEOFF")
    Boolean oneOff

    /**
     * Indicates if this template version is published (1=Yes or 0=No). A template is published to make it available for the BCM subsystem.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBTMPL_PUBLISH")
    Boolean published

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
     *  The user ID of the person who inserted or last updated this record.
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
        name(nullable: false, maxSize: 1020)
        description(nullable: false, maxSize: 4000)
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


    public static List findAllByFolderName(String folderName) {

        def queries = []
        CommunicationTemplate.withSession { session ->
            queries = session.getNamedQuery('CommunicationTemplate.findAllByFolderName')
                    .setString('folderName', folderName).list()
        }
        return queries
    }


    public static String getQuery(Map filterData) {
        def query =
                """ FROM CommunicationTemplate a
                """
        def predicateArray = []

        if (filterData?.params?.containsKey('name')) {
            predicateArray.push("""(upper(a.name) like upper(:name))""")
        }

        if (predicateArray.size() > 0) {
            query = query + """ WHERE """ + predicateArray.join(""" AND """)
        }

        return query
    }


    public static findByFilterPagingParams(filterData, pagingAndSortParams) {
        return (new DynamicFinder(CommunicationTemplate.class, getQuery(filterData), "a")).find(filterData,
                pagingAndSortParams)
    }


    public static countByFilterParams(filterData) {
        return new DynamicFinder(CommunicationTemplate.class, getQuery(filterData), "a").count(filterData)
    }


}
