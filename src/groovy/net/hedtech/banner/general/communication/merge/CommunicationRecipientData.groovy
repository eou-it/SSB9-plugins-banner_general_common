/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.merge

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.organization.CommunicationOrganization

import javax.persistence.*

/**
 A Recipient Data instance contains the evaluated values of communication fields referenced in the template.
 *
 */
@Entity
@Table(name = "GCBRDAT")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationRecipientData.fetchByTemplateId",
                query = """ FROM CommunicationRecipientData a
                    WHERE  a.templateId = :templateId"""),
        @NamedQuery(name = "CommunicationRecipientData.fetchByReferenceId",
                query = """ FROM CommunicationRecipientData a
                    WHERE  a.referenceId = :referenceId""")
])
class CommunicationRecipientData {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBRDAT_SURROGATE_ID")
    @SequenceGenerator(name = "GCBRDAT_SEQ_GEN", allocationSize = 1, sequenceName = "GCBRDAT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBRDAT_SEQ_GEN")
    Long id

    /**
     * Pidm that belong to a selection list.
     */
    @Column(name = "GCBRDAT_PIDM")
    Long pidm


    @Column(name = "GCBRDAT_TEMPLATE_ID")
    Long templateId

    /** Describes which communication channel of the template the recipient data refers to. **/
    @Column(name = "GCBRDAT_COMM_CHANNEL")
    @Enumerated(EnumType.STRING)
    CommunicationChannel communicationChannel

    @Column(name = "GCBRDAT_REFERENCE_ID")
    String referenceId

    @Column(name = "GCBRDAT_OWNER_ID")
    String ownerId

    @Column(name = "GCBRDAT_VPDI_CODE")
    String mepCode

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "GCRFVAL_canonicalForm", insertable = false, updatable = false)
    @CollectionTable(
            name = "GCRFVAL",
            joinColumns = @JoinColumn(name = "GCRFVAL_RECIPIENT_DATA_ID")
    )
    Map<String, CommunicationFieldValue> fieldValues = Collections.emptyMap(); // maps canonical form to field value

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBRDAT_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBRDAT_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBRDAT_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBRDAT_DATA_ORIGIN")
    String dataOrigin

    @JoinColumn(name="GCBRDAT_ORGANIZATION_ID" )
    @ManyToOne( fetch = FetchType.EAGER )
    CommunicationOrganization organization

    static constraints = {
        communicationChannel(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        mepCode(nullable:true)
    }

    public static List fetchByTemplateId(templateId) {

        def queryList
        CommunicationRecipientData.withSession { session ->
            queryList = session.getNamedQuery('CommunicationRecipientData.fetchByTemplateId')
                    .setLong('templateId', templateId)
                    .list()
        }
        return queryList
    }

    public static List fetchByReferenceId(referenceId) {

        def queryList
        CommunicationRecipientData.withSession { session ->
            queryList = session.getNamedQuery('CommunicationRecipientData.fetchByReferenceId')
                    .setString('referenceId', referenceId)
                    .list()
        }
        return queryList
    }

}

