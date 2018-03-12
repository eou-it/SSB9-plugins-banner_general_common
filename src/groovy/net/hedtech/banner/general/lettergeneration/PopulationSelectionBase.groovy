/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.lettergeneration

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator
import javax.persistence.Temporal
import javax.persistence.TemporalType
import org.hibernate.annotations.Type
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString


/**
 * Population Selection Base Table
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GLBSLCT")
class PopulationSelectionBase implements Serializable {

    /**
     * Surrogate ID for GLBSLCT
     */
    @Id
    @Column(name = "GLBSLCT_SURROGATE_ID")
    @SequenceGenerator(name = "GLBSLCT_SEQ_GEN", allocationSize = 1, sequenceName = "GLBSLCT_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GLBSLCT_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GLBSLCT
     */
    @Version
    @Column(name = "GLBSLCT_VERSION")
    Long version

    /**
     * APPLICATION: Application code which with the selectino code uniquely identifies this record.
     */
    @Column(name = "GLBSLCT_APPLICATION")
    String application

    /**
     * SELECTION: Selection code which with the application code uniquely identifies this record.
     */
    @Column(name = "GLBSLCT_SELECTION")
    String selection

    /**
     * CREATOR ID: The user id of the person who created this population selection.
     */
    @Column(name = "GLBSLCT_CREATOR_ID")
    String creatorId

    /**
     * DESCRIPTION: Description of this population selection.
     */
    @Column(name = "GLBSLCT_DESC")
    String description

    /**
     * LOCK INDICATOR: Indicator which indicates that this population selection is locked and cannot be executedby any other user.
     */
    @Type(type = "yes_no")
    @Column(name = "GLBSLCT_LOCK_IND")
    Boolean lockIndicator

    /**
     * SELECTION TYPE: Values are (M)anual PIDM joins required on form - GLOLETT will not add them, or blank.
     */
    @Column(name = "GLBSLCT_TYPE_IND")
    String typeIndicator

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GLBSLCT_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for GLBSLCT
     */
    @Column(name = "GLBSLCT_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GLBSLCT
     */
    @Column(name = "GLBSLCT_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        application( nullable: false, maxSize: 30 )
        selection( nullable: false, maxSize: 30 )
        description( nullable: false, maxSize: 30 )
        lockIndicator( nullable: false )
        typeIndicator( nullable: true, maxSize: 1, inList: ["M"] )
        lastModified( nullable: true )
        lastModifiedBy( nullable: true, maxSize: 30 )
        dataOrigin( nullable: true, maxSize: 30 )
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = ['application', 'selection', 'creatorId']
}
