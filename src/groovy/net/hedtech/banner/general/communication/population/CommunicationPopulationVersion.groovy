/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import org.hibernate.annotations.Type

import javax.persistence.*


class CommunicationPopulationVersion implements Serializable {


    Long id
    CommunicationPopulation population
    Date createDate
    String createdBy
    Long version
    Date lastModified
    String lastModifiedBy
    String dataOrigin
    String sqlString

    static constraints = {
        population(nullable: false)
        createDate(nullable: false)
        createdBy(nullable: false, maxSize: 30)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


    /**
     * Returns a list of population versions by the parent population id.
     * @param populationId the id of the owning population
     * @return a list of population versions
     */
    public static List findByPopulationId( Long populationId ) {
        def population
        CommunicationPopulationVersion.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulationVersion.findByPopulationId').setLong( 'populationId', populationId ).list()
        }
        return population
    }

    public static CommunicationPopulationVersion fetchById(Long id) {

        def population
        CommunicationPopulationVersion.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulationVersion.fetchById')
                    .setLong('id', id).list()[0]

        }
        return population
    }
}
