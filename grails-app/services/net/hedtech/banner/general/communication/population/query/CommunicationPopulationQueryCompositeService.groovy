/*******************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.population.CommunicationPopulationQueryParseResult
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQuery
import net.hedtech.banner.general.communication.population.query.CommunicationPopulationQueryVersion
import org.apache.log4j.Logger

/**
 * Service for creating and manipulating a population query and query versions.
 */
class CommunicationPopulationQueryCompositeService {

    def communicationPopulationQueryService
    def communicationPopulationQueryVersionService
    def communicationPopulationQueryStatementParseService
    def log = Logger.getLogger(this.getClass())


    /**
     * Creates a new population query.
     *
     * @param query the query to persist
     */
    public CommunicationPopulationQuery createPopulationQuery( CommunicationPopulationQuery query ) {
        log.trace( "createPopulationQuery called" )
        assert( query.id == null )

        query.changesPending = true;
        validateSqlStringForSaving( query.sqlString )
        return (CommunicationPopulationQuery) communicationPopulationQueryService.create( query )
    }


    /**
     * Retrieves a population query.
     *
     * @param id the primary key of the population query
     */
    public CommunicationPopulationQuery fetchPopulationQuery( long id ) {
        return communicationPopulationQueryService.get( Long.valueOf( id ) )
    }

    /**
     * Deletes the population query along with any dependent query versions.
     *
     * @param queryId the primary key of the population query
     * @param version the optimistic lock counter
     */
    public boolean deletePopulationQuery( CommunicationPopulationQuery populationQuery ) {
        return deletePopulationQuery(populationQuery.id, populationQuery.version)
    }

    /**
     * Deletes the population query along with any dependent query versions.
     *
     * @param queryId the primary key of the population query
     * @param version the optimistic lock counter
     */
    public boolean deletePopulationQuery( long queryId, long version ) {
        log.trace("deletePopulationQuery called")
        List queryVersions = CommunicationPopulationQueryVersion.findByQueryId( queryId )
        if(queryVersions != null && queryVersions.size() > 0) {
            communicationPopulationQueryVersionService.delete(queryVersions)
        }

        def queryAsMap = [
            id     : Long.valueOf( queryId ),
            version: Long.valueOf( version )
        ]
        return communicationPopulationQueryService.delete( queryAsMap )
    }

    /**
     * Deletes the population query and/or query version.
     *
     * @param populationQuery the population query
     * @param queryVersion the population query version
     */
    public boolean deletePopulationQueryVersion( CommunicationPopulationQuery populationQuery, CommunicationPopulationQueryVersion queryVersion ) {
        return deletePopulationQueryVersion(populationQuery.id, populationQuery.version, queryVersion?queryVersion.id:0, queryVersion?queryVersion.version:0)
    }

    /**
     * Deletes the population query and/or query version.
     *
     * @param queryId the primary key of the population query
     * @param version the optimistic lock counter
     * @param versionId the primary key of the population query version
     * @param revisionVersion the optimistic lock counter
     */
    public boolean deletePopulationQueryVersion( long queryId, long version, long versionId, long revisionVersion) {
        log.trace("deletePopulationQueryVersion called")
        boolean retValue = true
        CommunicationPopulationQuery parentQuery = CommunicationPopulationQuery.fetchById(queryId)
        List queryVersions = CommunicationPopulationQueryVersion.findByQueryId(queryId)
        if(!parentQuery.changesPending)
        {
            //Request to delete a published version from query table where other published version exists
            if (queryVersions != null && queryVersions.size() > 0) {
                def queryVersionAsMap = [
                        id     : Long.valueOf(versionId),
                        version: Long.valueOf(revisionVersion)
                ]
                retValue = communicationPopulationQueryVersionService.delete(queryVersionAsMap)
                if(queryVersions.size() == 1)
                {
                    //Delete the parent query as well since all versions got deleted
                    def queryAsMap = [
                            id     : Long.valueOf(queryId),
                            version: Long.valueOf(version)
                    ]
                    retValue = communicationPopulationQueryService.delete(queryAsMap)
                }
                else
                {
                    boolean updateParentQuery = versionId == ((CommunicationPopulationQueryVersion)queryVersions[0]).id
                    if (updateParentQuery) {
                        //Update the parent query only if deleting the latest published version
                        CommunicationPopulationQueryVersion latestPublishedVersion = queryVersions[1]
                        def queryAsMap = [
                                id            : Long.valueOf(queryId),
                                version       : Long.valueOf(version),
                                changesPending: Boolean.toString(false),
                                sqlString     : latestPublishedVersion.sqlString
                        ]
                        communicationPopulationQueryService.update(queryAsMap)
                    }
                }
            }
        }
        else
        {
            if (queryVersions != null && queryVersions.size() > 0) {
                if(queryId == versionId)
                {
                    //Request to delete the unpublished version from query table where an older published version exists
                    CommunicationPopulationQueryVersion latestPublishedVersion = queryVersions[0]
                    def queryAsMap = [
                            id             : Long.valueOf(queryId),
                            version        : Long.valueOf(version),
                            changesPending : Boolean.toString(false),
                            sqlString      : latestPublishedVersion.sqlString
                    ]
                    communicationPopulationQueryService.update(queryAsMap)
                    retValue = true
                }
                else
                {
                    //Request to delete a published version from query table where an unpublished version and other older published version exists
                    def queryVersionAsMap = [
                            id     : Long.valueOf(versionId),
                            version: Long.valueOf(revisionVersion)
                    ]
                    retValue = communicationPopulationQueryVersionService.delete(queryVersionAsMap)
                }
            }
            else
            {
                //Single unpublished new parent query exists, delete from query table alone
                def queryAsMap = [
                        id     : Long.valueOf(queryId),
                        version: Long.valueOf(version)
                ]
                retValue = communicationPopulationQueryService.delete(queryAsMap)
            }
        }
        return retValue
    }


