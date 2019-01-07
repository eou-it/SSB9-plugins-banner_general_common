/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.aip

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

@NamedQueries(value = [
        @NamedQuery(name = "UserActiveActionItem.checkIfUserActionItemsPresentByPidmAndCurrentDate",
                query = """select count (a.id) FROM UserActiveActionItem a
                            WHERE a.pidm = :pidm
                             AND trunc(sysdate) BETWEEN trunc(sysdate) AND trunc(a.displayEndDate)
                            """)
])

/**
 * Active and pending action items assigned to a person.
 */

@Entity
@Table(name = "GVQ_GCRACPN")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
public class UserActiveActionItem implements Serializable {
    @Id
    @Column(name = "GCRAACT_GCBACTM_ID")
    Long id

    /**
     * PIDM of the user action item belongs to
     */
    @Column(name = "GCRAACT_PIDM")
    Long pidm

    /**
     * Display Start Date
     */
    @Column(name = "GCRAACT_DISPLAY_START_DATE")
    Date displayStartDate

    /**
     * Display End Date
     */
    @Column(name = "GCRAACT_DISPLAY_END_DATE")
    Date displayEndDate

    /**
     * Check if Lists of user specific action items present
     * @param pidm
     * @return
     */
   static def checkIfActionItemPresent(Integer pidm) {

       def result = UserActiveActionItem.withSession { session ->
           session.getNamedQuery('UserActiveActionItem.checkIfUserActionItemsPresentByPidmAndCurrentDate')
                   .setInteger('pidm', pidm)
                   .uniqueResult() >0
       }
       return result
   }


}