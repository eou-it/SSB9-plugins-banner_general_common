/*********************************************************************************
 Copyright 2010-2015 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall.ldm

import net.hedtech.banner.service.DatabaseModifiesState

import javax.persistence.*

/**
 * Section Meeting Times model for HEDM search
 * It is a read-only model
 */

@NamedQueries([
        @NamedQuery(name = "SectionMeetingTimeHedmDetail.fetchAllByGuidInList",
                query = """FROM SectionMeetingTimeHedmDetail where guid in (:guids)""")
])

@Entity
@Table(name = "SVQ_SSRMEET_GORGUID")
@DatabaseModifiesState
class SectionMeetingTimeHedmDetail implements Serializable {

    @Id
    @Column(name = "instructional_event_guid")
    String guid

    @Column(name = "instructional_method_code")
    String instructionalMethodCode

    @Column(name = "instructional_method_desc")
    String instructionalMethodDescription

    @Column(name = "instructional_method_guid")
    String instructionalMethodGuid

    @Temporal(TemporalType.DATE)
    @Column(name = "start_date")
    Date startDate

    @Temporal(TemporalType.DATE)
    @Column(name = "end_date")
    Date endDate

    @Column(name = "begin_time")
    String beginTime

    @Column(name = "end_time")
    String endTime

    @Column(name = "sunday")
    String sunday

    @Column(name = "monday")
    String monday

    @Column(name = "tuesday")
    String tuesday

    @Column(name = "wednesday")
    String wednesday

    @Column(name = "thursday")
    String thursday

    @Column(name = "friday")
    String friday

    @Column(name = "saturday")
    String saturday

    @Column(name = "room_guid")
    String roomGuid

    @Column(name = "room_desc")
    String roomDescription

    @Column(name = "room_number")
    String roomNumber

    @Column(name = "building_guid")
    String buildingGuid

    @Column(name = "building_desc")
    String buildingDescription

    @Column(name = "house_number")
    String houseNumber

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        SectionMeetingTimeHedmDetail that = (SectionMeetingTimeHedmDetail) o

        if (beginTime != that.beginTime) return false
        if (buildingDescription != that.buildingDescription) return false
        if (buildingGuid != that.buildingGuid) return false
        if (endDate != that.endDate) return false
        if (endTime != that.endTime) return false
        if (friday != that.friday) return false
        if (guid != that.guid) return false
        if (houseNumber != that.houseNumber) return false
        if (instructionalMethodCode != that.instructionalMethodCode) return false
        if (instructionalMethodDescription != that.instructionalMethodDescription) return false
        if (instructionalMethodGuid != that.instructionalMethodGuid) return false
        if (monday != that.monday) return false
        if (roomDescription != that.roomDescription) return false
        if (roomGuid != that.roomGuid) return false
        if (roomNumber != that.roomNumber) return false
        if (saturday != that.saturday) return false
        if (startDate != that.startDate) return false
        if (sunday != that.sunday) return false
        if (thursday != that.thursday) return false
        if (tuesday != that.tuesday) return false
        if (wednesday != that.wednesday) return false

        return true
    }

    int hashCode() {
        int result
        result = (guid != null ? guid.hashCode() : 0)
        result = 31 * result + (instructionalMethodCode != null ? instructionalMethodCode.hashCode() : 0)
        result = 31 * result + (instructionalMethodDescription != null ? instructionalMethodDescription.hashCode() : 0)
        result = 31 * result + (instructionalMethodGuid != null ? instructionalMethodGuid.hashCode() : 0)
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0)
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0)
        result = 31 * result + (beginTime != null ? beginTime.hashCode() : 0)
        result = 31 * result + (endTime != null ? endTime.hashCode() : 0)
        result = 31 * result + (sunday != null ? sunday.hashCode() : 0)
        result = 31 * result + (monday != null ? monday.hashCode() : 0)
        result = 31 * result + (tuesday != null ? tuesday.hashCode() : 0)
        result = 31 * result + (wednesday != null ? wednesday.hashCode() : 0)
        result = 31 * result + (thursday != null ? thursday.hashCode() : 0)
        result = 31 * result + (friday != null ? friday.hashCode() : 0)
        result = 31 * result + (saturday != null ? saturday.hashCode() : 0)
        result = 31 * result + (roomGuid != null ? roomGuid.hashCode() : 0)
        result = 31 * result + (roomDescription != null ? roomDescription.hashCode() : 0)
        result = 31 * result + (roomNumber != null ? roomNumber.hashCode() : 0)
        result = 31 * result + (buildingGuid != null ? buildingGuid.hashCode() : 0)
        result = 31 * result + (buildingDescription != null ? buildingDescription.hashCode() : 0)
        result = 31 * result + (houseNumber != null ? houseNumber.hashCode() : 0)
        return result
    }

    static List<SectionMeetingTimeHedmDetail> fetchAllByGuidInList(List guids) {
        List<SectionMeetingTimeHedmDetail> sectionMeetingTimeHedmDetails = []
        List<SectionMeetingTimeHedmDetail> sectionMeetingTimeHedmDetailsTemp = []

        if(guids && guids.size() > 0){
            List guidsTemp = []
            def noOfLoops = Math.ceil(guids.size()/1000)
            int guidsTempLoop
            int guidsInd=0

            for (int i=0;i<noOfLoops;i++){
                guidsTemp.clear()

                guidsTempLoop=0
                while (guidsTempLoop<=999 && guidsInd<guids.size()){
                    guidsTemp.add(guids.get(guidsInd))
                    guidsInd = guidsInd+1
                    guidsTempLoop=guidsTempLoop+1
                }

                SectionMeetingTimeHedmDetail.withSession { session ->
                    sectionMeetingTimeHedmDetailsTemp = session.getNamedQuery('SectionMeetingTimeHedmDetail.fetchAllByGuidInList')
                            .setParameterList('guids', guidsTemp).list()
                }

                sectionMeetingTimeHedmDetails.addAll(sectionMeetingTimeHedmDetailsTemp)

            }
        }

       
        return sectionMeetingTimeHedmDetails
    }
}
