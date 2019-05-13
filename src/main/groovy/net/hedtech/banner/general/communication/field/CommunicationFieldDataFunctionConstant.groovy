/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.field

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.*

/**
 * rule Constant Table. A set of constants that may be used as rule arguments.
 */
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "GCBRCON")
// @NamedQueries(value = [
// @NamedQuery(name = "RuleConstantASetOfConstantsThatMayBeUsedAsRuleArguments.fetchByxxxxx",
//             query = """ FROM RuleConstantASetOfConstantsThatMayBeUsedAsRuleArguments a WHERE xxxxx """)
// ])
class CommunicationFieldDataFunctionConstant {
    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GCBRCON_SURROGATE_ID")
    @SequenceGenerator(name = "GCBRCON_SEQ_GEN", allocationSize = 1, sequenceName = "GCBRCON_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCBRCON_SEQ_GEN")
    Long id
    /**
     * DESCRIPTION: Description of the rule constant.
     */
    @Column(name = "GCBRCON_DESCRIPTION")
    String description
    /**
     * NAME: Name of the rule constant.
     */
    @Column(name = "GCBRCON_NAME")
    String name
    /**
     * SYSTEM REQUIRED IND: Indicates if the rule constant is required for proper functioning of the system (1=Yes or 0=No). If it is required by the system, special permission is required to modify or delete the record.
     */

    @Type(type = "yes_no")
    @Column(name = "GCBRCON_SYSTEM_REQ_IND")
    Boolean systemReqIndicator
    /**
     * VALUE: Value of the constant. All constants are primitives (Boolean, String, Number, or Date) such that they can be instantiated from their string equivalent. The value of a constant may also be null. The value is dynamically converted from a string to the appropriate data type of the argument referencing the constant.
     */
    @Column(name = "GCBRCON_VALUE")
    String value
    /**
     * VALUEDATEFORMAT: Required formatting information for a constant value that is a Date.
     */
    @Column(name = "GCBRCON_VALUEDATEFORMAT")
    String valueDateFormat
    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCBRCON_VERSION")
    Long version
    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCBRCON_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified
    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCBRCON_USER_ID")
    String lastModifiedBy
    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCBRCON_DATA_ORIGIN")
    String dataOrigin


    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        description(nullable: true, maxSize: 4000)
        name(nullable: false, maxSize: 1020)
        systemReqIndicator(nullable: false)
        value(nullable: false, maxSize: 1020)
        valueDateFormat(nullable: true, maxSize: 1020)
    }
    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


}
