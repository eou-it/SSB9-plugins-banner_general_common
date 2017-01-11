/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.parameter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.field.CommunicationField

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
@Table(name = "GCRFLPM")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationParameterFieldAssociation.findAllByField",
                query = """ FROM CommunicationParameterFieldAssociation a
                WHERE a.field = :field""")
])
class CommunicationParameterFieldAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRFLPM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRFLPM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRFLPM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRFLPM_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the communication parameter.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRFLPM_PARAMETER_ID", referencedColumnName = "GCRPARM_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRFLPM_INV_GCRPARM")
    CommunicationParameter parameter

    /**
     * Foreign key reference to the communication field.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRFLPM_FIELD_ID", referencedColumnName = "GCRCFLD_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRFLPM_INV_GCRCFLD")
    CommunicationField field

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRFLPM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRFLPM_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRFLPM_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRFLPM_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        parameter(nullable: false)
        field(nullable: false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    public static List findAllByField( CommunicationField field ) {
        def list
        CommunicationParameterFieldAssociation.withSession { session ->
            list = session.getNamedQuery( 'CommunicationParameterFieldAssociation.findAllByField' ).setParameter( 'field', field ).list()
        }
        return list
    }
}
