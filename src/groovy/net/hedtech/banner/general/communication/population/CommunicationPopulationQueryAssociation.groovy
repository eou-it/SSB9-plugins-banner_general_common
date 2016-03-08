/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion

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
@Table(name = "GCRPQID")
@NamedQueries(value = [
    @NamedQuery(name = "CommunicationPopulationQueryAssociation.findAllByPopulation",
            query = """ FROM CommunicationPopulationQueryAssociation a
                WHERE a.population = :population""")
])
class CommunicationPopulationQueryAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRPQID_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPQID_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPQID_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPQID_SEQ_GEN")
    Long id

    /**
     * Foreign key reference to the population.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPQID_POPL_ID", referencedColumnName = "GCBPOPL_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPQID_INV_GCBPOPL")
    CommunicationPopulation population

    /**
     * Foreign key reference to the population query.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPQID_QURY_ID", referencedColumnName = "GCBQURY_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPQID_INV_GCBQURY")
    CommunicationPopulationQuery populationQuery

    /**
     * Foreign key reference to the population query version.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRPQID_QRYV_ID", referencedColumnName = "GCRQRYV_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRPQID_INV_GCRQRYV")
    CommunicationPopulationQueryVersion populationQueryVersion

    /**
     * ACTIVITY_DATE: Most current date record was created or changed.
     */
    @Column(name = "GCRPQID_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER_ID: Oracle User ID of the person who last inserted or last updated the data.
     */
    @Column(name = "GCRPQID_USER_ID")
    String lastModifiedBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "GCRPQID_VERSION")
    Long version

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPQID_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        population(nullable: false)
        populationQuery(nullable: false)
        populationQueryVersion(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    public static List findAllByPopulation( CommunicationPopulation population ) {
        def list
        CommunicationPopulationQueryAssociation.withSession { session ->
            list = session.getNamedQuery( 'CommunicationPopulationQueryAssociation.findAllByPopulation' ).setParameter( 'population', population ).list()
        }
        return list
    }
}
