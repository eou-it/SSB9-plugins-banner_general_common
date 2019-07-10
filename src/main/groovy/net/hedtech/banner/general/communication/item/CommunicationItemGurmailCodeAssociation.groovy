/*********************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.system.LetterProcessLetter

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRLMAP")
class CommunicationItemGurmailCodeAssociation implements Serializable {

    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "GCRLMAP_SURROGATE_ID")
    @SequenceGenerator(name = "GCRLMAP_SEQ_GEN", allocationSize = 1, sequenceName = "GCRLMAP_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRLMAP_SEQ_GEN")
    Long id;

    @Column(name = "GCRLMAP_REFERENCE_ID")
    String referenceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "GCRLMAP_LETR_ID", referencedColumnName = "GTVLETR_SURROGATE_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK1_GCRLMAP_INV_GTVLETR_KEY")
    LetterProcessLetter communicationCode;

    @Column(name = "GCRLMAP_GURMAIL_ACTION")
    String gurmailAction;

    /**
     * Error Code: The error code for the error scenario that failed the gurmail insert/update
     */
    @Column(name = "GCRLMAP_ERROR_CODE")
    @Enumerated(EnumType.STRING)
    CommunicationErrorCode errorCode

    @Lob
    @Column(name = "GCRLMAP_ERROR_TEXT")
    String errorText

    /**
     *  Optimistic lock token.
     */
    @Version
    @Column(name = "GCRLMAP_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRLMAP_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCRLMAP_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCRLMAP_DATA_ORIGIN")
    String dataOrigin

    @Column(name = "GCRLMAP_VPDI_CODE")
    String mepCode


    static constraints = {
        referenceId(nullable: false)
        communicationCode(nullable: false)
        gurmailAction(nullable: true)
        errorCode(nullable: true)
        errorText(nullable: true)
        mepCode(nullable: true)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }
}
