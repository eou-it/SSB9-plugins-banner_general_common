/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.FlushMode
import org.hibernate.annotations.Type

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
        @NamedQuery(name = "CommunicationTemplate.fetchById",
                query = """ FROM CommunicationTemplate a
                    WHERE a.id = :id"""),
        @NamedQuery(name = "CommunicationTemplate.fetchByIdAndMepCode",
                query = """ FROM CommunicationTemplate a
                    WHERE a.id = :id
                      AND (:mepCode is null or a.mepCode is null or a.mepCode = :mepCode)"""),
        @NamedQuery(name = "CommunicationTemplate.existsAnotherNameFolder",
                query = """ FROM CommunicationTemplate a
                    WHERE a.folder.name = :folderName
                    AND   upper(a.name) = upper(:templateName)
                    AND   a.id <> :id""")
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
     * Indicates if the template is a personal template. Personal templates are available
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

    @Column(name="GCBTMPL_VPDI_CODE", insertable = false, updatable = false )
    String mepCode

    @Column(name="GCBTMPL_COMM_CHANNEL")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

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
        mepCode(nullable: true)
        communicationChannel(nullable: false)
    }

    CommunicationTemplate( CommunicationChannel communicationChannel ) {
        this.communicationChannel = communicationChannel
    }

    /**
     * Calls the visitor method that pertains to type of this object. An email template will call the
     * visitEmail method on the visitor. Useful when inheritance is not an option or when the code needs
     * to traverse a collection of objects.
     *
     * @param visitor an object implementing the template visitor interface and typically a method object itself
     * @return
     */
    public abstract CommunicationTemplateVisitor accept( CommunicationTemplateVisitor visitor )

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

    public static CommunicationTemplate fetchByIdAndMepCode(Long id, String mepCode) {

        def query
        if (mepCode != null) {
            CommunicationTemplate.withSession { session ->
                query = session.getNamedQuery('CommunicationTemplate.fetchById')
                        .setLong('id', id)
                        .list()[0]
            }
        }
        else {
            CommunicationTemplate.withSession { session ->
                query = session.getNamedQuery('CommunicationTemplate.fetchByIdAndMepCode')
                        .setLong('id', id)
                        .setString('mepCode', mepCode)
                        .list()[0]
            }
        }
        return query
    }

    public static Boolean existsAnotherNameFolder(Long templateId, String templateName, String folderName) {

        def query
        CommunicationTemplate.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
            query = session.getNamedQuery('CommunicationTemplate.existsAnotherNameFolder')
                    .setString('folderName', folderName)
                    .setString('templateName', templateName)
                    .setLong('id', templateId).list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (query != null)
    }


}
