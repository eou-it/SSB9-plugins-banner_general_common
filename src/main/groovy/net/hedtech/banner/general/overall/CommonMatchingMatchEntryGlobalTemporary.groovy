/*********************************************************************************
 Copyright 2010-2018 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.system.*

import javax.persistence.*

/**
 * Common Matching Match Entry Global Temporary Table 
 */
@Entity
@Table(name = "GOTCMME")
@EqualsAndHashCode(includeFields = true)
class CommonMatchingMatchEntryGlobalTemporary implements Serializable {

    /**
     * Surrogate ID for GOTCMME
     */
    @Id
    @Column(name = "GOTCMME_SURROGATE_ID")
    @SequenceGenerator(name = "GOTCMME_SEQ_GEN", allocationSize = 1, sequenceName = "GOTCMME_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOTCMME_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GOTCMME
     */
    @Version
    @Column(name = "GOTCMME_VERSION")
    Long version

    /**
     * LAST NAME: The last name of the person or the name of the non-person to be created.
     */
    @Column(name = "GOTCMME_LAST_NAME")
    String lastName

    /**
     * ENTITY INDICATOR: Identifies whether record is person or non-person to be created. It does not display on the form. Valid values P = person, C = non-person.
     */
    @Column(name = "GOTCMME_ENTITY_CDE")
    String entity

    /**
     * FIRST NAME: The name of the person to be created.
     */
    @Column(name = "GOTCMME_FIRST_NAME")
    String firstName

    /**
     * MIDDLE NAME: The middle name of the person to be created.
     */
    @Column(name = "GOTCMME_MI")
    String middleInitial

    /**
     * ID: Identification Number for the person or non-person record to be created.
     */
    @Column(name = "GOTCMME_ID")
    String commonMatchingMatchEntryGlobalTemporaryId

    /**
     * ADDRESS LINE 1: The first line of the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_STREET_LINE1")
    String streetLine1

    /**
     * ADDRESS LINE 2: The second line of the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_STREET_LINE2")
    String streetLine2

    /**
     * ADDRESS LINE 3: The third line of the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_STREET_LINE3")
    String streetLine3

    /**
     * CITY: The city associated with the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_CITY")
    String city

    /**
     * ZIP: The zip or postal code associated with the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_ZIP")
    String zip

    /**
     * AREA CODE: The telephone area code associated with the phone number of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_PHONE_AREA")
    String phoneArea

    /**
     * PHONE NUMBER: The telephone number of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_PHONE_NUMBER")
    String phoneNumber

    /**
     * PHONE EXTENSION: The extention of the telephone number of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_PHONE_EXT")
    String phoneExtension

    /**
     * SOCIAL SECURITY NUMBER: The Social Security Number, Social Insurance Number, or the Tax File Number associated with the person/non-person to be created.
     */
    @Column(name = "GOTCMME_SSN")
    String ssn

    /**
     * BIRTH DAY: The day of the person's birthday to be created.
     */
    @Column(name = "GOTCMME_BIRTH_DAY")
    String birthDay

    /**
     * BIRTH MONTH: The month of the person's birthday to be created.
     */
    @Column(name = "GOTCMME_BIRTH_MON")
    String birthMonday

    /**
     * BIRTH YEAR: The year of the person's birthday to be created.
     */
    @Column(name = "GOTCMME_BIRTH_YEAR")
    String birthYear

    /**
     * GENDER: The gender of the person to be created.
     */
    @Column(name = "GOTCMME_SEX")
    String sex

    /**
     * E-MAIL ADDRESS: The e-mail address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_EMAIL_ADDRESS")
    String emailAddress

    /**
     * TELEPHONE COUNTRY CODE: Code designating the region or country.
     */
    @Column(name = "GOTCMME_CTRY_CODE_PHONE")
    String countryPhone

    /**
     * HOUSE NUMBER: Building or lot number on a street or in an area.
     */
    @Column(name = "GOTCMME_HOUSE_NUMBER")
    String houseNumber

    /**
     * ADDRESS LINE4: The fourth line of the address of the person/non-person to be created.
     */
    @Column(name = "GOTCMME_STREET_LINE4")
    String streetLine4

    /**
     * SURNAME PREFIX: Name tag preceding the last name or surname.  (Van, Von, Mac, etc.)
     */
    @Column(name = "GOTCMME_SURNAME_PREFIX")
    String surnamePrefix

    /**
     * Last modification date for GOTCMME
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GOTCMME_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for GOTCMME
     */
    @Column(name = "GOTCMME_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GOTCMME
     */
    @Column(name = "GOTCMME_DATA_ORIGIN")
    String dataOrigin

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVATYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_ATYP_CODE", referencedColumnName = "STVATYP_CODE")
    ])
    AddressType addressType

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVTELE_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_TELE_CODE", referencedColumnName = "STVTELE_CODE")
    ])
    TelephoneType telephoneType

    /**
     * Foreign Key : FKV_GOTCMME_INV_GTVEMAL_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_EMAL_CODE", referencedColumnName = "GTVEMAL_CODE")
    ])
    EmailType emailType

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVASRC_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_ASRC_CODE", referencedColumnName = "STVASRC_CODE")
    ])
    AddressSource addressSource

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVSTAT_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_STAT_CODE", referencedColumnName = "STVSTAT_CODE")
    ])
    State state

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVNATN_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_NATN_CODE", referencedColumnName = "STVNATN_CODE")
    ])
    Nation nation

    /**
     * Foreign Key : FKV_GOTCMME_INV_STVCNTY_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name = "GOTCMME_CNTY_CODE", referencedColumnName = "STVCNTY_CODE")
    ])
    County county


    public String toString() {
        """CommonMatchingMatchEntryGlobalTemporary[
					id=$id, 
					version=$version, 
					lastName=$lastName, 
					entity=$entity, 
					firstName=$firstName, 
					middleInitial=$middleInitial, 
					commonMatchingMatchEntryGlobalTemporaryId=$commonMatchingMatchEntryGlobalTemporaryId, 
					streetLine1=$streetLine1, 
					streetLine2=$streetLine2, 
					streetLine3=$streetLine3, 
					city=$city, 
					zip=$zip, 
					phoneArea=$phoneArea, 
					phoneNumber=$phoneNumber, 
					phoneExtension=$phoneExtension, 
					ssn=$ssn, 
					birthDay=$birthDay, 
					birthMonday=$birthMonday, 
					birthYear=$birthYear, 
					sex=$sex, 
					emailAddress=$emailAddress, 
					countryPhone=$countryPhone, 
					houseNumber=$houseNumber, 
					streetLine4=$streetLine4, 
					surnamePrefix=$surnamePrefix, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					addressType=$addressType, 
					telephoneType=$telephoneType, 
					emailType=$emailType, 
					addressSource=$addressSource, 
					state=$state, 
					nation=$nation, 
					county=$county]"""
    }


    static constraints = {
        lastName(nullable: true, maxSize: 60)
        entity(nullable: false, maxSize: 1)
        firstName(nullable: true, maxSize: 60)
        middleInitial(nullable: true, maxSize: 60)
        streetLine1(nullable: true, maxSize: 75)
        streetLine2(nullable: true, maxSize: 75)
        streetLine3(nullable: true, maxSize: 75)
        city(nullable: true, maxSize: 50)
        zip(nullable: true, maxSize: 30)
        phoneArea(nullable: true, maxSize: 6)
        phoneNumber(nullable: true, maxSize: 12)
        phoneExtension(nullable: true, maxSize: 10)
        ssn(nullable: true, maxSize: 15)
        birthDay(nullable: true, maxSize: 2)
        birthMonday(nullable: true, maxSize: 2)
        birthYear(nullable: true, maxSize: 4)
        sex(nullable: true, maxSize: 1)
        emailAddress(nullable: true, maxSize: 128)
        countryPhone(nullable: true, maxSize: 4)
        houseNumber(nullable: true, maxSize: 10)
        streetLine4(nullable: true, maxSize: 75)
        surnamePrefix(nullable: true, maxSize: 60)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        addressType(nullable: true)
        telephoneType(nullable: true)
        emailType(nullable: true)
        addressSource(nullable: true)
        state(nullable: true)
        nation(nullable: true)
        county(nullable: true)
        commonMatchingMatchEntryGlobalTemporaryId(nullable: true, maxSize: 9)
    }
}
