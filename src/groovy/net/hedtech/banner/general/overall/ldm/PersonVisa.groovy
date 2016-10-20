/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm;

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

@Entity
@Table(name = "GVQ_PERSON_VISAS")
@ToString(includeFields = true, includeNames = true)
@EqualsAndHashCode
public class PersonVisa implements Serializable {

    @Id
    @Column(name = "GORVISA_GUID", nullable = true)
    String id

    @Column(name = "PERSON_GUID", nullable = false)
    String personGuid

    @Column(name = "NON_RES_IND", nullable = true)
    String nonResInd

    @Column(name = "VISA_TYPE_GUID", nullable = false)
    String visaTypeGuid

    @Temporal(TemporalType.DATE)
    @Column(name = "GORVISA_VISA_ISSUE_DATE", nullable = true)
    Date visaIssueDate

    @Column(name = "GORVISA_VISA_NUMBER", nullable = true)
    String visaNumber

    @Temporal(TemporalType.DATE)
    @Column(name = "GORVISA_VISA_REQ_DATE", nullable = true)
    Date visaRequestDate

    @Temporal(TemporalType.DATE)
    @Column(name = "GORVISA_VISA_EXPIRE_DATE", nullable = true)
    Date visaExpireDate


    static constraints = {
        id(nullable: true, maxSize: 36)
        personGuid(nullable: false, maxSize: 36)
        nonResInd(nullable: true, maxSize: 1)
        visaTypeGuid(nullable: false, maxSize: 36)
        visaIssueDate(nullable: true)
        visaNumber(nullable: true, maxSize: 18)
        visaRequestDate(nullable: true)
        visaExpireDate(nullable: true)
    }

}