/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.orm.PagedResultList
import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionResult
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryExecutionService
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryService
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersionService
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionList
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntry
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListEntryService
import net.hedtech.banner.general.communication.population.selectionlist.CommunicationPopulationSelectionListService
import net.hedtech.banner.general.scheduler.SchedulerJobReceipt
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder

/**
 * A service for driving interaction with Communication population objects and their
 * dependent objects and services.
 */
class CommunicationPopulationCompositeService {

    CommunicationPopulationQueryService communicationPopulationQueryService
    CommunicationPopulationQueryVersionService communicationPopulationQueryVersionService
    CommunicationPopulationVersionQueryAssociationService communicationPopulationVersionQueryAssociationService
    CommunicationPopulationService communicationPopulationService
    CommunicationPopulationVersionService communicationPopulationVersionService
    CommunicationPopulationCalculationService communicationPopulationCalculationService
    CommunicationPopulationQueryAssociationService communicationPopulationQueryAssociationService
    CommunicationPopulationQueryExecutionService communicationPopulationQueryExecutionService
    CommunicationPopulationSelectionListEntryService communicationPopulationSelectionListEntryService
    CommunicationPopulationSelectionListService communicationPopulationSelectionListService
    def sessionFactory
    SchedulerJobService schedulerJobService
    def log = Logger.getLogger(this.getClass())


    public PagedResultList findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {
        return CommunicationPopulationListView.findByNameWithPagingAndSortParams( filterData, pagingAndSortParams )
    }

    /**
     * Creates a new population which is associated with the latest published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQuery( Long populationQueryId, String name, String description = "" ) {
        log.trace( "createPopulationFromQuery called" )
        CommunicationPopulationQuery communicationPopulationQuery = CommunicationPopulationQuery.fetchById( populationQueryId )
        return createPopulationFromQuery( communicationPopulationQuery, name, description )
    }


    /**
     * Creates a new population which is associated with the latest published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQuery( CommunicationPopulationQuery populationQuery, String name, String description = "" ) {
        log.trace( "createPopulationFromQuery( populationQuery, name, description ) called" )

        CommunicationPopulation population = new CommunicationPopulation()
        population.name = name
        population.description = description
        population.folder = populationQuery.folder
        population = communicationPopulationService.create( population )

        CommunicationPopulationQueryAssociation populationQueryAssociation = new CommunicationPopulationQueryAssociation()
        populationQueryAssociation.populationQuery = populationQuery
        populationQueryAssociation.population = population
        populationQueryAssociation = communicationPopulationQueryAssociationService.create( populationQueryAssociation )
        if (log.isDebugEnabled()) log.debug( "Created population association with id = ${populationQueryAssociation.id}")

        createPopulationVersion( population )
        calculatePopulationForUser( population )
        return population
    }


    /**
     * Creates a new population which is associated with a specific published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQueryVersion( Long queryVersionId, String name, String description ) {
        log.trace( "createPopulationFromQueryVersion( queryVersionId, name, description ) called" )
        CommunicationPopulationQueryVersion queryVersion = CommunicationPopulationQueryVersion.fetchById( queryVersionId )
        return createPopulationFromQueryVersion( queryVersion, name, description )
    }


    /**
     * Creates a new population which is associated with a specific published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQueryVersion( CommunicationPopulationQueryVersion populationQueryVersion, String name, String description ) {
        log.trace( "createPopulationFromQueryVersion( populationVersion, name, description ) called" )
        assert(populationQueryVersion)

        CommunicationPopulation population = new CommunicationPopulation()
        population.name = name
        population.description = description
        population.folder = populationQueryVersion.query.folder
        population = communicationPopulationService.create( population )

        CommunicationPopulationQueryAssociation populationQueryAssociation = new CommunicationPopulationQueryAssociation()
        populationQueryAssociation.populationQuery = populationQueryVersion.query
        populationQueryAssociation.populationQueryVersion = populationQueryVersion
        populationQueryAssociation.population = population
        populationQueryAssociation = communicationPopulationQueryAssociationService.create(populationQueryAssociation)
        if (log.isDebugEnabled()) log.debug( "Created population association with id = ${populationQueryAssociation.id}")

        CommunicationPopulationVersion populationVersion = createPopulationVersion( population )
        calculatePopulationVersionForUser( populationVersion )
        return population
    }

    /**
     * Retrieves a population.
     *
     * @param id the primary key of the population
     */
    public CommunicationPopulation fetchPopulation( long id ) {
        return communicationPopulationService.get( Long.valueOf( id ) )
    }

