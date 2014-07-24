/*******************************************************************************
 Copyright 2013 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall



import javax.persistence.*

/**
 * Integration configuration rules table
 */

@Entity
@Table(name = "GV_GORICCR")
class IntegrationConfiguration implements Serializable {

    /**
     * Surrogate ID for GORICCR
     */
    @Id
    @Column(name = "GORICCR_SURROGATE_ID")
    @SequenceGenerator(name = "GORICCR_SEQ_GEN", allocationSize = 1, sequenceName = "GORICCR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GORICCR_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORICCR
     */
    @Version
    @Column(name = "GORICCR_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Most current date the record was created or changed.
     */
    @Column(name = "GORICCR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: Oracle user ID of the user who changed the record.
     */
    @Column(name = "GORICCR_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the row.
     */
    @Column(name = "GORICCR_DATA_ORIGIN")
    String dataOrigin

    /**
     * PROCESS CODE: The integration configuration setting process code.
     */
    @Column(name= "GORICCR_SQPR_CODE")
    String processCode

    /**
     * SETTING NAME: The integration configuration setting name.
     */
    @Column(name= "GORICCR_ICSN_CODE")
    String settingName

    /**
     * VALUE: Value of configuration setting.
     */
    @Column(name= "GORICCR_VALUE")
    String value

    /**
     * VALUE DESCRIPTION: Description of the value entered for the given setting.
     */
    @Column(name= "GORICCR_VALUE_DESC")
    String valueDescription

    /**
     * SEQUENCE NUMBER: Ordinal number used when order of configuration settings is important.
     */
    @Column(name= "GORICCR_SEQ_NO")
    Integer sequenceNumber

    /**
     * TRANSLATION VALUE: A value that is the technical equivalent of the value in the VALUE field.
     */
    @Column(name= "GORICCR_TRANSLATION_VALUE")
    String translationValue

    public String toString() {
        """IntegrationConfigurationRule[
					id=$id, 
					version=$version,
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin,
                    processCode=$processCode,
                    settingName=$settingName,
                    value=$value,
                    valueDescription=$valueDescription,
                    sequenceNumber=$sequenceNumber,
                    translationValue=$translationValue"""
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof IntegrationConfiguration)) return false
        IntegrationConfiguration that = (IntegrationConfiguration) o
        if (id != that.id) return false
        if (version != that.version) return false
        if (lastModified != that.lastModified) return false
        if (lastModifiedBy != that.lastModifiedBy) return false
        if (dataOrigin != that.dataOrigin) return false
        if (processCode != that.processCode) return false
        if (settingName != that.settingName) return false
        if (value != that.value) return false
        if (valueDescription != that.valueDescription) return false
        if (sequenceNumber != that.sequenceNumber) return false
        if (translationValue != that.translationValue) return false
        return true
    }


    int hashCode() {
        int result
        result = (id != null ? id.hashCode() : 0)
        result = 31 * result + (version != null ? version.hashCode() : 0)
        result = 31 * result + (lastModified != null ? lastModified.hashCode() : 0)
        result = 31 * result + (lastModifiedBy != null ? lastModifiedBy.hashCode() : 0)
        result = 31 * result + (dataOrigin != null ? dataOrigin.hashCode() : 0)
        result = 31 * result + (processCode != null ? processCode.hashCode() : 0)
        result = 31 * result + (settingName != null ? settingName.hashCode() : 0)
        result = 31 * result + (value != null ? value.hashCode() : 0)
        result = 31 * result + (valueDescription != null ? valueDescription.hashCode() : 0)
        result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0)
        result = 31 * result + (translationValue != null ? translationValue.hashCode() : 0)
        return result
    }


    static constraints = {
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
        processCode(nullable: false, maxSize: 30)
        settingName(nullable: false, maxSize: 30)
        value(nullable: false, maxSize: 200)
        valueDescription(nullable: true, maxSize: 60)
        sequenceNumber(nullable: true)
        translationValue(nullable: true, maxSize: 200)
    }

    //Read Only fields that should be protected against update
    public static readonlyProperties = []

}
