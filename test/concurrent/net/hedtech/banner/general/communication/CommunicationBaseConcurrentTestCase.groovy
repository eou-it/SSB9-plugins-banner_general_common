package net.hedtech.banner.general.communication

import grails.util.GrailsNameUtils
import groovy.sql.Sql
import net.hedtech.banner.configuration.ConfigurationUtils
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendCompositeService
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendExecutionState
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendItem
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSendRequest
import net.hedtech.banner.general.communication.job.CommunicationJob
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerConnectionSecurity
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerProperties
import net.hedtech.banner.general.communication.organization.CommunicationEmailServerPropertiesType
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccount
import net.hedtech.banner.general.communication.organization.CommunicationMailboxAccountType
import net.hedtech.banner.general.communication.organization.CommunicationOrganization
import net.hedtech.banner.general.communication.population.CommunicationPopulation
import net.hedtech.banner.general.communication.population.CommunicationPopulationCalculationStatus
import net.hedtech.banner.general.communication.population.CommunicationPopulationCompositeService
import net.hedtech.banner.general.communication.population.CommunicationPopulationVersion
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryCompositeService
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.template.CommunicationEmailTemplate
import com.icegreen.greenmail.util.*
import groovy.sql.Sql
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.security.FormContext
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken as UPAT
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

import java.util.concurrent.TimeUnit

import static org.junit.Assert.*

/**
 * A BaseIntegrationTestCase with added test support for communication artifacts.
 */
class CommunicationBaseConcurrentTestCase extends Assert {
    static transactional = false // set to false so that everything "autocommits" i.e. doesn't rollback at the end of the test

    def communicationGroupSendMonitor
    CommunicationGroupSendCompositeService communicationGroupSendCompositeService
    def communicationMailboxAccountService
    def communicationGroupSendService
    def communicationGroupSendItemService
    CommunicationPopulationQueryCompositeService communicationPopulationQueryCompositeService
    CommunicationPopulationCompositeService communicationPopulationCompositeService
    def communicationPopulationExecutionService
    def communicationPopulationSelectionListService
    def communicationFolderService
    def communicationTemplateService
    def communicationEmailTemplateService
    def communicationMobileNotificationTemplateService
    def communicationOrganizationService
    def communicationGroupSendItemProcessingEngine
    def communicationJobProcessingEngine
    def communicationRecipientDataService
    def communicationJobService
    def communicationItemService
    def communicationEmailItemService
    def communicationMobileNotificationItemService
    def communicationFieldService
    def CommunicationSendMobileNotificationService

    protected CommunicationOrganization defaultOrganization
    protected CommunicationFolder defaultFolder
    protected CommunicationEmailTemplate defaultEmailTemplate
    protected GreenMail mailServer
    protected static final int smtp_port = 4025

//    def transactional = false         // this turns off 'Grails' test framework management of transactions
//    def useTransactions = true        // and this enables our own management of transactions, which is what most tests will want
//    def exposeTransactionAwareSessionFactory = false

    def formContext = null              // This may be set within the subclass, prior to calling super.setUp(). If it isn't,
    // it will be looked up automatically.
    def bannerAuthenticationProvider    // injected
    def dataSource                      // injected via spring
    def transactionManager              // injected via spring
    def sessionFactory                  // injected via spring
    def nativeJdbcExtractor             // injected via spring
    def messageSource                   // injected via spring
    def codecLookup                     // injected via spring
    private validationTagLibInstance    // assigned lazily - see getValidationTagLib method

    def controller = null               // assigned by subclasses, e.g., within the setUp()
    def flash                           // Use this to look at the flash messages and errors
    def params                          // Use this to set params in each test: MyController.metaClass.getParams = { -> params }
    def renderMap                       // Use this to look at the rendered map: MyController.metaClass.render = { Map map -> renderMap = map }
    def redirectMap                     // Use this to look at the rendered map: MyController.metaClass.redirect = { Map map -> redirectMap = map }


