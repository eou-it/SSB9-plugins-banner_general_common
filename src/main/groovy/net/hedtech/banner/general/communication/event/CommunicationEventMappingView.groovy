/*******************************************************************************
 Copyright 2018 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.event

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryType
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GVQ_GCBEVMP")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationEventMappingView.fetchByName",
                query = """ FROM CommunicationEventMappingView a
                    WHERE lower(a.eventName) = lower(:eventName)""")
])
class CommunicationEventMappingView implements Serializable {

    @Id
    @Column(name = "SURROGATE_ID")
    Long surrogateId

    @Column(name = "EVENT_NAME")
    String eventName

    @Column(name = "ORGANIZATION_ID")
    Long organizationId

    @Column(name = "ORGANIZATION_NAME")
    String organizationName

    @Type(type = "yes_no")
    @Column(name = "ORGANIZATION_AVAILABLE_IND")
    Boolean organizationAvailableIndicator

    @Column(name = "TEMPLATE_ID")
    Long templateId

    @Column(name = "TEMPLATE_NAME")
    String templateName

    @Column(name = "COMMUNICATION_CHANNEL")
    @Enumerated(value = EnumType.STRING)
    CommunicationChannel communicationChannel

    @Type(type = "yes_no")
    @Column(name = "PUBLISH_IND")
    Boolean publishInd

    @Type(type = "yes_no")
    @Column(name = "TEMPLATE_ACTIVE_IND")
    Boolean templateActiveInd

    @Column(name = "TEMPLATE_FOLDER_ID")
    Long templateFolderId

    @Column(name = "QUERY_ID")
    Long queryId

    @Column(name = "QUERY_NAME")
    String queryName

    @Column(name = "QUERY_TYPE")
    CommunicationPopulationQueryType queryType

    @Type(type = "yes_no")
    @Column(name = "EVENT_ACTIVE_IND")
    Boolean isActive

    @Type(type = "yes_no")
    @Column(name = "SYSTEM_IND")
    Boolean systemIndicator

    @Column(name = "VERSION")
    Long version

    @Column(name = "VPDI_CODE")
    String vpdiCode

    static constraints = {
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']


    public static CommunicationEventMappingView fetchByName(String eventName) {

        def query
        CommunicationEventMappingView.withSession { session ->
            query = session.getNamedQuery('CommunicationEventMappingView.fetchByName')
                    .setString('eventName', eventName)
                    .list()[0]
        }
        return query
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def ascdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'asc'

        def queryCriteria = CommunicationEventMappingView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("eventName", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.eventName))
            order((ascdir ? Order.asc(pagingAndSortParams?.sortColumn) : Order.desc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    public boolean availableForUse() {

        if(!this.isActive && !this.templateId && !this.publishInd && !this.templateActiveInd && !this.organizationId && !this.organizationAvailableIndicator) {
            return false;
        }

        CommunicationOrganization rootOrganization = CommunicationOrganization.fetchRoot()
        CommunicationOrganization organization = CommunicationOrganization.get(this.organizationId)
        if ((this.communicationChannel == CommunicationChannel.EMAIL) &&
                !((organization?.senderMailboxAccount && organization?.replyToMailboxAccount) &&
                        (organization?.sendEmailServerProperties || rootOrganization?.sendEmailServerProperties))) {
            return false;
        }

        return true;
    }
}
