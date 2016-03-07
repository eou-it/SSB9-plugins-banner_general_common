/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.log4j.Logger

/**
 * A service for driving interaction with Communication population objects and their
 * dependent objects and services.
 */
class CommunicationPopulationCompositeService {

    CommunicationPopulationService communicationPopulationService
    CommunicationPopulationVersionService communicationPopulationVersionService
    CommunicationPopulationQueryAssociationService communicationPopulationQueryAssociationService
    SchedulerJobService schedulerJobService
    def log = Logger.getLogger(this.getClass())


    /**
     * Creates a new population.
     *
     * @param population the population to persist
     */
    public CommunicationPopulation createPopulation( CommunicationPopulation population, CommunicationPopulationQueryAssociation populationQueryAssociation ) {
        log.trace( "createPopulation called" )
        assert( population.id == null )
        assert(populationQueryAssociation.id == null)

        CommunicationPopulation population1 = (CommunicationPopulation) communicationPopulationService.create( population )
        populationQueryAssociation.population = population1
        CommunicationPopulationQueryAssociation popQryAssociation = (CommunicationPopulationQueryAssociation) communicationPopulationQueryAssociationService.create( populationQueryAssociation )

        return popQryAssociation
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
        List populationVersions = CommunicationPopulationVersion.findByPopulationId( populationId )
        if(populationVersions != null && populationVersions.size() > 0) {
            communicationPopulationVersionService.delete(populationVersions)
        }

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
        // communicationPopulationVersionJobAssociationService.delete any job with populationVersionJobAssociation


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
//                                sqlString     : latestPublishedVersion.sqlString
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
//                            sqlString      : latestPublishedVersion.sqlString
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

//        if (!equals( query.sqlString, (String) queryAsMap.sqlString )) {
//            queryAsMap.changesPending = true;
//        }

//        validateSqlStringForSaving( (String) queryAsMap.sqlString )

        return (CommunicationPopulation) communicationPopulationService.update( populationAsMap )
    }


    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param population the population
     */
    public CommunicationPopulation calculatePopulation( CommunicationPopulation population ) {
        return calculatePopulation( population.id, population.version )
    }


    /**
     * Calculates the population by creating a read only copy of the intrinsic properties of the population
     * into a population version table and executing the queries to produce a resultant set of person id's (pidm's).
     *
     * @param id the primary key of the population query
     * @param version the optimistic lock counter
     */
    public CommunicationPopulation calculatePopulation( Long id, Long version ) {
        log.trace( "publishPopulation called" )
        assert( id != null )
        assert( version != null ) // optimistic lock counter

        CommunicationPopulation population = CommunicationPopulation.fetchById( id )
        if (!population) {
            throw new ApplicationException(
                    "CommunicationPopulation",
                    population.id,
                    new NotFoundException(id: population.id, entityClassName: CommunicationPopulation.class.simpleName)
            )
        }

        CommunicationPopulationVersion populationVersion = new CommunicationPopulationVersion()
        populationVersion.population = population
        populationVersion.status = CommunicationPopulationCalculationStatus.PENDING_EXECUTION
        String jobId = UUID.randomUUID().toString()
        //populationVersion.jobId = jobId
        communicationPopulationVersionService.create( [ domainModel: populationVersion ] )

        // TODO: schedule quartz job call method to call sql proc that generates pidms
//        schedulerJobService.

        return population
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
//     * @param sqlString the sql content
//     * @return a detail result describing the if the sqlString is valid or not
//     * @throws ApplicationException if the sqlString is so offensive as to prevent persisting
//     */
//    private CommunicationPopulationQueryParseResult validateSqlStringForSaving( String sqlString ) {
//        //check for sql injection and if it returns true then throw invalid exception
//        if (CommunicationCommonUtility.sqlStatementNotAllowed( sqlString, false )) {
//            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
//        }
//
//        return communicationPopulationQueryStatementParseService.parse( sqlString )
//    }

    /**
     * Checks two strings for equality regardless if either member is null.
     */
    private static boolean equals( String s1, String s2 ) {
        return s1 == null ? s2 == null : s1 == s2
    }


}
