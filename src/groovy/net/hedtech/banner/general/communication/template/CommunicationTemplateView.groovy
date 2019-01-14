/*********************************************************************************
 Copyright 2014-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*

@Entity
@Table(name = "GVQ_GCBTMPL")
@EqualsAndHashCode
@ToString
public class CommunicationTemplateView implements Serializable {
    /**
     * KEY: Generated unique key.
     */
    @Id
    @Column(name = "surrogate_id")
    Long id

    /*
     *  Name of the communication template.
    */
    @Column(name = "name")
    String name

    /**
     * Description of the communication template.
     */
    @Column(name = "description")
    String description


    /**
     *  Name of the communication template.
     */
    @Column(name = "folder_name")
    String folderName


    /**
     * Indicates if the template is both published and effective for the current date.
     */
    @Type(type = "yes_no")
    @Column(name = "active")
    Boolean active


    @Column(name = "communication_channel")
    @Enumerated(EnumType.STRING)
    CommunicationChannel communicationChannel

    /**
     * Indicates if the template is a personal template. Personal templates are available
     * only for use by the owner and are not available to other users.
     */
    @Type(type = "yes_no")
    @Column(name = "personal")
    Boolean personal

    /**
     * Indicates if the template was created through the seeded data set and should not be deleted or modified in any way.
     */
    @Type(type = "yes_no")
    @Column(name = "SYSTEM_IND")
    Boolean systemIndicator = false

    /**
     *  The user ID of the person who inserted this record.
     */
    @Column(name = "creator_id")
    String createdBy

    /**
     * VERSION: Optimistic lock token
     */
    @Version
    @Column(name = "version")
    Long version


    /**
     * Return list of templates along with count for display on list page
     * will return all shared templates and personal templates belonging to the current user
     * @param filterData
     * @param pagingAndSortParams
     * @return
     */
    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {
//commenting out ignore case for creator id as the view stores in upper case and utility returns in upper case
        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationTemplateView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            and {
                or {
                    eq("personal",false)
                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName())
                }
            }
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

    /**
     * Returns a list of templates to be used when sending messages to a population
     * will return all templates that are active, shared and personal templates belonging to the user will be returned
     * @param filterData
     * @return
     */
    public static findAllForSendByPagination(filterData, pagingAndSortParams) {
        def currentDate = new Date()
        def queryCriteria = CommunicationTemplateView.createCriteria()
        def searchName = CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name)

        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            eq("active", true)
            and {
                or {
                    eq("personal", false)
                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName())
                }
            }
            and {
                or {
                    ilike("name", searchName)
                    ilike("folderName", searchName)
                }
            }
            order(Order.asc("folderName").ignoreCase())
            order(Order.asc("name").ignoreCase())
        }
        return results
    }
}
