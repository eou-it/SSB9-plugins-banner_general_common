/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.apache.log4j.Logger
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * Represents a Directory Profile item
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
@Entity
@Table(name = "GVQ_GOBDIRO")
@NamedQueries(value = [
        @NamedQuery(name = "DirectoryProfileView.fetchAllOrderBySeqNo",
                query = """FROM DirectoryProfileView a
    ORDER BY a.sequenceNumber
""")
])
class DirectoryProfileView {
    static def log = Logger.getLogger('net.hedtech.banner.general.person.view.DirectoryProfileView')

    /**
     * Surrogate ID for GOBDIRO
     */
    @Id
    @Column(name = "GOBDIRO_SURROGATE_ID")
    @SequenceGenerator(name = "GOBDIRO_SEQ_GEN", allocationSize = 1, sequenceName = "GOBDIRO_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBDIRO_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GOBDIRO
     */
    @Version
    @Column(name = "GOBDIRO_VERSION")
    Long version

    /**
     * The Directory Profile item code
     */
    @Column(name = "GOBDIRO_DIRO_CODE")
    String code

    /**
     * The Directory Profile directory type
     */
    @Column(name = "GOBDIRO_DIRECTORY_TYPE")
    String directoryType

    /**
     * The Directory Profile item type
     */
    @Column(name = "GOBDIRO_ITEM_TYPE")
    String itemType

    /**
     * The Directory Profile display profile indicator
     */
    @Type(type = "yes_no")
    @Column(name = "GOBDIRO_DISP_PROFILE_IND")
    Boolean displayProfileIndicator

    /**
     * The Directory Profile update profile indicator
     */
    @Type(type = "yes_no")
    @Column(name = "GOBDIRO_UPD_PROFILE_IND")
    Boolean updateProfileIndicator

    /**
     * The Directory Profile default indicator
     * Indicates if the item should be defaulted TRUE if the user profile does
     * not currently have a definition for that item.
     */
    @Type(type = "yes_no")
    @Column(name = "GOBDIRO_NON_PROFILE_DEF_IND")
    Boolean nonProfileDefaultIndicator

    /**
     * The Directory Profile System Required Indicator.
     */
    @Type(type = "yes_no")
    @Column(name = "GOBDIRO_SYSTEM_REQ_IND")
    Boolean systemRequiredIndicator

    /**
     * The Directory Profile system sequence number
     */
    @Column(name = "GOBDIRO_SEQ_NO")
    Integer sequenceNumber



    public static List fetchAll() {
        def directoryProfileItems

        DirectoryProfileView.withSession { session ->
            directoryProfileItems = session.getNamedQuery('DirectoryProfileView.fetchAllOrderBySeqNo').list()
        }

        return directoryProfileItems
    }

}