    @Before
    public void setUp() {
        defaultSetUp()

        deleteAll()
        setUpDefaultOrganization()
        setUpDefaultFolder()
        setUpDefaultEmailTemplate()

        ServerSetup smtpServerSetup = new ServerSetup( smtp_port, "127.0.0.1", ServerSetup.PROTOCOL_SMTP);
        mailServer = new GreenMail( smtpServerSetup)

        CommunicationEmailServerProperties sendEmailServerProperties = defaultOrganization.theSendEmailServerProperties
        defaultOrganization.theReceiveEmailServerProperties
        String userPassword = communicationOrganizationService.decryptPassword( defaultOrganization.theSenderMailboxAccount.encryptedPassword )
        mailServer.start()
    }

    @After
    public void tearDown() {
        if (mailServer) mailServer.stop()

        deleteAll()
        defaultTearDown()
    }

    /**
     * Performs a login for the standard 'grails_user' if necessary, and calls super.setUp().
     * If you need to log in another user or ensure no user is logged in,
     * then you must either NOT call super.setUp from your setUp method
     * or you must not extend from this class (but extend from GroovyTestCase directly).
     **/
    @Before
    public void defaultSetUp() {
        params = [:]
        renderMap = [:]
        redirectMap = [:]
        flash = [:]

        if (formContext) {
            FormContext.set( formContext )
        } else if (controller) {
            // the formContext wasn't set explicitly, but we should be able to set it automatically since we know the controller
            def controllerName = controller?.class.simpleName.replaceAll( /Controller/, '' )
            Map formControllerMap = getFormControllerMap() // note: getFormControllerMap() circumvents a current grails bug
            def associatedFormsList = formControllerMap[ controllerName?.toLowerCase() ]
            formContext = associatedFormsList
            FormContext.set( associatedFormsList )
        } else {
            println "Warning: No FormContext has been set, and it cannot be set automatically without knowing the controller..."
        }

        if (controller) {
            controller.class.metaClass.getParams = { -> params }
            controller.class.metaClass.getFlash = { -> flash  }
            controller.class.metaClass.redirect = { Map args -> redirectMap = args  }
            controller.class.metaClass.render = { Map args -> renderMap = args  }
        }

        loginIfNecessary()

//        if (useTransactions) {
//            sessionFactory.currentSession.with {
//                connection().rollback()                 // needed to protect from other tests
//                clear()                                 // needed to protect from other tests
//                disconnect()                            // needed to release the old database connection
//                reconnect( dataSource.getConnection() ) // get a new connection that has unlocked the needed roles
//            }
//            transactionManager.getTransaction().setRollbackOnly()                 // and make sure we don't commit to the database
//        }
    }


    /**
     * Clears the hibernate session, but does not logout the user. If your test
     * needs to logout the user, it should do so by explicitly calling logout().
     **/
    @After
    public void defaultTearDown() {
        FormContext.clear()

//        if (useTransactions) {
//            sessionFactory.currentSession.connection().rollback()
//            sessionFactory.currentSession.close()
//        }

    }


    public void assertTrueWithRetry( Closure booleanClosure, Object arguments, long maxAttempts, int pauseBetweenAttemptsInSeconds = 10 ) {
        boolean result = false
        for (int i=0; i<maxAttempts; i++ ) {
            result = booleanClosure.call( arguments )
            if (result) {
                break
            } else {
                TimeUnit.SECONDS.sleep( pauseBetweenAttemptsInSeconds )
            }
        }
        assertTrue( result )
    }


    protected void deleteAll() {
        def sql
        try {
            sessionFactory.currentSession.with { session ->
                sql = new Sql(session.connection())
                def tx = session.beginTransaction()
                sql.executeUpdate("Delete from GCRMITM")
                sql.executeUpdate("Delete from GCREITM")
                sql.executeUpdate("Delete from GCRCITM")
                sql.executeUpdate("Delete from GCBCJOB")
                sql.executeUpdate("Delete from GCBRDAT")
                sql.executeUpdate("Delete from GCRGSIM")
                sql.executeUpdate("Delete from GCBGSND")
                sql.executeUpdate("Delete from GCBEMTL")
                sql.executeUpdate("Delete from GCBMNTL")
                sql.executeUpdate("Delete from GCBTMPL")
                sql.executeUpdate("Delete from GCRCFLD")
                sql.executeUpdate("Delete from GCRLENT")
                sql.executeUpdate("Delete from GCRPVID")
                sql.executeUpdate("Delete from GCRPOPV")
                sql.executeUpdate("Delete from GCRPQID")
                sql.executeUpdate("Delete from GCBPOPL")
                sql.executeUpdate("Delete from GCRSLIS")
                sql.executeUpdate("Delete from GCRQRYV")
                sql.executeUpdate("Delete from GCBQURY")
                sql.executeUpdate("Delete from GCRFLDR")
                sql.executeUpdate("Delete from GCRORAN")
                sql.executeUpdate("Delete from GCRMBAC")
                tx.commit()
            }
        } finally {
            sql?.close()
        }
    }


