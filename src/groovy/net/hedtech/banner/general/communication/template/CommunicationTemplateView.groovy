/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.item.CommunicationChannel
import org.hibernate.annotations.Type
import org.hibernate.criterion.Order

import javax.persistence.*
/*
CREATE OR REPLACE FORCE VIEW gvq_gcbtmpl
(
   surrogate_id
  ,name
  ,description
  ,folder_name
  ,active
  ,communication_channel
)
 */
@Entity
@Table(name = "GVQ_GCBTMPL")
@EqualsAndHashCode
@ToString
//@NamedQueries(value = [
//        @NamedQuery(name = "CommunicationTemplateView.fetchByTemplateNameAndFolderName",
//                query = """ FROM CommunicationTemplate a
//                    WHERE a.folder.name = :folderName
//                      AND upper(a.name) = upper(:templateName)"""),
//        @NamedQuery(name = "CommunicationTemplate.existsAnotherNameFolder",
//                query = """ FROM CommunicationTemplate a
//                    WHERE a.folder.name = :folderName
//                    AND   upper(a.name) = upper(:templateName)
//                    AND   a.id <> :id""")
//])
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


//    /******************* Named Queries *******************/
//
//    public static CommunicationTemplate fetchByTemplateNameAndFolderName(String templateName, String folderName) {
//
//        def query
//        CommunicationTemplate.withSession { session ->
//            query = session.getNamedQuery('CommunicationTemplate.fetchByTemplateNameAndFolderName')
//                    .setString('folderName', folderName)
//                    .setString('templateName', templateName)
//                    .list()[0]
//        }
//        return query
//    }
//
//    public static Boolean existsAnotherNameFolder(Long templateId, String templateName, String folderName) {
//
//        def query
//        CommunicationTemplate.withSession { session ->
//            query = session.getNamedQuery('CommunicationTemplate.existsAnotherNameFolder')
//                    .setString('folderName', folderName)
//                    .setString('templateName', templateName)
//                    .setLong('id', templateId).list()[0]
//
//        }
//        return (query != null)
//    }

    /**
     * Return list of templates along with count for display on list page
     * will return all shared templates and personal templates belonging to the current user
     * @param filterData
     * @param pagingAndSortParams
     * @return
     */
    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {

        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationTemplateView.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            and {
                or {
                    eq("personal",false)
                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName().toLowerCase(),[ignoreCase: true])
                }
            }
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }

//    /**
//     * Returns a list of templates to be used when sending messages to a population
//     * will return all templates that are active, shared and personal templates belonging to the user will be returned
//     * @param filterData
//     * @return
//     */
//    public static findByFolderForSend(filterData) {
//
//        def currentDate = new Date()
//        def queryCriteria = CommunicationTemplate.createCriteria()
//
//        def results = queryCriteria.list {
//            folder {
//                eq("name", filterData?.params?.folderName?.toLowerCase(), [ignoreCase: true])
//            }
//            eq("published",true)
//            and {
//                or {
//                    eq("personal",false)
//                    eq("createdBy", CommunicationCommonUtility.getUserOracleUserName().toLowerCase(),[ignoreCase: true])
//                }
//            }
//            le("validFrom",currentDate)
//            and {
//                or {
//                    isNull("validTo")
//                    ge("validTo",currentDate)
//                }
//            }
//            order( Order.asc("name").ignoreCase())
//        }
//        return results
//    }
}
