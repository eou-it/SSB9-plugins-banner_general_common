/*********************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.event

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.hibernate.FlushMode
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 * Mapping for templates, organization and query for an event
 */
@Entity
@Table(name = "GCBEVMP")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationEventMapping.fetchByName",
                query = """ FROM CommunicationEventMapping a
                    WHERE lower(a.eventName) = lower(:eventName)"""),
        @NamedQuery(name = "CommunicationEventMapping.existsAnotherSameNameEvent",
                query = """ FROM CommunicationEventMapping a
                    WHERE lower(a.eventName) = lower(:eventName)
                    AND   a.id <> :id""")
])
class CommunicationEventMapping implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCBEVMP_SURROGATE_ID")
    @SequenceGenerator(name = "GCBEVMP_SEQ_GEN", allocationSize = 1, sequenceName = "GCBEVMP_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBEVMP_SEQ_GEN")
    Long id

    /**
     * Event Code.
     */
    @Column(name = "GCBEVMP_EVENT_NAME")
    String eventName

    /**
     * ID of the organization to be used for sending messages for this event.
     */
    @ManyToOne(optional = true)
    @JoinColumn(name = "GCBEVMP_ORGANIZATION_ID", referencedColumnName = "GCRORAN_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBEVMP_INV_GCRORAN")
    CommunicationOrganization organization


    /**
     * ID of the template to be used for sending messages for this event.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCBEVMP_TEMPLATE_ID", referencedColumnName = "GCBTMPL_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCBEVMP_INV_GCBTMPL")
    CommunicationTemplate template

    /**
     * ID of the query to be used for calculating the population for this event.
     */
    @Column(name = "GCBEVMP_QUERY_ID")
    Long queryId

    /**
     * Indicates if the event mapping is active for use.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBEVMP_ACTIVE_IND")
    Boolean isActive = false

    /**
     * Indicates if the event mapping was created through the seeded data set and should not be deleted or modified in any way.
     */
    @Type(type = "yes_no")
    @Column(name = "GCBEVMP_SYSTEM_REQ_IND")
    Boolean systemIndicator = false

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCBEVMP_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBEVMP_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCBEVMP_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCBEVMP_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        eventName(nullable: false, maxSize: 255)
        organization(nullable:true)
        template(nullable: false)
        queryId(nullable: true)
        systemIndicator(nullable: false)
        isActive(nullable:false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static CommunicationEventMapping fetchByName(String eventName) {

        def query
        CommunicationEventMapping.withSession { session ->
            query = session.getNamedQuery('CommunicationEventMapping.fetchByName')
                    .setString('eventName', eventName)
                    .list()[0]
        }
        return query
    }

    public static Boolean existsAnotherSameNameEvent(Long eventId, String eventName ) {

        def query

        CommunicationEventMapping.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
                query = session.getNamedQuery('CommunicationEventMapping.existsAnotherSameNameEvent')
                        .setString('eventName', eventName)
                        .setLong('id', eventId)
                        .list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (query != null)
    }
}
