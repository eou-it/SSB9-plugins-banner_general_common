/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.letter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel

import javax.persistence.*

/**
 * Communication Letter Item View. Denotes a communication that has been sent to a recipient.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCRLETM")
@NamedQueries(value = [
    @NamedQuery( name = "CommunicationLetterItemView.fetchByGroupSendItemId",
            query = """ FROM CommunicationLetterItemView view
                WHERE view.groupSendItemId = :groupSendItemId """
    )
])
class CommunicationLetterItemView implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "SURROGATE_ID")
    Long id

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Column(name = "GROUP_SEND_ID")
    Long groupSendId

    @Column(name = "GROUP_SEND_ITEM_ID")
    Long groupSendItemId

    /**
     * Username of the person who initiated the communication.
     */
    @Column(name = "initiated_by")
    String name

    /**
     * The date the communication item was created in the system.
     */
    @Column(name = "create_date")
    Date createDate

    /**
     * The pidm of the recipient
     */
    @Column(name = "pidm")
    Long pidm

    /**
     * The Banner ID of the recipient
     */
    @Column(name = "banner_id")
    String bannerId

    /**
     * The first name of the recipient
     */
    @Column(name = "first_name")
    String firstName

    /**
     * The last name of the recipient
     */
    @Column(name = "last_name")
    String lastName

    /**
     * The middle name of the recipient
     */
    @Column(name = "middle_name")
    String middleName

    /**
     * The date the communication item was actually sent.
     */
    @Column(name = "sent_date")
    Date sentDate

    /**
     * The name of the template send in the communication
     */
    @Column(name = "template_id")
    Long templateId

    /**
     * The name of the organization on whose behalf the communication was sent
     */
    @Column(name = "organization_id")
    Long organizationId

    @Column(name = "TO_ADDRESS")
    String toAddress

    @Column(name = "LETTER_CONTENT")
    String content

    /**
     * Email, Letter, etc
     */
    @Column(name = "communication_channel")
    CommunicationChannel communicationChannel

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version


    public static int fetchCountByGroupSendId( Long groupSendId ) {
        return CommunicationLetterItemView.createCriteria().list {
            projections {
                count()
            }
            eq( 'groupSendId', groupSendId )
        }[0]
    }

    public static CommunicationLetterItemView fetchByGroupSendItemId(Long groupSendItemId) {
        def results = null
        CommunicationLetterItemView.withSession { session ->
            results = session.getNamedQuery('CommunicationLetterItemView.fetchByGroupSendItemId')
                .setLong('groupSendItemId', groupSendItemId).list()
        }
        if (results == null || results.size() == 0) {
            return null
        } else {
            return results[0]
        }
    }

}