    /**
     * Updates an existing population query.
     *
     * @param query the query to persist
     */
    public CommunicationPopulationQuery updatePopulationQuery( Map queryAsMap ) {
        log.trace( "updatePopulationQuery called" )
        assert( queryAsMap.id != null )
        assert( queryAsMap.version != null ) // optimistic lock counter

        CommunicationPopulationQuery query = CommunicationPopulationQuery.fetchById( queryAsMap.id )
        if (!query) {
            throw new ApplicationException(
                "CommunicationPopulationQuery",
                queryAsMap.id,
                new NotFoundException(id: queryAsMap.id, entityClassName: CommunicationPopulationQuery.class.simpleName)
            )
        }

        if (!equals( query.sqlString, (String) queryAsMap.sqlString )) {
            queryAsMap.changesPending = true;
        }

        validateSqlStringForSaving( (String) queryAsMap.sqlString )

        return (CommunicationPopulationQuery) communicationPopulationQueryService.update( queryAsMap )
    }


    /**
     * Publishes the query by creating a read only copy of the intrinsic properties of the query
     * into a query version table. The query needs to valid for this operation to be legal.
     *
     * @param populationQuery the population query
     */
    public CommunicationPopulationQuery publishPopulationQuery( CommunicationPopulationQuery populationQuery ) {
        return publishPopulationQuery( populationQuery.id, populationQuery.version )
    }


    /**
     * Publishes the query by creating a read only copy of the intrinsic properties of the query
     * into a query version table. The query needs to valid for this operation to be legal.
     *
     * @param id the primary key of the population query
     * @param version the optimistic lock counter
     */
    public CommunicationPopulationQuery publishPopulationQuery( Long id, Long version ) {
        log.trace( "publishPopulationQuery called" )
        assert( id != null )
        assert( version != null ) // optimistic lock counter

        CommunicationPopulationQuery query = CommunicationPopulationQuery.fetchById( id )
        if (!query) {
            throw new ApplicationException(
                    "CommunicationPopulationQuery",
                    query.id,
                    new NotFoundException(id: query.id, entityClassName: CommunicationPopulationQuery.class.simpleName)
            )
        }

        if (!query.changesPending) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQuery.class, "noChangesToPublish" )
        }

        if (!query.sqlString || query.sqlString.trim().length() == 0) {
            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
        }

        CommunicationPopulationQueryParseResult parseResult = validateSqlStringForSaving( query.sqlString )
        if (!parseResult.isValid()) {
            throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
        }

        query.changesPending = false
        query = communicationPopulationQueryService.update( query )

        CommunicationPopulationQueryVersion queryVersion = new CommunicationPopulationQueryVersion()
        queryVersion.query = query
        queryVersion.sqlString = query.sqlString
        communicationPopulationQueryVersionService.create( [ domainModel: queryVersion ] )
        return query
    }


    /**
     * Checks the syntax of the given sql statement that it validates minimal requirements of a
     * sql query that can be used in a population query.
     *
     * @param populationQuerySql the sql string
     */
    public CommunicationPopulationQueryParseResult validateSqlStatement( String populationQuerySql ) {
        //ensure banner security is setup for this person so they can validate and execute
        //throw exception if the banner security for query execution is not setup for this user
        if (!CommunicationCommonUtility.userCanExecuteQuery()) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQuery.class, "noPermission" )
        }

        return communicationPopulationQueryStatementParseService.parse( populationQuerySql, false )
    }

    /**
     * Checks the sql if it is valid.
     * @param sqlString the sql content
     * @return a detail result describing the if the sqlString is valid or not
     * @throws ApplicationException if the sqlString is so offensive as to prevent persisting
     */
    private CommunicationPopulationQueryParseResult validateSqlStringForSaving( String sqlString ) {
        //check for sql injection and if it returns true then throw invalid exception
        if (CommunicationCommonUtility.sqlStatementNotAllowed( sqlString, false )) {
            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
        }

        return communicationPopulationQueryStatementParseService.parse( sqlString )
    }

    /**
     * Checks two strings for equality regardless if either member is null.
     */
    private static boolean equals( String s1, String s2 ) {
        return s1 == null ? s2 == null : s1 == s2
    }


}