    /**
     * Delete the population calculation marked by the given calculation id.
     * @param populationCalculationId the long id of the population calculation
     * @return true if successful
     */
    public boolean deletePopulationCalculation( Long populationCalculationId ) {
        deletePopulationCalculation( CommunicationPopulationCalculation.get( populationCalculationId ) )
    }

    /**
     * Delete the population calculation marked by the given calculation id.
     * @param calculation the population calculation
     * @return true if successful
     */
    public boolean deletePopulationCalculation( CommunicationPopulationCalculation calculation ) {
        return calculation
    }

    /**
     * Deletes the population along with any dependent population versions.
     *
     * @param populationId the primary key of the population
     * @param version the optimistic lock counter
     */
    public boolean deletePopulation( CommunicationPopulation population ) {
        return deletePopulation(population.id, population.version)
    }


    /**
     * Deletes the population version along with any dependent population versions.
     *
     * @param populationId the primary key of the population population
     * @param version the optimistic lock counter
     */
    public boolean deletePopulation( long populationId, long version ) {
        log.trace("deletePopulation( populationId, version ) called")
        CommunicationPopulation population = CommunicationPopulation.fetchById(populationId)
        List populationVersions = CommunicationPopulationVersion.findByPopulationId( population.id )
        populationVersions.each { CommunicationPopulationVersion populationVersion ->
            populationVersion.refresh()
            deletePopulationVersion( populationVersion )
        }

        //Delete the CommunicationPopulationQueryAssociation
        List<CommunicationPopulationQueryAssociation> populationQueryAssociations = CommunicationPopulationQueryAssociation.findAllByPopulation( population )
        populationQueryAssociations.each { CommunicationPopulationQueryAssociation populationQueryAssociation ->
            populationQueryAssociation.refresh()
            communicationPopulationQueryAssociationService.delete( populationQueryAssociation )
        }

        return communicationPopulationService.delete( [ id : Long.valueOf( populationId ), version: Long.valueOf( version ) ] )
    }


    /**
     * Updates an existing population.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation updatePopulation( Map populationAsMap) {
        log.trace( "updatePopulation called" )
        assert( populationAsMap.id != null )
        assert( populationAsMap.version != null ) // optimistic lock counter

        CommunicationPopulation population = CommunicationPopulation.fetchById( populationAsMap.id )
        if (!population) {
            throw new ApplicationException(
                "CommunicationPopulation",
                populationAsMap.id,
                new NotFoundException(id: populationAsMap.id, entityClassName: CommunicationPopulation.class.simpleName)
            )
        }

        return (CommunicationPopulation) communicationPopulationService.update( populationAsMap )
    }


    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param population the population
     */
    public CommunicationPopulationCalculation calculatePopulationForUser( Long id, Long version, String oracleUserName = SecurityContextHolder.context.authentication.principal.getOracleUserName() ) {
        CommunicationPopulation population = CommunicationPopulation.fetchById( id )
        if (!population) {
            throw new ApplicationException(
                    "CommunicationPopulation",
                    population.id,
                    new NotFoundException(id: population.id, entityClassName: CommunicationPopulation.class.simpleName)
            )
        }

        return calculatePopulationForUser( population, oracleUserName )
    }


    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param id the primary key of the population query
     * @param version the optimistic lock counter
     */
    public CommunicationPopulationCalculation calculatePopulationForUser( CommunicationPopulation population, String oracleName = getCurrentUserBannerId() ) {
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationId( population.id )
        return calculatePopulationVersionForUser( populationVersion, oracleName )
    }

    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param id the primary key of the population query
     * @param version the optimistic lock counter
     */
    public CommunicationPopulationCalculation calculatePopulationVersionForUser( CommunicationPopulationVersion populationVersion, String oracleName = getCurrentUserBannerId() ) {
        log.trace( "calculatePopulationVersionForUser( population ) called" )
        try {
            assert( populationVersion.id != null )
            assert( oracleName )

            removeObsoleteCalculationIfNecessaryForUser( populationVersion, oracleName )
            CommunicationPopulationCalculation populationCalculation = createNewCalculation( populationVersion, oracleName, true )

            SchedulerJobReceipt receipt = schedulerJobService.scheduleNowServiceMethod(
                populationCalculation.jobId,
                oracleName,
                populationCalculation.mepCode,
                "communicationPopulationCompositeService",
                "processPendingPopulationCalculation",
                [ "populationCalculationId" : populationCalculation.id ]
            )
            return populationCalculation
        } finally {
            log.trace( "exiting calculatePopulationVersionForUser( population )")
        }
    }

