/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.parameter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.field.CommunicationField
import net.hedtech.banner.general.communication.template.CommunicationTemplate

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

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRTPFL")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationTemplateFieldAssociation.findAllByField",
                query = """ FROM CommunicationTemplateFieldAssociation a
                WHERE a.field = :field"""),
        @NamedQuery(name = "CommunicationTemplateFieldAssociation.findAllByTemplate",
                query = """ FROM CommunicationTemplateFieldAssociation a
                WHERE a.template = :template"""),
        @NamedQuery(name = "CommunicationTemplateFieldAssociation.findByTemplateAndField",
                query = """ FROM CommunicationTemplateFieldAssociation a
                WHERE a.template = :template
                AND a.field = :field """)
])
class CommunicationTemplateFieldAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRTPFL_SURROGATE_ID")
    @SequenceGenerator(name = "GCRTPFL_SEQ_GEN", allocationSize = 1, sequenceName = "GCRTPFL_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRTPFL_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the communication parameter.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRTPFL_TEMPLATE_ID", referencedColumnName = "GCBTMPL_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRTPFL_INV_GCBTMPL")
    CommunicationTemplate template

    /**
     * Foreign key reference to the communication field.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRTPFL_FIELD_ID", referencedColumnName = "GCRCFLD_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRTPFL_INV_GCRCFLD")
    CommunicationField field

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRTPFL_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRTPFL_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRTPFL_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRTPFL_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        template(nullable: false)
        field(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static List findAllByField( CommunicationField field ) {
        def list
        CommunicationTemplateFieldAssociation.withSession { session ->
            list = session.getNamedQuery( 'CommunicationTemplateFieldAssociation.findAllByField' ).setParameter( 'field', field ).list()
        }
        return list
    }

    public static List findAllByTemplate( CommunicationTemplate template ) {
        def list
        CommunicationTemplateFieldAssociation.withSession { session ->
            list = session.getNamedQuery( 'CommunicationTemplateFieldAssociation.findAllByTemplate' ).setParameter( 'template', template ).list()
        }
        return list
    }

    public static CommunicationTemplateFieldAssociation findByTemplateAndField( CommunicationTemplate template, CommunicationField field ) {
        def templateFieldAssociation
        CommunicationTemplateFieldAssociation.withSession { session ->
            templateFieldAssociation = session.getNamedQuery( 'CommunicationTemplateFieldAssociation.findByTemplateAndField' )
                    .setParameter( 'template', template ).setParameter( 'field', field ).list()[0]
        }
        return templateFieldAssociation
    }
}