    protected void setUpDefaultOrganization() {
        List organizations = communicationOrganizationService.list()
        if (organizations.size() == 0) {
            defaultOrganization = new CommunicationOrganization(name: "Test Org")
            defaultOrganization.mobileEndPointUrl = "http://mobiledev3.ellucian.com/banner30-mobileserver/api/notification/notifications/"
            defaultOrganization.mobileApplicationName = "StudentSuccess"
            defaultOrganization.clearMobileApplicationKey = "ss-key-value"

            def cma = new CommunicationMailboxAccount(
                    emailAddress: 'rasul.shishehbor@ellucian.com',
                    encryptedPassword: communicationOrganizationService.encryptPassword( "changeit" ),
                    userName: 'rshishehbor',
                    organization: defaultOrganization,
                    type: CommunicationMailboxAccountType.Sender
            )
            defaultOrganization.senderMailboxAccountSettings = [cma]

            def cesp = new CommunicationEmailServerProperties(
                    securityProtocol: CommunicationEmailServerConnectionSecurity.None,
                    host: "127.0.0.1",
                    port: smtp_port,
                    organization: defaultOrganization,
                    type: CommunicationEmailServerPropertiesType.Send
            )
            defaultOrganization.sendEmailServerProperties = [cesp]
            defaultOrganization = communicationOrganizationService.create(defaultOrganization) as CommunicationOrganization
        } else {
            defaultOrganization = organizations.get(0) as CommunicationOrganization
        }
    }


//    private def newCommunicationEmailServerProperties( CommunicationEmailServerPropertiesType serverType, organization ) {
//        def communicationEmailServerProperties = new CommunicationEmailServerProperties(
//                // Required fields
//                securityProtocol: "TTTTTTTTTT",
//                host: "TTTTTTTTTT",
//                port: 1234,
//                organization: organization,
//                type: serverType
//        )
//        return communicationEmailServerProperties
//    }
//
//
//    private def newCommunicationMailBoxProperties( CommunicationMailboxAccountType communicationMailboxAccountType, organization ) {
//
//
//        def CommunicationMailboxAccount = new CommunicationMailboxAccount(
//                encryptedPassword: "supersecretpassword",
//                organization: organization,
//                type: communicationMailboxAccountType,
//                emailAddress: "Registrar@BannerUniversity.edu",
//                userName: "bannerRegUser" + communicationMailboxAccountType,
//                emailDisplayName: "The Office of The Registrar"
//        )
//        return CommunicationMailboxAccount
//    }
//


    protected void testDeleteGroupSend( CommunicationTemplate template ) {
        CommunicationGroupSend groupSend
        CommunicationPopulationQuery populationQuery = communicationPopulationQueryCompositeService.createPopulationQuery( newPopulationQuery("testDeleteGroupSend") )
        CommunicationPopulationQueryVersion queryVersion = communicationPopulationQueryCompositeService.publishPopulationQuery( populationQuery )

        CommunicationPopulation population = communicationPopulationCompositeService.createPopulationFromQuery( populationQuery, "testDeleteGroupSend Population" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, 'BCMADMIN' )
        def isAvailable = {
            def aPopulationVersion = CommunicationPopulationVersion.get( it )
            aPopulationVersion.refresh()
            return aPopulationVersion.status == CommunicationPopulationCalculationStatus.AVAILABLE
        }
        assertTrueWithRetry( isAvailable, populationVersion.id, 30, 10 )

        CommunicationGroupSendRequest request = new CommunicationGroupSendRequest(
                name: "testDeleteGroupSend",
                populationId: population.id,
                templateId: defaultEmailTemplate.id,
                organizationId: defaultOrganization.id,
                referenceId: UUID.randomUUID().toString(),
                recalculateOnSend: false
        )

        groupSend = communicationGroupSendCompositeService.sendAsynchronousGroupCommunication( request )
        assertNotNull(groupSend)
        assertEquals( 1, fetchGroupSendCount( groupSend.id ) )

        try {
            communicationGroupSendCompositeService.deleteGroupSend( groupSend.id )
        } catch (ApplicationException e) {
            assertEquals( "@@r1:cannotDeleteRunningGroupSend@@", e.getWrappedException().getMessage() )
        }

        sleepUntilGroupSendComplete( groupSend, 120 )
        communicationGroupSendCompositeService.deleteGroupSend( groupSend.id )

        assertEquals( 0, fetchGroupSendCount( groupSend.id ) )
        assertEquals( 0, fetchGroupSendItemCount( groupSend.id ) )
    }


