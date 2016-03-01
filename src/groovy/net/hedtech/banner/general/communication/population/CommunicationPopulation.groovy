/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.general.communication.folder.CommunicationFolder

import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

class CommunicationPopulation implements Serializable {


    Long id
    String name
    String description
    CommunicationFolder folder
    Date createDate
    String createdBy
    Long version
    Date lastModified
    String lastModifiedBy
    String dataOrigin

    public static CommunicationPopulation fetchById(Long id) {
        def population
        CommunicationPopulationQuery.withSession { session ->
            population = session.getNamedQuery('CommunicationPopulation.fetchById')
                    .setLong('id', id).list()[0]

        }
        return population
    }

}