    public CommunicationPopulationCalculation calculatePopulationVersionForGroupSend( CommunicationPopulationVersion populationVersion) {
        assert( populationVersion )
        CommunicationPopulationCalculation populationCalculation = createNewCalculation( populationVersion, AsynchronousBannerAuthenticationSpoofer.monitorOracleUserName, false )
        return processPendingPopulationCalculation(["populationCalculationId": populationCalculation.id])
    }

    public void deletePopulationVersion( CommunicationPopulationVersion populationVersion ) {
        assert populationVersion != null
        if (log.isTraceEnabled()) {
            log.trace( "deletePopulationVersion( populationVersion = ${populationVersion.id} ) called")
        }

        List calculations = CommunicationPopulationCalculation.findByPopulationVersionId( populationVersion.id )
        calculations.each { CommunicationPopulationCalculation calculation ->
            calculation.refresh()
            CommunicationPopulationSelectionList selectionList = calculation.selectionList
            if (calculation.status == CommunicationPopulationCalculationStatus.PENDING_EXECUTION) {
                schedulerJobService.deleteScheduledJob( calculation.jobId, "communicationPopulationCompositeService", "processPendingPopulationCalculation" )
            }
            try {
                communicationPopulationCalculationService.delete( calculation )
            } catch( ApplicationException e ) {
                if (e.getWrappedException().getCause().getConstraintName().equals( "GENERAL.FK1_GCBGSND_INV_GCRPOPC" )) {
                    throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotDeletePopulationWithExistingGroupSends")
                } else {
                    throw e
                }
            }

            if (selectionList) {
                deleteSelectionListEntries( selectionList )
                communicationPopulationSelectionListService.delete( selectionList )
            }
        }

        //Delete the CommunicationPopulationVersionQueryAssociation
        List<CommunicationPopulationVersionQueryAssociation> populationVersionQueryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion(populationVersion)
        populationVersionQueryAssociations.each { CommunicationPopulationVersionQueryAssociation populationVersionQueryAssociation ->
            populationVersionQueryAssociation.refresh()
            if (log.isDebugEnabled()) log.debug( "Deleting population version query association with id = ${populationVersionQueryAssociation.id}")
            communicationPopulationVersionQueryAssociationService.delete(populationVersionQueryAssociation)
        }

        communicationPopulationVersionService.delete( populationVersion )
    }


    /**
     * This method is meant to be called from a quartz service to complete
     * the calculation request of a population version.
     * @param parameters a set of parameters passed from the original request through the quartz job detail data map
     */
    public CommunicationPopulationCalculation processPendingPopulationCalculation( Map parameters ) {
        if (log.isDebugEnabled()) {
            log.debug( "calculatePendingPopulationVersion with " + parameters )
        }

        Long populationCalculationId = parameters.get( "populationCalculationId" )
        CommunicationPopulationCalculation calculation = CommunicationPopulationCalculation.fetchById( populationCalculationId )
        if (!calculation) {
            // Calculation may have been deleted in which case silently return
            return null
        }

        if (!calculation.getStatus().equals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION )) {
            throw CommunicationExceptionFactory.createApplicationException( this.class, "populationCalculationInvalidState" )
        }

        boolean errorFound = false
        long totalCount = 0
        CommunicationPopulationQueryExecutionResult result
        try {
            result = communicationPopulationQueryExecutionService.execute( calculation.populationQueryVersion.id )
            if (result.selectionListId) {
                calculation.selectionList = CommunicationPopulationSelectionList.fetchById( result.selectionListId )
                totalCount += result.calculatedCount
            } else {
                errorFound = true
                calculation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
                calculation.errorText = result.errorString
            }
        } catch( Throwable t ) {
            errorFound = true
            calculation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            calculation.errorText = t.message
        }

