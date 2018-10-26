/*******************************************************************************
 Copyright 2013-2018 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */
package net.hedtech.banner.general.overall

import groovy.transform.EqualsAndHashCode

import net.hedtech.banner.general.system.AddressType
import net.hedtech.banner.general.system.DirectoryOption
import net.hedtech.banner.general.system.TelephoneType

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version
import javax.persistence.GenerationType
import javax.persistence.SequenceGenerator
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne


/**
 * Directory Address Table
 */
@Entity
@Table(name = "GORDADD")
@EqualsAndHashCode(includeFields = true)
@NamedQueries(value = [
@NamedQuery(name = "DirectoryAddress.fetchByDirectoryOptionOrderByPriority",
        query = """FROM  DirectoryAddress a
	       WHERE a.directoryOption.code = :directoryOption ORDER BY a.priorityNumber""")
])
class DirectoryAddress implements Serializable {

    /**
     * Surrogate ID for GORDADD
     */
    @Id
    @Column(name="GORDADD_SURROGATE_ID")
    @SequenceGenerator(name ="GORDADD_SEQ_GEN", allocationSize =1, sequenceName  ="GORDADD_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy =GenerationType.SEQUENCE, generator ="GORDADD_SEQ_GEN")
    Long id

    /**
     * Optimistic lock token for GORDADD
     */
    @Version
    @Column(name = "GORDADD_VERSION")
    Long version

    /**
     * PRIORITY NUMBER: Priority Number.
     */
    @Column(name = "GORDADD_PRIORITY_NO")
    Integer priorityNumber

    /**
     * ACTIVITY DATE: Date of last activity on this record.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "GORDADD_ACTIVITY_DATE")
    Date lastModified

    /**
     * Last modified by column for GORDADD
     */
    @Column(name = "GORDADD_USER_ID")
    String lastModifiedBy

    /**
     * Data origin column for GORDADD
     */
    @Column(name = "GORDADD_DATA_ORIGIN")
    String dataOrigin


    /**
     * Foreign Key : FKV_GORDADD_INV_GTVDIRO_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name="GORDADD_DIRO_CODE", referencedColumnName="GTVDIRO_CODE")
    ])
    DirectoryOption directoryOption

    /**
     * Foreign Key : FKV_GORDADD_INV_STVATYP_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name="GORDADD_ATYP_CODE", referencedColumnName="STVATYP_CODE")
    ])
    AddressType addressType

    /**
     * Foreign Key : FKV_GORDADD_INV_STVTELE_CODE
     */
    @ManyToOne
    @JoinColumns([
    @JoinColumn(name="GORDADD_TELE_CODE", referencedColumnName="STVTELE_CODE")
    ])
    TelephoneType telephoneType


    public String toString() {
        """DirectoryAddress[
					id=$id, 
					version=$version, 
					priorityNumber=$priorityNumber, 
					lastModified=$lastModified, 
					lastModifiedBy=$lastModifiedBy, 
					dataOrigin=$dataOrigin, 
					directoryOption=$directoryOption, 
					addressType=$addressType, 
					telephoneType=$telephoneType]"""
    }


    static constraints = {
        priorityNumber(nullable:false, min: -9, max: 9)
        lastModified(nullable:true)
        lastModifiedBy(nullable:true, maxSize:30)
        dataOrigin(nullable:true, maxSize:30)
        directoryOption(nullable:false)
        addressType(nullable:true)
        telephoneType(nullable:true)
    }


    //Read Only fields that should be protected against update
    public static readonlyProperties = [ 'priorityNumber', 'directoryOption' ]


    public static def fetchByDirectoryOptionOrderByPriority(String directoryOption) {
        def directroyAddresses = DirectoryAddress.withSession { session ->
            session.getNamedQuery('DirectoryAddress.fetchByDirectoryOptionOrderByPriority').setString('directoryOption', directoryOption).list()
        }

        return directroyAddresses
    }

}
