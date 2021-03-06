/*******************************************************************************
 Copyright 2016-2017 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.population

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

@Slf4j
@Transactional
class CommunicationPopulationCalculationService extends ServiceBase {

    //private static final log = Logger.getLogger(CommunicationPopulationCalculationService.class)


    def preCreate(domainModelOrMap) {
        if (!CommunicationCommonUtility.userCanCreatePopulation()) {
            throw new ApplicationException(CommunicationPopulationCalculation, "@@r1:operation.not.authorized@@")
        }

        CommunicationPopulationCalculation calculation = getPopulationCalculation( domainModelOrMap )
        calculation.createDate = new Date()
        if (calculation.getCreatedBy() == null) {
            calculation.setCreatedBy( SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() )
        };
    }


    def preUpdate(domainModelOrMap) {
        CommunicationPopulationCalculation calculation = getPopulationCalculation( domainModelOrMap )

        if (calculation.id == null)
            throw new ApplicationException(CommunicationPopulationCalculation, "@@r1:populationCalculationDoesNotExist@@")

        def oldCalculation = CommunicationPopulationCalculation.get(calculation.id)

        if (oldCalculation.id == null)
            throw new ApplicationException(CommunicationPopulationCalculation, "@@r1:populationCalculationDoesNotExist@@")
    }


    private CommunicationPopulationCalculation getPopulationCalculation( domainModelOrMap ) {
        (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationPopulationCalculation
    }
}
