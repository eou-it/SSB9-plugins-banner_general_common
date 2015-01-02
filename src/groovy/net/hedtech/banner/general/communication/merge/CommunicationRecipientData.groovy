/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.merge

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
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
        @NamedQuery(name = "CommunicationRecipientData.findByTemplateId",
                query = """ FROM CommunicationRecipientData a
                    WHERE  a.templateId = :templateId"""),
        @NamedQuery(name = "CommunicationRecipientData.findByReferenceId",
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
    Long Id

    /**
     * Pidm that belong to a selection list.
     */
    @Column(name = "GCBRDAT_PIDM")
    Long pidm


    @Column(name = "GCBRDAT_TEMPLATE_ID")
    Long templateId


    @Column(name = "GCBRDAT_REFERENCE_ID")
    String referenceId

    @Column(name = "GCBRDAT_OWNER_ID")
    String ownerId


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
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationOrganization organization

    public static List findByTemplateId(templateId) {

        def queryList
        CommunicationRecipientData.withSession { session ->
            queryList = session.getNamedQuery('CommunicationRecipientData.findByTemplateId')
                    .setLong('templateId', templateId)
                    .list()
        }
        return queryList
    }

    public static List findByReferenceId(referenceId) {

        def queryList
        CommunicationRecipientData.withSession { session ->
            queryList = session.getNamedQuery('CommunicationRecipientData.findByReferenceId')
                    .setLong('referenceId', referenceId)
                    .list()
        }
        return queryList
    }

}

