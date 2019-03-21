/*******************************************************************************
 Copyright 2014-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population.query

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import org.apache.commons.lang.NotImplementedException

import java.sql.Connection
import java.sql.SQLException

/**
 * Service for creating and manipulating a population query and query versions.
 */
@Slf4j
class CommunicationPopulationQueryCompositeService {

    def communicationPopulationQueryService
    def communicationPopulationQueryVersionService
    def communicationPopulationQueryStatementParseService
    def sessionFactory
    //private static final log = Logger.getLogger(CommunicationPopulationQueryCompositeService.class)


    /**
     * Creates a new population query.
     *
     * @param query the query to persist
     */
    public CommunicationPopulationQuery createPopulationQuery( CommunicationPopulationQuery query ) {
        log.trace( "createPopulationQuery called" )
        assert( query.id == null )

        query.changesPending = true;
        validateQueryString( query )
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
                                changesPending: false,
                                queryString     : latestPublishedVersion.queryString
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
                            changesPending : false,
                            queryString      : latestPublishedVersion.queryString
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

        if (!equals( query.queryString, (String) queryAsMap.queryString )) {
            queryAsMap.changesPending = true;
        }

        CommunicationPopulationQueryType queryType = query.type
        if (queryAsMap.type != null) {
            queryType = (CommunicationPopulationQueryType) queryAsMap.type
        }

        validateQueryString( queryType, (String) queryAsMap.queryString, false )

        return (CommunicationPopulationQuery) communicationPopulationQueryService.update( queryAsMap )
    }


    /**
     * Publishes the query by creating a read only copy of the intrinsic properties of the query
     * into a query version table. The query needs to valid for this operation to be legal.
     *
     * @param populationQuery the population query
     * @return the query version produced by publishing the query
     */
    public CommunicationPopulationQueryVersion publishPopulationQuery( CommunicationPopulationQuery populationQuery ) {
        return publishPopulationQuery( populationQuery.id, populationQuery.version )
    }


    /**
     * Publishes the query by creating a read only copy of the intrinsic properties of the query
     * into a query version table. The query needs to valid for this operation to be legal.
     *
     * @param id the primary key of the population query
     * @param version the optimistic lock counter
     * @return the query version produced by publishing the query
     */
    public CommunicationPopulationQueryVersion publishPopulationQuery( Long id, Long version ) {
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

        if (!query.queryString || query.queryString.trim().length() == 0) {
            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
        }

        validateQueryString( query, true )

        query.changesPending = false
        query = communicationPopulationQueryService.update( query )

        CommunicationPopulationQueryVersion queryVersion = new CommunicationPopulationQueryVersion()
        queryVersion.query = query
        queryVersion.type = query.type
        queryVersion.queryString = query.queryString
        queryVersion = communicationPopulationQueryVersionService.create( [ domainModel: queryVersion ] )
        return queryVersion
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
     * Fetches the count of a population selection extract
     */
    public int fetchPopulationSelectionExtractQueryCount( String application, String selection, String creatorId, String userId ) {
        CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
        extractStatement.application = application
        extractStatement.selection = selection
        extractStatement.creatorId = creatorId
        extractStatement.userId = userId
        extractStatement.validate()

        def Sql sql
        try {
            int maxRows = 1
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            List<GroovyRowResult> resultSet = sql.rows( extractStatement.getCountSqlStatement(), 0, 1 )

            int count = 0
            resultSet.each { row ->
                row.each { column ->
                    count = column.value
                }
            }

            return count
        } catch (SQLException e) {
            log.error( "Failed to execute fetchPopulationSelectionExtractQueryCount with (application, selection, creatorId, userId) = (${application}, ${selection}, ${creatorId}, ${userId}).", e )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationQueryCompositeService.class, e, "fetchPopulationSelectionExtractQueryCountFailed" )
        } catch (Exception e) {
            log.error( "Failed to execute fetchPopulationSelectionExtractQueryCount with (application, selection, creatorId, userId) = (${application}, ${selection}, ${creatorId}, ${userId}).", e )
            throw e
        } finally {
            sql?.close()
        }
    }

    private void validateQueryString( CommunicationPopulationQuery query, boolean performPublishChecks = false ) {
        validateQueryString( query.type, query.queryString, performPublishChecks )
    }

    private void validateQueryString(CommunicationPopulationQueryType type, String queryString, boolean performPublishChecks ) {
        if (type?.equals( CommunicationPopulationQueryType.POPULATION_SELECTION_EXTRACT )) {
            CommunicationPopulationQueryExtractStatement extractStatement = new CommunicationPopulationQueryExtractStatement()
            extractStatement.setQueryString( queryString )
            extractStatement.validate()
        } else if (type?.equals( CommunicationPopulationQueryType.SQL_STATEMENT )) {
            CommunicationPopulationQueryParseResult parseResult = validateSqlStringForSaving( queryString )
            if (performPublishChecks && !parseResult.isValid()) {
                throw new ApplicationException(CommunicationPopulationQuery, "@@r1:queryInvalidCall@@")
            }
        } else {
            throw new NotImplementedException()
        }
    }

    /**
     * Checks the sql if it is valid.
     * @param queryString the sql content
     * @return a detail result describing the if the queryString is valid or not
     * @throws ApplicationException if the queryString is so offensive as to prevent persisting
     */
    private CommunicationPopulationQueryParseResult validateSqlStringForSaving( String queryString ) {
        //check for sql injection and if it returns true then throw invalid exception
        if (CommunicationCommonUtility.sqlStatementNotAllowed( queryString, false )) {
            throw new ApplicationException( CommunicationPopulationQuery, "@@r1:queryInvalidCall@@" )
        }

        return communicationPopulationQueryStatementParseService.parse( queryString )
    }

    /**
     * Checks two strings for equality regardless if either member is null.
     */
    private static boolean equals( String s1, String s2 ) {
        return s1 == null ? s2 == null : s1 == s2
    }


}