    protected void setUpDefaultFolder() {
        defaultFolder = CommunicationFolder.findByName( "CommunicationGroupSendCompositeServiceTests" )
        if (!defaultFolder) {
            defaultFolder = new CommunicationFolder( name: "CommunicationGroupSendCompositeServiceTests", description: "integration test" )
            defaultFolder = communicationFolderService.create( defaultFolder )
        }
    }

    protected void setUpDefaultEmailTemplate() {
        defaultEmailTemplate = CommunicationEmailTemplate.findByName( "CommunicationGroupSendCompositeServiceTests_template" )
        if (!defaultEmailTemplate) {
            defaultEmailTemplate = new CommunicationEmailTemplate (
                    name: "CommunicationGroupSendCompositeServiceTests_template",
                    personal: false,
                    oneOff: false,
                    folder: defaultFolder,
                    toList: "test@test.edu",
                    subject: "test subject",
                    content: "test content",
            )
            defaultEmailTemplate = communicationEmailTemplateService.create( defaultEmailTemplate )
            defaultEmailTemplate = communicationEmailTemplateService.publish( defaultEmailTemplate )
        }
    }

    protected void deleteAll( service ) {
        service.findAll().each {
            service.delete( it )
        }
    }

    /**
     * Convenience method to login a user if not already logged in. You may pass in a username and password,
     * or omit and accept the default 'grails_user' and 'u_pick_it'.
     **/
    protected void loginIfNecessary( userName = "grails_user", password = "u_pick_it" ) {
        if (!SecurityContextHolder.getContext().getAuthentication()) {
            login userName, password
        }
    }


    /**
     * Convenience method to login a user. You may pass in a username and password,
     * or omit and accept the default 'grails_user' and 'u_pick_it'.
     **/
    protected void login( userName = "grails_user", password = "u_pick_it" ) {
        Authentication auth = bannerAuthenticationProvider.authenticate( new UsernamePasswordAuthenticationToken( userName, password ) )
        SecurityContextHolder.getContext().setAuthentication( auth )
    }


    /**
     * Convenience method to logout a user. This simply clears the authentication
     * object from the Spring security context holder.
     **/
    protected void logout() {
        SecurityContextHolder.getContext().setAuthentication( null )
    }


    /**
     * Convenience closure to save a domain object and log the errors if not successful.
     * Usage: save( myEntityInstance )
     **/
    protected Closure save = { domainObj ->
        try {
            assertNotNull domainObj
            domainObj.save( failOnError:true, flush: true )
        } catch (e) {
            def ae = new ApplicationException( domainObj.class, e )
            fail "Could not save $domainObj due to ${ae}"
        }
    }


    /**
     * Convenience closure to validate a domain object and log the errors if not successful.
     * Usage: validate( myEntityInstance )
     **/
    protected Closure validate = { domainObject, failOnError = true ->
        assertNotNull domainObject
        domainObject.validate()

        if (domainObject.hasErrors() && failOnError ) {
            String message = ""

            domainObject.errors.allErrors.each {
                message += "${getErrorMessage( it )}\n"
            }
            fail( message )
        }
    }


