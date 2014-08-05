/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Display Masking Readonly view for finding masking
 */
@NamedQueries(value = [
@NamedQuery(
        name = "DisplayMaskingColumnRuleView.fetchSSBNameMask",
        query = """
           SELECT a.displayIndicator
           FROM DisplayMaskingColumnRuleView a
           WHERE a.object = '**SSB_MASKING'
           AND a.blockName = 'F_FORMAT_NAME'
           AND a.queryColumn = 'F_FORMAT_NAME'
           AND a.columnName = 'SPRIDEN_SURNAME_PREFIX' """),
@NamedQuery(
        name = "DisplayMaskingColumnRuleView.fetchSSBMaskByBlockNameAndColumnName",
        query = """
           FROM DisplayMaskingColumnRuleView a
           WHERE a.object = '**SSB_MASKING'
           AND a.blockName = :blockName
           AND a.columnName = :columnName """),
@NamedQuery(
        name = "DisplayMaskingColumnRuleView.fetchSSBMaskByBlockName",
        query = """
           FROM DisplayMaskingColumnRuleView a
           WHERE a.object = '**SSB_MASKING'
           AND a.blockName = :blockName """)
])

@Entity
@Table(name = "GVQ_GORDMSK")
class DisplayMaskingColumnRuleView implements Serializable {

    /**
     * Surrogate ID for GORDMSK
     */
    @Id
    @Column(name = "GORDMSK_SURROGATE_ID")
    Long id

    /**
     * Optimistic lock token for GORDMSK
     */
    @Version
    @Column(name = "GORDMSK_VERSION")
    Long version

    /**
     * DISPLAY OBJECT: Object which the display rules will be applied to.
     */
    @Column(name = "GORDMSK_OBJS_CODE")
    String object

    /**
     * BLOCK NAME: Block Name.
     */
    @Column(name = "GORDMSK_BLOCK_NAME")
    String blockName

    /**
     * COLUMN NAME: Column Name.
     */
    @Column(name = "GORDMSK_COLUMN_NAME")
    String columnName

    /**
     * COLUMN NAME: Query Column Name from GORDMCL.
     */
    @Column(name = "GORDMSK_QUERY_COLUMN")
    String queryColumn
    /**
     * SEQUENCE NUMBER: Sequence number to make a unique key.
     */
    @Column(name = "GORDMSK_SEQNO")
    Integer sequenceNumber

    /**
     * DISPLAY IND: Y/N indicator that the column should be displayed on the form.  Default value is N.
     */
    @Column(name = "GORDMSK_DISPLAY_IND")
    String displayIndicator

    /**
     * CONCEAL IND: Y/N indicator that the column should be displayed, yet the value concealed on the form.  Default value is N.
     */
    @Type(type = "yes_no")
    @Column(name = "GORDMSK_CONCEAL_IND")
    Boolean concealIndicator

    /**
     * APPLY TO ALL USERS IND: Y/N indicator that the masking applies to all users of the system.  Default value is Y.
     */

    @Column(name = "GORDMSK_ALL_USER_IND")
    String allUserIndicator

    /**
     * DATA MASK: display format for string text.  Use S value for display, and * to subsitute for the character to hide it.
     */
    @Column(name = "GORDMSK_DATA_MASK")
    String dataMask

    /**
     * FGAC USER ID: User ID the masking applies too.  Must be blank if All Users is checked on.
     */
    @Column(name = "GORDMSK_FGAC_USER_ID")
    String fineGrainedAccessControlUserId

    /**
     * Mask Direction: For partial character masking only with GORDMCL_DATA_TYPE_CDE of C.  L, R or null. Indicates the direction of the mask on a field as starting at the left(L) or right(R) most part of field being masked.
     */
    @Column(name = "GORDMSK_MASK_DIRECTION")
    String maskDirection

    /**
     * Mask Length: For partial character masking only with GORDMCL_DATATYPE_CDE of C.  Indicates the number of characters the mask will be applied to a field beginning at the left or right most part of the field.
     */
    @Column(name = "GORDMSK_MASK_LENGTH")
    Integer maskLength

    /**
     * ACTIVITY DATE: The most recent date a record was created or updated
     */
    @Column(name = "GORDMSK_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The most recent user to create or update a record.
     */
    @Column(name = "GORDMSK_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GORDMSK
     */
    @Column(name = "GORDMSK_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GORDMSK_INV_GTVFBPR_CODE
     */
    @Column(name = "GORDMSK_FBPR_CODE")
    String fgacBusinessProfile



    public static def fetchSSBNameMask() {
        def display
        DisplayMaskingColumnRuleView.withSession { session ->
            display = session.getNamedQuery('DisplayMaskingColumnRuleView.fetchSSBNameMask')
                    .uniqueResult()
        }
        return display
    }


    static def fetchSSBMaskByBlockNameAndColumnName(Map parms) {
        def display
        DisplayMaskingColumnRuleView.withSession { session ->
            display = session.getNamedQuery(
                    'DisplayMaskingColumnRuleView.fetchSSBMaskByBlockNameAndColumnName')
                    .setString('blockName', parms?.blockName).setString('columnName',parms?.columnName).uniqueResult()
        }
        if (!display)
            DisplayMaskingColumnRuleView.withSession { session ->
                display = session.getNamedQuery(
                        'DisplayMaskingColumnRuleView.fetchSSBMaskByBlockNameAndColumnName')
                        .setString('blockName', parms?.blockName + "_ALL").setString('columnName',parms?.columnName).uniqueResult()
            }
        return display
    }


    static def fetchSSBMaskByBlockName(Map parms) {
        def display
        DisplayMaskingColumnRuleView.withSession { session ->
            display = session.getNamedQuery(
                    'DisplayMaskingColumnRuleView.fetchSSBMaskByBlockName')
                    .setString('blockName', parms?.blockName).list()
        }
        if (!display)
            DisplayMaskingColumnRuleView.withSession { session ->
                display = session.getNamedQuery(
                        'DisplayMaskingColumnRuleView.fetchSSBMaskByBlockName')
                        .setString('blockName', parms?.blockName + "_ALL").list()
            }
        return display
    }


    public String toString() {
        """DisplayMaskingColumnRuleView[
   					id=$id,
   					version=$version,
   					object=$object,
   					blockName=$blockName,
   					columnName=$columnName,
                    queryColumn=$queryColumn,
   					sequenceNumber=$sequenceNumber,
   					displayIndicator=$displayIndicator,
   					concealIndicator=$concealIndicator,
   					allUserIndicator=$allUserIndicator,
                    fgacBusinessProfile=$fgacBusinessProfile,
   					dataMask=$dataMask,
   					fineGrainedAccessControlUserId=$fineGrainedAccessControlUserId,
   					maskDirection=$maskDirection,
   					maskLength=$maskLength,
   					lastModified=$lastModified,
   					lastModifiedBy=$lastModifiedBy,
   					dataOrigin=$dataOrigin]"""

    }

}