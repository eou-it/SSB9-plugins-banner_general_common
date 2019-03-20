/*********************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode

import javax.persistence.*
import net.hedtech.banner.service.DatabaseModifiesState

/**
 * Third party access table.
 */

@Entity
@Table(name = "GV_GOBTPAC")
@EqualsAndHashCode(includeFields = true)
@DatabaseModifiesState
class ThirdPartyAccess implements Serializable {

    /**
     * Surrogate ID for GOBTPAC
     */
    @Id
    @Column(name = "GOBTPAC_SURROGATE_ID")
    @SequenceGenerator(name = "GOBTPAC_SEQ_GEN", allocationSize = 1, sequenceName = "GOBTPAC_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GOBTPAC_SEQ_GEN")
    Integer id

    /**
     * Optimistic lock token for GOBTPAC
     */
    @Version
    @Column(name = "GOBTPAC_VERSION")
    Integer version

    /**
     * Internal identification number of the person.
     */
    @Column(name = "GOBTPAC_PIDM")
    Integer pidm

    /**
     * PIN disabled Indicator. When Y, access to SCT Third Party Access products (Web Fors, VR, Kiosk) will be denied.
     */
    @Column(name = "GOBTPAC_PIN_DISABLED_IND")
    String pinDisabledIndicator

    /**
     * When N, Terms of Usage page displays upon login to Web For products. When Y then it does not display. 
     * This page can be customized using the WebTailor Customize a set of Information Text feature.
     */
    @Column(name = "GOBTPAC_USAGE_ACCEPT_IND")
    String termsOfUsageIndicator

    /**
     * It is used together with the Login ID to access SCT Third Party products and other partner systems. 
     * Must be a six digit number. 
     */
    @Column(name = "GOBTPAC_PIN")
    String personalIdentificationNumber

    /**
     * When valued and the date is in the past, a user attempting to access a Third Party product will be required to change their pin. 
     */
    @Column(name = "GOBTPAC_PIN_EXP_DATE")
    Date pinExpirationDate

    /**
     * Value which may be passed to the partner systems as a login ID and/or email ID.
     */
    @Column(name = "GOBTPAC_EXTERNAL_USER")
    String externalUser

    /**
     * Free form question entered by a Web User to be used as a hint when trying to get their PIN reset. 
     */
    @Column(name = "GOBTPAC_QUESTION")
    String pinResetQuestion

    /**
     * Free form response entered by a Web User to be used as answer to the hint question when trying to get their PIN reset. 
     */
    @Column(name = "GOBTPAC_RESPONSE")
    String pinResetResponse

    /**
     * Describes the source of the GOBTPAC insert or update. SELF = User change the pin record, 
     * ADMIN = Administrator change the pin record, SYSTEM = Record was changed by the logic in a process.
     */
    @Column(name = "GOBTPAC_INSERT_SOURCE")
    String activitySource

    /**
     * This will optionally store non LUMINIS LDAP user mapping for Banner.
     */
    @Column(name = "GOBTPAC_LDAP_USER")
    String ldapUserMapping

    /**
     * Random value used in generation of PIN Hash.
     */
    @Column(name = "GOBTPAC_SALT")
    String salt

    /**
     * This field identifies the system user last updating this record.9.x
     * **lastModifiedBy User is always set back to the original user by API
     */
    @Column(name = "GOBTPAC_USER_ID")
    String lastModifiedBy

    /**
     * Date on which the record was last added or updated.
     */
    @Column(name = "GOBTPAC_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * DATA SOURCE: Source system that created or updated the row
     */
    @Column(name = "GOBTPAC_DATA_ORIGIN")
    String dataOrigin


    public static readonlyProperties = ['pidm']


    public String toString() {
        """ThirdPartyAccess[
                            id=$id,
                            version=$version,
                            pidm=$pidm,
                            pinDisabledIndicator=$pinDisabledIndicator,
                            termsOfUsageIndicator=$termsOfUsageIndicator,
                            personalIdentificationNumber=$personalIdentificationNumber,
                            externalUser=$externalUser,
                            pinResetQuestion=$pinResetQuestion,
                            pinResetResponse=$pinResetResponse,
                            activitySource=$activitySource,
                            ldapUserMapping=$ldapUserMapping,
                            salt=$salt,
                            lastModified=$lastModified,
                            lastModifiedBy=$lastModifiedBy,
                            dataOrigin=$dataOrigin]"""
    }


    static constraints = {
        pidm(nullable: false, maxsize: 22)
        pinDisabledIndicator(nullable: false, maxsize: 1)
        termsOfUsageIndicator(nullable: false, maxsize: 1)
        personalIdentificationNumber(nullable: true, maxsize: 256)
        externalUser(nullable: true, maxsize: 30)
        pinResetQuestion(nullable: true, maxsize: 90)
        pinResetResponse(nullable: true, maxsize: 30)
        activitySource(nullable: true, maxsize: 8)
        ldapUserMapping(nullable: true, maxsize: 255)
        salt(nullable: true, maxsize: 128)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }


}