    protected executeUpdateSQL( String updateStatement, id ) {
        def sql
        try {
            sql = new Sql( sessionFactory.getCurrentSession().connection() )
            sql.executeUpdate( updateStatement, [ id ] )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
    }


    protected Date removeFractionalSecondsFrom( Date date ) {
        Calendar cal = Calendar.getInstance()
        cal.setTime( date )
        cal.set( Calendar.MILLISECOND, 0 ) // truncate fractional seconds, so that we can compare dates to those retrieved from the database
        new Date( cal.getTime().getTime() )
    }


    /**
     * Asserts that a FieldError exists for the expectedField, and that other FieldError attributes are as expected.
     * The FieldError may be asserted against the following properties.  The expectedField property.
     *
     *    [ fieldName: fieldName, errorName: errorName, modelName: modelName,
     *      partialMessage: partialMessage, exactMessage: exactMessage, rejectedValue: rejectedValue ]
     *
     * @param errors the list of FieldError objects that should contain the expected FieldError properties
     * @param expected a Map of expected field error properties, where only expectedField is required
     **/
    protected void assertFieldErrorContent( List errors, Map expected ) {

        def fieldErrors = errors.findAll { it.field == expected.fieldName }
        assertTrue "Did not find field errors for field '${expected.fieldName}'", fieldErrors instanceof List && fieldErrors.size() > 0

        if (expected.modelName) {
            assertTrue "Field errors do not have expected model name ('${expected.modelName}'), but instead error(s) have content ${fieldErrors*.model}",
                    fieldErrors.every { expected.modelName == it.model }
        }

        if (expected.rejectedValue) {
            assertTrue "Field error not found having rejected value '${expected.rejectedValue}', but instead error(s) have content ${fieldErrors*.rejectedValue}",
                    fieldErrors.any { expected.rejectedValue == it.rejectedValue }
        }

        if (expected.partialMessage) {
            assertTrue "Field error not found having partial message content '${expected.partialMessage}', but instead error(s) have content ${fieldErrors*.message}",
                    fieldErrors.any { it.message?.contains( expected.partialMessage ) }
        }

        if (expected.exactMessage) {
            assertTrue "Field error not found having exact message content '${expected.exactMessage}', but instead error(s) have content ${fieldErrors*.message}",
                    fieldErrors.any { expected.exactMessage == it.message }
        }

    }


    protected void assertErrorsFor( model, errorName, fieldList ) {
        fieldList.each { field ->
            def fieldError = model.errors.getFieldError( field )
            assertNotNull "Did not find expected '$errorName' error for ${model?.class.simpleName}.$field",
                    fieldError?.codes.find { it == "${GrailsNameUtils.getPropertyNameRepresentation( model?.class )}.${field}.${errorName}.error" }
        }
    }


    protected void assertNoErrorsFor( model, fieldList ) {
        fieldList.each {
            assertNull "Found unexpected error for ${model?.class.simpleName}.$it", model.errors.getFieldError( it )
        }
    }


    protected void assertApplicationException( ApplicationException ae, String resourceCodeOrExceptedMessage, String message = null ) {

        if (ae.sqlException) {
            if (!ae.sqlException.toString().contains( resourceCodeOrExceptedMessage )) {
                fail( "This should of returned a SQLException with a message '$resourceCodeOrExceptedMessage'." )
            }
        }
        else if (ae.wrappedException?.message) {

            def messageEvaluator
            if (ae.type == "MultiModelValidationException" ) {
                messageEvaluator = {
                    ae.wrappedException.modelValidationErrorsMaps.collect {
                        it.errors.getAllErrors().collect{ err -> err.codes }
                    }.flatten().toString().contains( resourceCodeOrExceptedMessage )
                }
            }
            else {
                // Default evaluation
                // Typically we would be more explicit, but we have gotten into the habit of doing a regex to evaluate the
                // wrapped exception with the 'resourceCode' varying from including '@@r1:' but excluding potential parameter information that
                // comes on the tail of the ApplicationException.  We are just evaluating that the message contains the code.
                messageEvaluator = {
                    ae.wrappedException.message.contains( resourceCodeOrExceptedMessage )
                }
            }

            if (messageEvaluator()) {
                // this is ok, we found the correct error message
            }
            else {
                if (message == null) {
                    message = "Did not find expected error code $resourceCodeOrExceptedMessage.  Found '${ae.wrappedException}' instead."
                }

                fail( message )
            }
        }
        else {
            throw new Exception( "Unable to assert application exception" )
        }
    }


    protected ValidationTagLib getValidationTagLib() {
        if (!validationTagLibInstance) {
            validationTagLibInstance = new ValidationTagLib()
        }
        validationTagLibInstance
    }


    protected def message = { attrs ->
        getValidationTagLib().messageImpl( attrs )
    }


    /**
     * Convience method to return a localized string based off of an error.
     **/
    protected String getErrorMessage( error ) {
        return messageSource.getMessage( error, Locale.getDefault()  )
    }


    /**
     * Convenience method to assert an expected error is found, and that it's localized message matches the supplied matchString.
     **/
    protected assertLocalizedError( model, errorName, matchString, prop ) {
        assertTrue "Did not find expected '$errorName' property error for ${model?.class?.simpleName}.$prop, but got ${model.errors.getFieldError( prop )}",
                model.errors.getFieldError( prop ).toString() ==~ /.*nullable.*/
        assertTrue "Did not find expected field error ${getErrorMessage( model.errors.getFieldError( prop ) )}",
                getErrorMessage( model.errors.getFieldError( prop ) ) ==~ matchString
    }


    /**
     * Convenience method to assert that there are no errors upon validation.  This will fail with the
     * localized message for easier debugging
     **/
    protected void assertNoErrorsUponValidation( domainObj ) {
        validate( domainObj )
    }


    protected int fetchGroupSendCount( Long groupSendId ) {
        def sql
        def result
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            result = sql.firstRow( "select count(*) as rowcount from GCBGSND where GCBGSND_SURROGATE_ID = ${groupSendId}" )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return result.rowcount
    }


    protected int fetchGroupSendItemCount( Long groupSendId ) {
        def sql
        def result
        try {
            sql = new Sql(sessionFactory.getCurrentSession().connection())
            result = sql.firstRow( "select count(*) as rowcount from GCRGSIM where GCRGSIM_GROUP_SEND_ID = ${groupSendId}" )
            println( result.rowcount )
        } finally {
            sql?.close() // note that the test will close the connection, since it's our current session's connection
        }
        return result.rowcount
    }


    protected void sleepUntilGroupSendItemsComplete( CommunicationGroupSend groupSend, long totalNumJobs, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            int countCompleted = CommunicationGroupSendItem.fetchByCompleteExecutionStateAndGroupSend( groupSend ).size()

            if ( countCompleted >= totalNumJobs) {
                break;
            }
        }
    }


