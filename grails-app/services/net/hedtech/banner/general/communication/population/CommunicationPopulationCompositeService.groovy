/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.util.Holders
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
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
    CommunicationPopulationQueryAssociationService communicationPopulationQueryAssociationService
    CommunicationPopulationQueryExecutionService communicationPopulationQueryExecutionService
    CommunicationPopulationSelectionListEntryService communicationPopulationSelectionListEntryService
    CommunicationPopulationSelectionListService communicationPopulationSelectionListService

    SchedulerJobService schedulerJobService
    def log = Logger.getLogger(this.getClass())


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
        log.trace( "createPopulationFromQuery called" )

        CommunicationPopulation population = new CommunicationPopulation()
        population.name = name
        population.description = description
        population.folder = populationQuery.folder
        population = communicationPopulationService.create( population )

        CommunicationPopulationQueryAssociation populationQueryAssociation = new CommunicationPopulationQueryAssociation()
        populationQueryAssociation.populationQuery = populationQuery
        populationQueryAssociation.population = population
        populationQueryAssociation = communicationPopulationQueryAssociationService.create( populationQueryAssociation )
        calculatePopulationForUser( population )
        return population
    }


    /**
     * Creates a new population which is associated with a specific published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQueryVersion( Long queryVersionId, String name, String description ) {
        log.trace( "createPopulationFromQuery called" )
        CommunicationPopulationQueryVersion queryVersion = CommunicationPopulationQueryVersion.fetchById( queryVersionId )
        return createPopulationFromQueryVersion( queryVersion, name, description )
    }


    /**
     * Creates a new population which is associated with a specific published query.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulationFromQueryVersion( CommunicationPopulationQueryVersion populationQueryVersion, String name, String description ) {
        log.trace( "createPopulationFromQueryVersion called" )
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

        calculatePopulationForUser( population )

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
        log.trace("deletePopulation called")
        CommunicationPopulation population = CommunicationPopulation.fetchById(populationId)
        List populationVersions = CommunicationPopulationVersion.findByPopulationId( population.id )
        if(populationVersions != null && populationVersions.size() > 0) {
            for(CommunicationPopulationVersion populationVersion : populationVersions) {

                List<CommunicationPopulationVersionQueryAssociation> populationVersionQueryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion(populationVersion)
                //Delete the CommunicationPopulationVersionQueryAssociation
                communicationPopulationVersionQueryAssociationService.delete(populationVersionQueryAssociations)
                //Delete the CommunicationPopulationSelectionList
                communicationPopulationSelectionListService.delete(populationVersionQueryAssociations.get(0).selectionList)
                //Delete the CommunicationPopulationSelectionListEntry
                List<CommunicationPopulationSelectionListEntry> selectionListEntries = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationVersionQueryAssociations.get(0).selectionList.id)
                communicationPopulationSelectionListEntryService.delete(selectionListEntries)
            }
            //Delete the CommunicationPopulationVersion
            communicationPopulationVersionService.delete(populationVersions)
        }

        //Delete the CommunicationPopulationQueryAssociation
        List<CommunicationPopulationQueryAssociation> populationQueryAssociations = CommunicationPopulationQueryAssociation.findAllByPopulation(population)
        communicationPopulationQueryAssociationService.delete(populationQueryAssociations)
        //Delete the CommunicationPopulation
        def populationAsMap = [
            id     : Long.valueOf( populationId ),
            version: Long.valueOf( version )
        ]
        return communicationPopulationService.delete( populationAsMap )
    }


    /**
     * Deletes the population population and/or population version.
     *
     * @param population the population
     * @param populationVersion the population version
     */
    public boolean deletePopulationVersion( CommunicationPopulation population, CommunicationPopulationVersion populationVersion ) {
        return deletePopulationVersion(population.id, population.version, populationVersion?populationVersion.id:0, populationVersion?populationVersion.version:0)
    }


    /**
     * Deletes the population and/or population version.
     *
     * @param populationId the primary key of the population
     * @param version the optimistic lock counter
     * @param versionId the primary key of the population version
     * @param revisionVersion the optimistic lock counter
     */
    public boolean deletePopulationVersion( long populationId, long version, long versionId, long revisionVersion) {
        log.trace("deletePopulationVersion called")
        boolean retValue = true
        CommunicationPopulation parentPopulation = CommunicationPopulation.fetchById(populationId)
        List populationVersions = CommunicationPopulationVersion.findByPopulationId(populationId)

        // TODO: Delete any pop version job associations with this version

//        if(!parentPopulation.changesPending)
//        {
//            //Request to delete a published version from query table where other published version exists
//            if (populationVersions != null && populationVersions.size() > 0) {
//                def queryVersionAsMap = [
//                        id     : Long.valueOf(versionId),
//                        version: Long.valueOf(revisionVersion)
//                ]
//                retValue = communicationPopulationQueryVersionService.delete(queryVersionAsMap)
//                if(populationVersions.size() == 1)
//                {
//                    //Delete the parent query as well since all versions got deleted
//                    def queryAsMap = [
//                            id     : Long.valueOf(populationId),
//                            version: Long.valueOf(version)
//                    ]
//                    retValue = communicationPopulationQueryService.delete(queryAsMap)
//                }
//                else
//                {
//                    boolean updateParentQuery = versionId == ((CommunicationPopulationQueryVersion)populationVersions[0]).id
//                    if (updateParentQuery) {
//                        //Update the parent query only if deleting the latest published version
//                        CommunicationPopulationQueryVersion latestPublishedVersion = populationVersions[1]
//                        def queryAsMap = [
//                                id            : Long.valueOf(populationId),
//                                version       : Long.valueOf(version),
//                                changesPending: Boolean.toString(false),
//                                queryString     : latestPublishedVersion.queryString
//                        ]
//                        communicationPopulationQueryService.update(queryAsMap)
//                    }
//                }
//            }
//        }
//        else
//        {
//            if (populationVersions != null && populationVersions.size() > 0) {
//                if(populationId == versionId)
//                {
//                    //Request to delete the unpublished version from query table where an older published version exists
//                    CommunicationPopulationQueryVersion latestPublishedVersion = populationVersions[0]
//                    def queryAsMap = [
//                            id             : Long.valueOf(populationId),
//                            version        : Long.valueOf(version),
//                            changesPending : Boolean.toString(false),
//                            queryString      : latestPublishedVersion.queryString
//                    ]
//                    communicationPopulationQueryService.update(queryAsMap)
//                    retValue = true
//                }
//                else
//                {
//                    //Request to delete a published version from query table where an unpublished version and other older published version exists
//                    def queryVersionAsMap = [
//                            id     : Long.valueOf(versionId),
//                            version: Long.valueOf(revisionVersion)
//                    ]
//                    retValue = communicationPopulationVersionService.delete(queryVersionAsMap)
//                }
//            }
//            else
//            {
//                //Single unpublished new parent query exists, delete from query table alone
//                def queryAsMap = [
//                        id     : Long.valueOf(populationId),
//                        version: Long.valueOf(version)
//                ]
//                retValue = communicationPopulationService.delete(queryAsMap)
//            }
//        }
        return retValue
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

//        if (!equals( query.queryString, (String) queryAsMap.queryString )) {
//            queryAsMap.changesPending = true;
//        }

//        validateSqlStringForSaving( (String) queryAsMap.queryString )

        return (CommunicationPopulation) communicationPopulationService.update( populationAsMap )
    }


    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param population the population
     */
    public CommunicationPopulationVersion calculatePopulationForUser( Long id, Long version, String oracleUserName = SecurityContextHolder.context.authentication.principal.getOracleUserName() ) {
        CommunicationPopulation population = CommunicationPopulation.fetchById( id )
        if (!population) {
            throw new ApplicationException(
                    "CommunicationPopulation",
                    population.id,
                    new NotFoundException(id: population.id, entityClassName: CommunicationPopulation.class.simpleName)
            )
        }

        if (population.version != version) {
            // TODO: throw optimistic lock exception
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
    public CommunicationPopulationVersion calculatePopulationForUser( CommunicationPopulation population, String oracleName = getCurrentUserBannerId() ) {
        log.trace( "calculatePopulationForUser called" )
        assert( population.id )
        assert( oracleName )

        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationIdAndCreatedBy( population.id, oracleName )
        if (populationVersion) {
            if (populationVersion.status.equals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION ) ) {
                // if we decide to support this capability in the future, we should unschedule the quartz job
                throw CommunicationExceptionFactory.createApplicationException( this.getClass(), "cannotRecalculatePopulationPendingExecution" )
            } else {
                //TODO: (1) Check for not able to delete population version when a job using this population version is processing

                List<CommunicationPopulationVersionQueryAssociation> populationVersionQueryAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion(populationVersion)
                //Delete the CommunicationPopulationVersionQueryAssociation
                communicationPopulationVersionQueryAssociationService.delete(populationVersionQueryAssociations)
                //Delete the CommunicationPopulationSelectionListEntry
                List<CommunicationPopulationSelectionListEntry> selectionListEntries = CommunicationPopulationSelectionListEntry.fetchBySelectionListId(populationVersionQueryAssociations.get(0).selectionList.id)
                communicationPopulationSelectionListEntryService.delete(selectionListEntries)
                //Delete the CommunicationPopulationSelectionList, currently only one exist
                communicationPopulationSelectionListService.delete(populationVersionQueryAssociations.get(0).selectionList)
                //Delete the CommunicationPopulationVersion
                communicationPopulationVersionService.delete( populationVersion )
            }
        }

        populationVersion = createPopulationVersion( population, oracleName, UUID.randomUUID().toString() )

        SchedulerJobReceipt receipt = schedulerJobService.scheduleNowServiceMethod(
            populationVersion.jobId,
            oracleName,
            populationVersion.mepCode,
            "communicationPopulationCompositeService",
            "calculatePendingPopulationVersion",
            [ "populationVersionId" : populationVersion.id ]
        )
        return populationVersion
    }


    public CommunicationPopulationVersion calculatePopulationForGroupSend( CommunicationPopulation population, String oracleName ) {
        CommunicationPopulationVersion populationVersion = createPopulationVersion(population, AsynchronousBannerAuthenticationSpoofer.monitorOracleUserName)
        calculatePendingPopulationVersion(["populationVersionId": populationVersion.id])
    }


    /**
     * This method is meant to be called from a quartz service to complete
     * the calculation request of a population version.
     * @param parameters a set of parameters passed from the original request through the quartz job detail data map
     */
    public CommunicationPopulationVersion calculatePendingPopulationVersion( Map parameters ) {
        if (log.isDebugEnabled()) {
            log.debug( "calculatePendingPopulationVersion with " + parameters )
        }

        Long populationVersionId = parameters.get( "populationVersionId" )
        CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.fetchById( populationVersionId )
        if (!populationVersion) {
            log.error( "Could not fetch communication population version with id ${populationVersionId}." )
        }
        if (!populationVersion.getStatus().equals( CommunicationPopulationCalculationStatus.PENDING_EXECUTION )) {
            throw CommunicationExceptionFactory.createApplicationException( this.class, "populationCalculationInvalidState" )
        }

        boolean errorFound = false
        long totalCount = 0
        List queryVersionAssociations = CommunicationPopulationVersionQueryAssociation.findByPopulationVersion( populationVersion )
        queryVersionAssociations.each {
            CommunicationPopulationVersionQueryAssociation queryAssociation = (CommunicationPopulationVersionQueryAssociation) it
            CommunicationPopulationQueryExecutionResult result
            try {
                result = communicationPopulationQueryExecutionService.execute( queryAssociation.populationQueryVersion.id )
                if (result.selectionListId) {
                    queryAssociation.selectionList = CommunicationPopulationSelectionList.fetchById( result.selectionListId )
                    totalCount += result.calculatedCount
                } else {
                    errorFound = true
                    queryAssociation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
                    queryAssociation.errorText = result.errorString
                }
            } catch( Throwable t ) {
                errorFound = true
                queryAssociation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
                queryAssociation.errorText = t.message
            }
            try {
                communicationPopulationVersionQueryAssociationService.update( queryAssociation )
            } catch (Throwable t) {
                t.printStackTrace()
            }
        }

        populationVersion.calculatedCount = totalCount
        populationVersion.status = errorFound ? CommunicationPopulationCalculationStatus.ERROR : CommunicationPopulationCalculationStatus.AVAILABLE
        populationVersion = communicationPopulationVersionService.update( populationVersion )
        return populationVersion
    }


//    /**
//     * Checks the syntax of the given sql statement that it validates minimal requirements of a
//     * sql query that can be used in a population query.
//     *
//     * @param populationQuerySql the sql string
//     */
//    public CommunicationPopulationQueryParseResult validateSqlStatement( String populationQuerySql ) {
//        //ensure banner security is setup for this person so they can validate and execute
//        //throw exception if the banner security for query execution is not setup for this user
//        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
//            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQuery.class, "noPermission" )
//        }
//
//        return communicationPopulationQueryStatementParseService.parse( populationQuerySql, false )
//    }

//    /**
//     * Checks the sql if it is valid.
//     * @param queryString the sql content
//     * @return a detail result describing the if the queryString is valid or not
//     * @throws ApplicationException if the queryString is so offensive as to prevent persisting
//     */
//    private CommunicationPopulationQueryParseResult validateSqlStringForSaving( String queryString ) {
//        //check for sql injection and if it returns true then throw invalid exception
//        if (CommunicationCommonUtility.sqlStatementNotAllowed( queryString, false )) {
//            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
//        }
//
//        return communicationPopulationQueryStatementParseService.parse( queryString )
//    }

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


    private CommunicationPopulationVersion createPopulationVersion(CommunicationPopulation population, String createdByOracleName, String jobId = null) {
        CommunicationPopulationVersion populationVersion
        populationVersion = new CommunicationPopulationVersion()
        populationVersion.population = population
        populationVersion.status = CommunicationPopulationCalculationStatus.PENDING_EXECUTION
        populationVersion.jobId = jobId
        populationVersion.createdBy = createdByOracleName
        populationVersion.calculatedBy = getCurrentUserBannerId()
        populationVersion = communicationPopulationVersionService.create(populationVersion)
        assert populationVersion.id

        CommunicationPopulationQueryAssociation populationQueryAssociation = fetchPopulationQueryAssociation(population)

        // peek at the query association, if the query version is explicitly set, use it, otherwise choose the latest published query version
        // for the given population query associated with this population
        CommunicationPopulationVersionQueryAssociation populationVersionQueryAssociation = new CommunicationPopulationVersionQueryAssociation()
        populationVersionQueryAssociation.populationVersion = populationVersion
        if (populationQueryAssociation.populationQueryVersion) {
            populationVersionQueryAssociation.populationQueryVersion = populationQueryAssociation.populationQueryVersion
        } else {
            List queryVersionList = CommunicationPopulationQueryVersion.findByQueryId(populationQueryAssociation.populationQuery.id)
            if (!queryVersionList || queryVersionList.size() == 0) {
                throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotFindPublishedQueryVersion")
            }
            populationVersionQueryAssociation.populationQueryVersion = (CommunicationPopulationQueryVersion) queryVersionList.get(0)
        }
        populationVersionQueryAssociation = communicationPopulationVersionQueryAssociationService.create(populationVersionQueryAssociation)
        assert populationVersionQueryAssociation.id
        return populationVersion
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
}
