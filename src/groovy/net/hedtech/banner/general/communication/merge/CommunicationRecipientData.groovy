/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.merge

import groovy.transform.EqualsAndHashCode

import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

/**
 A Recipient Data instance contains the evaluated values of communication fields referenced in the template.
 *
 */
@Entity
@Table(name = "GCBRDAT")
@EqualsAndHashCode
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

    /**
     * Pidm that belong to a selection list.
     */
    @Column(name = "GCBRDAT_PIDM")
    Long pidm

    @ElementCollection(fetch=FetchType.EAGER)
    @MapKeyColumn(name = "GCRFVAL_canonicalForm", insertable = false, updatable = false)
    @CollectionTable(
        name="GCRFVAL",
        joinColumns = @JoinColumn(name = "GCBRDAT_SURROGATE_ID")
    )
    Map<String,CommunicationFieldValue> fieldValues = Collections.emptyMap(); // maps canonical form to field value

}
