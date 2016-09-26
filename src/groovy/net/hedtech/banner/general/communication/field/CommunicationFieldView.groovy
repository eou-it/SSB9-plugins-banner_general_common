/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

/**
 * Communication Field View. Denotes a communication field in a field set. entity.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRCFLD")
class CommunicationFieldView implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "SURROGATE_ID")
    Long id

    /**
     * Name of the field.
     */
    @Column(name = "NAME")
    String name

    /**
     * Immutable Id.
     */
    @Column(name = "IMMUTABLE_ID")
    String immutableId

    /**
     * FOLDER: The folder containing this object.
     */
    @Column(name = "FOLDER_ID")
    String folderId

    /**
     * FOLDER: The folder containing this object.
     */
    @Column(name = "FOLDER_NAME")
    String folderName

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

}
