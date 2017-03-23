/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.orm.PagedResultList
import grails.util.Holders
import groovy.sql.Sql
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.exceptions.NotFoundException
import net.hedtech.banner.general.asynchronous.AsynchronousBannerAuthenticationSpoofer
import net.hedtech.banner.general.communication.CommunicationErrorCode
import net.hedtech.banner.general.communication.exceptions.CommunicationExceptionFactory
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.general.communication.groupsend.CommunicationGroupSend
import net.hedtech.banner.general.communication.interaction.CommunicationInteractionCompositeService
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
import net.hedtech.banner.general.person.PersonIdentificationName
import net.hedtech.banner.general.person.PersonUtility
import net.hedtech.banner.general.scheduler.SchedulerErrorContext
import net.hedtech.banner.general.scheduler.SchedulerJobContext
import net.hedtech.banner.general.scheduler.SchedulerJobReceipt
import net.hedtech.banner.general.scheduler.SchedulerJobService
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

import java.sql.Connection
import java.sql.SQLException

/**
 * A service for driving interaction with Communication population objects and their
 * dependent objects and services.
 */
@Transactional
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

    public CommunicationPopulation createPopulation( CommunicationFolder folder, String name, String description = "" ) {
        log.trace( "createPopulation called" )
        CommunicationPopulation population = new CommunicationPopulation()
        population.folder = folder
        population.name = name
        population.description = description
        population.changesPending = true
        population = communicationPopulationService.create( population )
        return population
    }

    public CommunicationPopulation updatePopulation( CommunicationPopulation population ) {
        log.trace( "updatePopulation called" )
        population = communicationPopulationService.update( population )
        return population
    }


    public CommunicationPopulationSelectionListBulkResults addPersonsToIncludeList( CommunicationPopulation population, List<String> bannerIds ) {
        log.trace("addPersonsToIncludeList called")

        if (bannerIds == null) {
            bannerIds = new ArrayList<String>()
        }

        def config = Holders?.config
        def dataOrigin = config.dataOrigin ?: "Banner"

        if (log.isDebugEnabled()) {
            log.debug("Dataorigin has been determined: $dataOrigin")
        }

        CommunicationPopulationSelectionListBulkResults results = new CommunicationPopulationSelectionListBulkResults()

        if (population.includeList == null) {
            if (log.isDebugEnabled()) {
                log.debug( "Creating new manual include list for population with id ${population.id}." )
            }

            CommunicationPopulationSelectionList selectionList = new CommunicationPopulationSelectionList()
            communicationPopulationSelectionListService.create( selectionList )

            population.includeList = selectionList
            population.changesPending = true
            population = communicationPopulationService.update( population )
        } else if (!population.changesPending) {
            population.changesPending = true
            population = communicationPopulationService.update( population )
        }
        if (log.isDebugEnabled()) {
            log.debug("Selection list has been created - $population?.includeList?.id")
        }

       //Define all the sql statements needed to insert, check for duplicates and check for non existent IDs
        def insertString1 = """  insert into gcrlent (
                                    gcrlent_slis_id,gcrlent_pidm,gcrlent_user_id,gcrlent_activity_date, gcrlent_data_origin
                                )
                                 select  ?, goodpidmlist.spridenpidm, USER, SYSDATE, ?
                                 from (SELECT spriden_pidm spridenpidm, spriden_change_ind spridenchangeind FROM spriden WHERE spriden_id IN
                             """
        def insertString2 = """     AND NOT EXISTS (select b.gcrlent_pidm from gcrlent b where  b.gcrlent_slis_id = ? and b.gcrlent_pidm = spriden_pidm)) goodpidmlist
                                 where  goodpidmlist.spridenchangeind IS NULL
                           """
        def idExistsSqlString = """
                           SELECT spriden_id from spriden, gcrlent
                           where spriden_pidm = gcrlent_pidm
                           and gcrlent_slis_id = ?
                           and spriden_Id in
                           """
        def duplicateExistsSqlString = """
                        SELECT  distinct spriden_pidm FROM spriden, gcrlent
                        WHERE  spriden_pidm = gcrlent_pidm
                        AND gcrlent_slis_id = ?
                        AND spriden_id IN
                        """
        //Define all total count variables
        def totalInserted = 0
        def totalIgnored = 0
        def totalDuplicates = 0
        def totalBannerIdsNotFound = 0
        def bannerIdsNotFound = []

        def batchSize = 0
        def batchDuplicateCount = 0
        def batchIgnoredCount = 0
        def batchInsertedcount = 0
        def batchNotFound = 0

        def sql
        def sqlParams = []
        def paramsMap = []
        def resultSet

        try {
            Connection conn = (Connection) sessionFactory.getCurrentSession().connection()
            sql = new Sql((Connection) sessionFactory.getCurrentSession().connection())

            def groupedBannerIds = bannerIds.collate(1000)

            groupedBannerIds.each { batchBannerIds ->

                batchSize = batchBannerIds.size()

                batchDuplicateCount = 0
                batchIgnoredCount = 0
                batchInsertedcount = 0
                batchNotFound = 0

                paramsMap = []
                sqlParams = []
//create the bind parameter map for the insert statement
                paramsMap.add(population.includeList.id)
                paramsMap.add(dataOrigin)
                paramsMap.addAll(batchBannerIds)
                paramsMap.add(population.includeList.id)

                // create the bind parameter map for the 2 query statement
                sqlParams.add(population.includeList.id)
                sqlParams.addAll(batchBannerIds)

                //A IN predicate requires a bind placeholder for each of the value.
                def bindplaceholderstring = StringUtils.repeat("?,", batchSize - 1) + '?'

                //run the dup query for duplicates already existing in selection list
                def sqldupquery = duplicateExistsSqlString + '(' + bindplaceholderstring + ')'
                def duplicateIds = sql.rows(sqldupquery, sqlParams)
                batchDuplicateCount = duplicateIds.size()

                //run the insert statement
                def sqlinsert = insertString1 + '(' + bindplaceholderstring + ')' + insertString2
                sql.executeUpdate(sqlinsert, paramsMap)
                batchInsertedcount = sql.updateCount

                //get which ids exist
                def sqlquery = idExistsSqlString + '(' + bindplaceholderstring + ')'
                resultSet = sql.rows(sqlquery, sqlParams)

                //collect all IDs that dont exist in banner
                batchBannerIds.removeAll(resultSet.collect { it.SPRIDEN_ID })
                bannerIdsNotFound.addAll(batchBannerIds)
                batchNotFound = batchBannerIds.size()

                batchIgnoredCount = (batchSize - batchInsertedcount)  // dup or not exists
                //the duplicates in the file + pidms that were already in selection list + mulitple ids in file for the same pidm
                // duplicates in file = batchSize - uniquebatch size
                //duplicates already in selection list =  batchDuplicationCount
                //multiple pidms = what was ignored - what doesnt exist - what already exists

                batchDuplicateCount = (batchSize - batchInsertedcount - batchNotFound)

                //update the totals
                totalDuplicates = totalDuplicates + batchDuplicateCount
                totalInserted = totalInserted + batchInsertedcount
                totalBannerIdsNotFound = totalBannerIdsNotFound + batchNotFound
                totalIgnored = totalIgnored + batchIgnoredCount
            }
        } catch (Exception e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService.class, e )
        } finally {
            sql?.close()
        }
        results.population = population
        results.insertedCount = totalInserted
        results.duplicateCount = totalDuplicates
        results.ignoredCount = totalIgnored
        results.bannerIdsNotFound = bannerIdsNotFound
        results.notExistCount = totalBannerIdsNotFound
        return results
    }

    public CommunicationPopulation addPersonToIncludeList( CommunicationPopulation population, String bannerId ) {
        log.trace( "addPersonToIncludeList called" )
        if ((bannerId == null) || (bannerId.trim().size() == 0)) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService.class, "bannerIdInvalidOrEmpty", bannerId )
        }

        CommunicationPopulationSelectionListBulkResults results = addPersonsToIncludeList( population, [bannerId] )
        if (results.insertedCount == 1) {
            if (log.isDebugEnabled()) {
                log.debug( "Banner ID '${bannerId} added to population id = ${population.id}." )
            }
        } else if (results.notExistCount > 0) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService.class, "bannerIdNotFound", bannerId )
        } else if (results.duplicateCount == 1) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService.class, "bannerIdAlreadyExistsInPopulation", bannerId )
            if (log.isDebugEnabled()) {
                log.debug( "Banner ID '${bannerId} already exists in population include list - ignoring." )
            }
        }
        return results.population
    }

    public CommunicationPopulation removePersonFromIncludeList( CommunicationPopulation population, String bannerId ) {
        log.trace( "removePersonFromIncludeList called" )

        if (population.includeList == null) {
            if (log.isDebugEnabled()) {
                log.debug( "Population has no include list - ignoring." )
            }
            return population
        }

        PersonIdentificationName identificationName = CommunicationInteractionCompositeService.getPersonOrNonPerson( bannerId )
        if (identificationName == null) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService.class, "bannerIdNotFound", bannerId )
        }

        CommunicationPopulationSelectionListEntry found = CommunicationPopulationSelectionListEntry.findByPidmAndPopulationSelectionList( identificationName.pidm, population.includeList )
        if (found) {
            if (log.isDebugEnabled()) {
                log.debug( "Removing manual include entry for population ${population.id} with banner id ${bannerId}." )
            }
            communicationPopulationSelectionListEntryService.delete( found )

            if (!population.changesPending) {
                population.changesPending = true
                population = communicationPopulationService.update( population )
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug( "Person with banner id ${bannerId} not found in population ${population.id} - ignoring." )
            }
        }

        return population
    }

    public CommunicationPopulation removeAllPersonsFromIncludeList( CommunicationPopulation population ) {
        log.trace( "removeAllPersonsFromIncludeList called" )
        assert (population != null) && (population.id != null)

        if (population.includeList == null) {
            if (log.isDebugEnabled()) {
                log.debug( "Include list for population with id ${population.id} is null." )
            }
            return population
        }
        assert population.includeList.id != null

        def entryCount = CommunicationPopulationSelectionListEntry.countByPopulationSelectionList( population.includeList )

        if (entryCount == 0) {
            if (log.isDebugEnabled()) {
                log.debug( "Include list for population with id ${population.id} has no entries." )
            }
            return population
        } else {
            if (log.isDebugEnabled()) {
                log.debug( "Entry count for population with id ${population.id} is ${entryCount} before update." )
            }
        }

        def Sql sql
        try {
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            int rowsUpdated = sql.executeUpdate( "delete from GCRLENT where GCRLENT_SLIS_ID = ?", [population.includeList.id] )
            if (log.isDebugEnabled()) {
                log.debug( "Deleted ${rowsUpdated} included entries for population with name ${population.name} and id ${population.id}." )
            }
        } catch (SQLException e) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService, e )
        } catch (Throwable t) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService, t )
        } finally {
            sql?.close()
        }

        if (!population.changesPending) {
            population.changesPending = true
            population = communicationPopulationService.update( population )
        }

        return population
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
        log.trace( "createPopulationFromQueryVersion( populationVersion, name, d`escription ) called" )
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

    public CommunicationPopulationDetail fetchPopulationDetail( long populationId ) {
        log.trace( "Calling fetchPopulationDetail with id ${populationId}" )

        CommunicationPopulation population = fetchPopulation( populationId )
        if (population == null) {
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService, "populationNotFound" )
        }

        CommunicationPopulationDetail populationDetail = new CommunicationPopulationDetail()
        populationDetail.populationListView = CommunicationPopulationListView.fetchLatestByPopulation( population )
        populationDetail.totalCount = CommunicationPopulationProfileView.findTotalCountByPopulation( populationDetail.populationListView )
        return populationDetail
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
        communicationPopulationCalculationService.delete( calculation )
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
    public CommunicationPopulation updatePopulation(Map populationAsMap) {
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
        int queryAssociationCount = CommunicationPopulationQueryAssociation.countByPopulation( population )

        if (queryAssociationCount == 0) {
            return null
        } else {
            CommunicationPopulationVersion populationVersion = CommunicationPopulationVersion.findLatestByPopulationId( population.id )
            return calculatePopulationVersionForUser( populationVersion, oracleName )
        }
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

            SchedulerJobContext jobContext = new SchedulerJobContext( populationCalculation.jobId )
                    .setBannerUser( oracleName )
                    .setMepCode( populationCalculation.mepCode )
                    .setJobHandle( "communicationPopulationCompositeService", "processPendingPopulationCalculationFired" )
                    .setErrorHandle( "communicationPopulationCompositeService", "processPendingPopulationCalculationFailed" )
                    .setParameter( "populationCalculationId", populationCalculation.id )

            SchedulerJobReceipt jobReceipt = schedulerJobService.scheduleNowServiceMethod( jobContext )
            return populationCalculation
        } finally {
            log.trace( "exiting calculatePopulationVersionForUser( population )")
        }
    }

    public CommunicationPopulationCalculation calculatePopulationVersionForGroupSend(CommunicationPopulationVersion populationVersion) {
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
                schedulerJobService.deleteScheduledJob( calculation.jobId, "communicationPopulationCompositeService", "processPendingPopulationCalculationFired" )
            }
            try {
                communicationPopulationCalculationService.delete( calculation )
            } catch( ApplicationException e ) {
                if (e.getWrappedException()?.getCause()?.getConstraintName()?.equals( "GENERAL.FK1_GCBGSND_INV_GCRPOPC" )) {
                    throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotDeletePopulationWithExistingGroupSends")
                } else {
                    throw e
                }
            }

            if (selectionList) {
                selectionList.delete()
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

    public CommunicationPopulationCalculation processPendingPopulationCalculationFired( SchedulerJobContext jobContext ) {
        return processPendingPopulationCalculation( jobContext.parameters )
    }

    public CommunicationPopulationCalculation processPendingPopulationCalculationFailed( SchedulerErrorContext errorContext ) {
        Long populationCalculationId = errorContext.jobContext.getParameter( "populationCalculationId" )
        if (log.isDebugEnabled()) {
            log.debug("${errorContext.jobContext.errorHandle} called for groupSendId = ${groupSendId} with message = ${errorContext?.cause?.message}")
        }

        CommunicationPopulationCalculation calculation = CommunicationPopulationCalculation.fetchById( populationCalculationId )
        if (!calculation) {
            // Calculation may have been deleted in which case silently return
            return null
        }

        calculation.status = CommunicationPopulationCalculationStatus.ERROR
        if (errorContext.cause) {
            calculation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
            calculation.errorText = errorContext.cause.message
        } else {
            calculation.errorCode = CommunicationErrorCode.UNKNOWN_ERROR
        }
        calculation.save( failOnError: true, flush: true )
        return calculation
    }

    /**
     * This method is meant to be called from a quartz service to complete
     * the calculation request of a population version.
     * @param parameters a set of parameters passed from the original request through the quartz job detail data map
     */
    private CommunicationPopulationCalculation processPendingPopulationCalculation( Map parameters ) {
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
            return null
        } else {
            return (CommunicationPopulationQueryAssociation) queryAssociationList.get(0)
        }
    }

    /**
     * Creates a read only copy of the current population. This should be done at least once prior to a calculation as
     * calculations are based on a static evaluation of the population. This model allows for changes in the population
     * where calculations can co-exists with different versions of the population.
     * @param population
     * @return
     */
    public CommunicationPopulationVersion createPopulationVersion(CommunicationPopulation population) {
        log.trace( "createPopulationVersion( population ) called" )
        try {
            CommunicationPopulationVersion populationVersion = new CommunicationPopulationVersion()
            populationVersion.population = population
            populationVersion.includeList = cloneSelectionList( population.includeList )

            populationVersion = (CommunicationPopulationVersion) communicationPopulationVersionService.create( populationVersion )
            assert populationVersion.id
            if (log.isDebugEnabled()) log.debug( "population version with id = ${populationVersion.id} created." )

            CommunicationPopulationQueryAssociation populationQueryAssociation = fetchPopulationQueryAssociation( population )
            if (populationQueryAssociation) {
                CommunicationPopulationVersionQueryAssociation populationVersionQueryAssociation = populationQueryAssociation.createVersion( populationVersion )
                populationVersionQueryAssociation = (CommunicationPopulationVersionQueryAssociation) communicationPopulationVersionQueryAssociationService.create( populationVersionQueryAssociation )
                assert populationVersionQueryAssociation.id
                if (log.isDebugEnabled()) log.debug( "population version query association with id = ${populationVersionQueryAssociation.id} created." )
            }

            return populationVersion
        } finally {
            log.trace( "createPopulationVersion( population ) exited" )
        }
    }

    public CommunicationPopulationSelectionList cloneSelectionList( CommunicationPopulationSelectionList selectionList ) {
        if (selectionList == null) {
            return null
        }
        CommunicationPopulationSelectionList clone = new CommunicationPopulationSelectionList()
        clone = communicationPopulationSelectionListService.create( clone )

        def Sql sql
        try {
            sql = new Sql( (Connection) sessionFactory.getCurrentSession().connection() )
            int rowsUpdated = sql.executeUpdate(
                "INSERT INTO GCRLENT (GCRLENT_SLIS_ID, GCRLENT_PIDM, GCRLENT_USER_ID, GCRLENT_ACTIVITY_DATE, GCRLENT_DATA_ORIGIN) " +
                        "SELECT ?, GCRLENT_PIDM, ?, SYSDATE, GCRLENT_DATA_ORIGIN FROM GCRLENT WHERE GCRLENT_SLIS_ID = ?",
                [ clone.id, getCurrentUserBannerId(), selectionList.id ]
            )

            if (log.isDebugEnabled()) {
                log.debug( "Deleted ${rowsUpdated} included entries for population with name ${population.name} and id ${population.id}." )
            }
        } catch (SQLException e) {
            this.log.error( "Failed to clone selection list", e )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService, e )
        } catch (Throwable t) {
            this.log.error( "Failed to clone selection list", t )
            throw CommunicationExceptionFactory.createApplicationException( CommunicationPopulationCompositeService, t )
        } finally {
//            sql?.close()
        }

        return clone
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
        CommunicationPopulationCalculation populationCalculation = CommunicationPopulationCalculation.findLatestByPopulationVersionIdAndCalculatedBy(populationVersion.id, oracleName)
        if (populationCalculation) {
            if (populationCalculation.status.equals(CommunicationPopulationCalculationStatus.PENDING_EXECUTION)) {
                throw CommunicationExceptionFactory.createApplicationException(this.getClass(), "cannotRecalculatePopulationPendingExecution")
            } else {
                if (populationCalculation.id != null && CommunicationGroupSend.countByPopulationCalculationId( populationCalculation.id ) == 0) {
                    deletePopulationCalculation(populationCalculation)
                }
//                if (CommunicationGroupSend.findCountByPopulationCalculationId(populationCalculation.id) == 0) {
//                    deletePopulationCalculation(populationCalculation)
//                }
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
