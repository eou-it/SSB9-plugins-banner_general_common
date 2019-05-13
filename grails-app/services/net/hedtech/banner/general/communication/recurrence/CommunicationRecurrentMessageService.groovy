/*******************************************************************************
 Copyright 2018-2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.communication.recurrence

import grails.gorm.transactions.Transactional
import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.CommunicationCommonUtility
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import net.hedtech.banner.service.ServiceBase
import org.springframework.security.core.context.SecurityContextHolder

import java.text.SimpleDateFormat

@Transactional
class CommunicationRecurrentMessageService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationRecurrentMessage recurrentMessage = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationRecurrentMessage
        if (recurrentMessage.getCreatedBy() == null) {
            recurrentMessage.setCreatedBy( SecurityContextHolder?.context?.authentication?.principal?.getOracleUserName() )
        }

        Date now = new Date()
        if (recurrentMessage.getCreationDateTime() == null) {
            recurrentMessage.setCreationDateTime( now )
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = sdf.parse(sdf.format(now));
        if(today.compareTo(recurrentMessage.startDate) == 0) {
            //Today's date
            recurrentMessage.setStartDate(now)
        } else if(today.compareTo(recurrentMessage.startDate) < 0) {
            //future date
            Calendar startDateCalendar = Calendar.getInstance()
            startDateCalendar.setTime(recurrentMessage.startDate)
            startDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
            startDateCalendar.set(Calendar.MINUTE, 0)
            startDateCalendar.set(Calendar.SECOND, 0)
            startDateCalendar.set(Calendar.MILLISECOND, 0)
            recurrentMessage.setStartDate(startDateCalendar.getTime())
        }

        if (recurrentMessage.getName() == null) {
            recurrentMessage.setName(CommunicationTemplate.get(recurrentMessage.templateId).getName())
        }
    }

    def preUpdate( domainModelOrMap ) {
        CommunicationRecurrentMessage recurrentMessage = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationRecurrentMessage

        if (recurrentMessage.id == null)
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:recurrentMessageDoesNotExist@@")

        def oldRecurrentMessage = CommunicationRecurrentMessage.get(recurrentMessage.id)

        if (oldRecurrentMessage.id == null)
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:recurrentMessageDoesNotExist@@")

        //check if user is authorized. user should be admin or author
        if (!CommunicationCommonUtility.userCanUpdateDeletePopulation(oldRecurrentMessage.createdBy)) {
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:operation.not.authorized@@")
        }

        if (recurrentMessage.name == null || recurrentMessage.name == "") {
            throw new ApplicationException(CommunicationRecurrentMessage, "@@r1:nameCannotBeNull@@")
        }

        if(recurrentMessage.startDate.compareTo(oldRecurrentMessage.startDate) != 0) {
            Date now = new Date()
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date today = sdf.parse(sdf.format(now));

            if (today.compareTo(recurrentMessage.startDate) == 0) {
                //Today's date
                recurrentMessage.setStartDate(now)
            } else if (today.compareTo(recurrentMessage.startDate) < 0) {
                //future date
                Calendar startDateCalendar = Calendar.getInstance()
                startDateCalendar.setTime(recurrentMessage.startDate)
                startDateCalendar.set(Calendar.HOUR_OF_DAY, 0)
                startDateCalendar.set(Calendar.MINUTE, 0)
                startDateCalendar.set(Calendar.SECOND, 0)
                startDateCalendar.set(Calendar.MILLISECOND, 0)
                recurrentMessage.setStartDate(startDateCalendar.getTime())
            }
        }
    }
}