    protected void sleepUntilCommunicationJobsComplete( long totalNumJobs, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            int countCompleted = CommunicationJob.fetchCompleted().size()

            if ( countCompleted >= totalNumJobs) {
                break;
            }
        }
    }


    protected void sleepUntilGroupSendComplete( CommunicationGroupSend groupSend, int maxSleepTime ) {
        final int interval = 2;                 // test every second
        int count = maxSleepTime / interval;    // calculate max loop count
        while (count > 0) {
            count--;
            TimeUnit.SECONDS.sleep( interval );

            sessionFactory.currentSession.flush()
            sessionFactory.currentSession.clear()

            groupSend = CommunicationGroupSend.get( groupSend.id )

            if ( groupSend.currentExecutionState.equals( CommunicationGroupSendExecutionState.Complete ) ) {
                break;
            }
        }

        assertEquals( CommunicationGroupSendExecutionState.Complete, groupSend.getCurrentExecutionState() )
    }


    protected def newPopulationQuery( String queryName ) {
        def populationQuery = new CommunicationPopulationQuery(
                // Required fields
                folder: defaultFolder,
                name: queryName,
                description: "test description",
                sqlString: "select spriden_pidm from spriden where rownum < 6 and spriden_change_ind is null"
        )

        return populationQuery
    }


    protected void assertLength(int length, def array) {
        assertEquals(length, array?.size());
    }


    private getFormControllerMap() {
        ConfigurationUtils.getConfiguration()?.formControllerMap
    }

}
