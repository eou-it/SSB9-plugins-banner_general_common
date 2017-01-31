/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.parameter

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.CommunicationCommonUtility
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version
import org.hibernate.FlushMode
import org.hibernate.criterion.Order

@Entity
@EqualsAndHashCode
@ToString
@Table(name = "GCRPARM")
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationParameter.fetchByName",
                query = """ FROM CommunicationParameter a
                    WHERE lower(a.name) = lower(:name)"""),
        @NamedQuery(name = "CommunicationParameter.existsAnotherName",
                query = """select a.name  FROM CommunicationParameter a
                    WHERE  lower(a.name) = lower(:name)
                    AND   a.id <> :id""")
])
class CommunicationParameter implements Serializable {

    /**
     * SURROGATE ID: Immutable unique key.
     */
    @Id
    @Column(name = "GCRPARM_SURROGATE_ID")
    @SequenceGenerator(name = "GCRPARM_SEQ_GEN", allocationSize = 1, sequenceName = "GCRPARM_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCRPARM_SEQ_GEN")
    Long id

    /**
     * Name of the parameter.
     */
    @Column(name = "GCRPARM_NAME")
    String name

    /**
     * Title of the parameter.
     */
    @Column(name = "GCRPARM_TITLE")
    String title

    /**
     * Type of the parameter.
     */
    @Column(name = "GCRPARM_TYPE")
    @Enumerated(EnumType.STRING)
    CommunicationParameterType type

    /**
     * Indicates if the parameter was created through the seeded data set and should not be deleted or modified in any way.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRPARM_SYSTEM_IND")
    Boolean systemIndicator = false
    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "GCRPARM_VERSION")
    Long version

    /**
     * ACTIVITY DATE: Date that record was created or last updated.
     */
    @Column(name = "GCRPARM_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     * USER ID: The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCRPARM_USER_ID")
    String lastModifiedBy

    /**
     * DATA ORIGIN: Source system that created or updated the data.
     */
    @Column(name = "GCRPARM_DATA_ORIGIN")
    String dataOrigin

    static constraints = {
        name(nullable: false, maxSize: 255)
        title(nullable: false, maxSize: 255)
        type(nullable: false, maxSize: 255)
        systemIndicator(nullable:false)
        lastModified(nullable: true)
        lastModifiedBy(nullable: true, maxSize: 30)
        dataOrigin(nullable: true, maxSize: 30)
    }

    // Read Only fields that should be protected against update
    public static readonlyProperties = ['id']

    public static CommunicationParameter fetchByName(String parameterName) {
        def parameter
        CommunicationParameter.withSession { session ->
            parameter = session.getNamedQuery('CommunicationParameter.fetchByName').setString('name', parameterName).list()[0]
        }
        return parameter
    }

    public static Boolean existsAnotherName(Long parameterId, String parameterName) {

        def parameter
        CommunicationParameter.withSession { session ->
            session.setFlushMode(FlushMode.MANUAL);
            try {
                parameter = session.getNamedQuery('CommunicationParameter.existsAnotherName')
                        .setString('name', parameterName)
                        .setLong('id', parameterId).list()[0]
            } finally {
                session.setFlushMode(FlushMode.AUTO)
            }
        }
        return (parameter != null)
    }

    public static findByNameWithPagingAndSortParams(filterData, pagingAndSortParams) {
        def descdir = pagingAndSortParams?.sortDirection?.toLowerCase() == 'desc'

        def queryCriteria = CommunicationParameter.createCriteria()
        def results = queryCriteria.list(max: pagingAndSortParams.max, offset: pagingAndSortParams.offset) {
            ilike("name", CommunicationCommonUtility.getScrubbedInput(filterData?.params?.name))
            order((descdir ? Order.desc(pagingAndSortParams?.sortColumn) : Order.asc(pagingAndSortParams?.sortColumn)).ignoreCase())
        }
        return results
    }
}
