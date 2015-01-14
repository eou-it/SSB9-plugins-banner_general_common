package net.hedtech.banner.general.communication

import grails.gorm.DetachedCriteria
import groovy.sql.Sql
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.item.CommunicationEmailItem
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.merge.CommunicationRecipientData
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * A BaseIntegrationTestCase with added test support for communication artifacts.
 */
class CommunicationBaseIntegrationTestCase extends BaseIntegrationTestCase {

    def communicationGroupSendCommunicationService
    def communicationGroupSendService
    def communicationGroupSendItemService
    def communicationPopulationQueryService
    def communicationPopulationExecutionService
    def communicationPopulationSelectionListService
    def communicationFolderService
    def communicationTemplateService
    def communicationEmailTemplateService
    def communicationOrganizationService
    def communicationGroupSendItemProcessingEngine
    def communicationRecipientDataService
    def communicationJobService
    def communicationItemService
    def communicationEmailItemService

    protected CommunicationOrganization defaultOrganization
    protected CommunicationFolder defaultFolder
    protected CommunicationEmailTemplate defaultEmailTemplate

    @Before
    public void setUp() {
        super.setUp()

        deleteAll()
        setUpDefaultOrganization()
        setUpDefaultFolder()
        setUpDefaultEmailTemplate()
    }

    @After
    public void tearDown() {
        deleteAll()
        super.tearDown()
    }

    protected void deleteAll() {
        def sql
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            sql.executeUpdate( "Delete from GCREITM" )
            sql.executeUpdate( "Delete from GCRCITM" )
            sql.executeUpdate( "Delete from GCBCJOB" )
            sql.executeUpdate( "Delete from GCBRDAT" )
            sql.executeUpdate( "Delete from GCRGSIM" )
            sql.executeUpdate( "Delete from GCBGSND" )
            sql.executeUpdate( "Delete from GCBEMTL" )
            sql.executeUpdate( "Delete from GCBTMPL" )
            sql.executeUpdate( "Delete from GCRSLIS" )
            sql.executeUpdate( "Delete from GCBQURY" )
            sql.executeUpdate( "Delete from GCRFLDR" )
            sql.executeUpdate( "Delete from GCRORAN" )
        } finally {
            sql?.close()
        }
    }

    protected void setUpDefaultOrganization() {
        List organizations = communicationOrganizationService.list()
        if (organizations.size() == 0) {
            defaultOrganization = new CommunicationOrganization(name: "Test Org", isRoot: true)
            defaultOrganization = communicationOrganizationService.create(defaultOrganization) as CommunicationOrganization
        } else {
            defaultOrganization = organizations.get(0) as CommunicationOrganization
        }
    }

    protected void setUpDefaultFolder() {
        defaultFolder = CommunicationFolder.findByName( "CommunicationGroupSendCommunicationServiceTests" )
        if (!defaultFolder) {
            defaultFolder = new CommunicationFolder( name: "CommunicationGroupSendCommunicationServiceTests", description: "integration test" )
            defaultFolder = communicationFolderService.create( defaultFolder )
        }
    }

    protected void setUpDefaultEmailTemplate() {
        defaultEmailTemplate = CommunicationEmailTemplate.findByName( "CommunicationGroupSendCommunicationServiceTests_template" )
        if (!defaultEmailTemplate) {
            defaultEmailTemplate = new CommunicationEmailTemplate (
                    name: "CommunicationGroupSendCommunicationServiceTests_template",
                    personal: false,
                    active: true,
                    oneOff: false,
                    folder: defaultFolder,
                    toList: "test@test.edu",
                    subject: "test subject",
                    content: "test content",
            )
            defaultEmailTemplate = communicationEmailTemplateService.create( defaultEmailTemplate )
            defaultEmailTemplate = communicationTemplateService.publish( defaultEmailTemplate )
        }
    }

    protected void deleteAll( service ) {
        service.findAll().each {
            service.delete( it )
        }
    }

}
