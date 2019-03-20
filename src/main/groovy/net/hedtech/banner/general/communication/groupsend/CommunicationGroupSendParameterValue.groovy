/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRGSPV")
class CommunicationGroupSendParameterValue implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GCRGSPV_SURROGATE_ID")
    @SequenceGenerator(name = "GCRGSPV_SEQ_GEN", allocationSize = 1, sequenceName = "GCRGSPV_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRGSPV_SEQ_GEN")
    Long id

    /**
     * Group Send Id.
     */
    @Column(name = "GCRGSPV_GROUP_SEND_ID")
    Long groupSendId

    /**
     * ID of the parameter.
     */
    @Column(name = "GCRGSPV_PARAMETER_ID")
    Long parameterId

    /**
     * Type of the parameter
     */
    @Column(name = "GCRGSPV_PARAMETER_TYPE")
    CommunicationParameterType parameterType

    /**
     * Value entered for the parameter during group send request
     */
    @Lob
    @Column(name = "GCRGSPV_PARAMETER_VALUE")
    String parameterValue

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRGSPV_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCRGSPV_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRGSPV_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRGSPV_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        groupSendId(nullable: false)
        parameterId(nullable: false)
        parameterType(nullable: false, maxSize: 255)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

}