        calculation.calculatedCount = totalCount
        calculation.status = errorFound ? CommunicationPopulationCalculationStatus.ERROR : CommunicationPopulationCalculationStatus.AVAILABLE
        calculation.jobId = null
        calculation.save( failOnError: true, flush: true )
        return calculation
    }

    /**
     * Checks two strings for equality regardless if either member is null.
     */
    private static boolean equals( String s1, String s2 ) {
        return s1 == null ? s2 == null : s1 == s2
    }


    private CommunicationPopulationQueryAssociation fetchPopulationQueryAssociation(CommunicationPopulation population) {
        List queryAssociationList = CommunicationPopulationQueryAssociation.findAllByPopulation(population)
        if (!queryAssociationList || queryAssociationList.size() == 0) {
            throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotFindAssociatedQueryForPopulation")
        }
        CommunicationPopulationQueryAssociation populationQueryAssociation = queryAssociationList.get(0)
        populationQueryAssociation
    }

    /**
     * Creates a read only copy of the current population. This should be done at least once prior to a calculation as
     * calculations are based on a static evaluation of the population. This model allows for changes in the population
     * where calculations can co-exists with different versions of the population.
     * @param population
     * @return
     */
    private CommunicationPopulationVersion createPopulationVersion(CommunicationPopulation population) {
        log.trace( "createPopulationVersion( population ) called" )
        try {
            CommunicationPopulationVersion populationVersion = population.createVersion()
            populationVersion = (CommunicationPopulationVersion) communicationPopulationVersionService.create( populationVersion )
            assert populationVersion.id
            if (log.isDebugEnabled()) log.debug( "population version with id = ${populationVersion.id} created." )

            CommunicationPopulationQueryAssociation populationQueryAssociation = fetchPopulationQueryAssociation( population )
            CommunicationPopulationVersionQueryAssociation populationVersionQueryAssociation = populationQueryAssociation.createVersion( populationVersion )
            populationVersionQueryAssociation =
                (CommunicationPopulationVersionQueryAssociation) communicationPopulationVersionQueryAssociationService.create( populationVersionQueryAssociation )
            assert populationVersionQueryAssociation.id
            if (log.isDebugEnabled()) log.debug( "population version query association with id = ${populationVersionQueryAssociation.id} created." )

            return populationVersion
        } finally {
            log.trace( "createPopulationVersion( population ) exited" )
        }
    }

    /**
     * Returns the banner id of the current session in uppercase.
     */
    private String getCurrentUserBannerId() {
        def creatorId = SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName()
        if (creatorId == null) {
            def config = Holders.config
            creatorId = config?.bannerSsbDataSource?.username
        }
        return creatorId.toUpperCase()
    }

    private void deleteSelectionListEntries(CommunicationPopulationSelectionList selectionList) {
        CommunicationPopulationSelectionListEntry.executeUpdate(
                "delete CommunicationPopulationSelectionListEntry where populationSelectionList = :selectionList",
                [selectionList: selectionList]
        )
    }

    private CommunicationPopulationQueryVersion getQueryVersionForNewCalculation(CommunicationPopulationVersion populationVersion) {
        List queryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        CommunicationPopulationVersionQueryAssociation populationVersionQueryAssociation = (CommunicationPopulationVersionQueryAssociation) queryAssociations.get( 0 )

        if (!populationVersionQueryAssociation.shouldRequestLatestQueryVersion()) {
            return populationVersionQueryAssociation.populationQueryVersion
        } else {
            List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId( populationVersionQueryAssociation.populationQuery.id )
            if (!queryVersionList || queryVersionList.size() == 0) {
                throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotFindPublishedQueryVersion")
            }
            return (CommunicationPopulationQueryVersion) queryVersionList.get(0)
        }
    }

    private void removeObsoleteCalculationIfNecessaryForUser(CommunicationPopulationVersion populationVersion, String oracleName) {
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationVersionIdAndCreatedBy(populationVersion.id, oracleName)
        if (populationCalculation) {
            if (populationCalculation.status.equals(CommunicationPopulationCalculationStatus.PENDING_EXECUTION)) {
                throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotRecalculatePopulationPendingExecution")
            } else {
                if (CommunicationGroupSend.findCountByPopulationCalculationId(populationCalculation.id) == 0) {
                    deletePopulationCalculation(populationCalculation)
                }
            }
        }
    }

    private CommunicationPopulationCalculation createNewCalculation( CommunicationPopulationVersion populationVersion, String calculatedBy, boolean userRequested ) {
        CommunicationPopulationCalculation populationCalculation = new CommunicationPopulationCalculation()
        populationCalculation.userRequested = userRequested
        populationCalculation.populationVersion = populationVersion
        populationCalculation.populationQueryVersion = getQueryVersionForNewCalculation(populationVersion)
        populationCalculation.calculatedBy = calculatedBy
        populationCalculation.status = CommunicationPopulationCalculationStatus.PENDING_EXECUTION
        populationCalculation.jobId = UUID.randomUUID().toString()
        populationCalculation = communicationPopulationCalculationService.create(populationCalculation)
        return populationCalculation
    }
}
