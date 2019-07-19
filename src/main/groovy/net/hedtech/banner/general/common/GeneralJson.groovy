/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

@NamedQueries(value = [
        @NamedQuery(name = "GeneralJson.fetchJsonData",
                query = """ FROM GeneralJson s 
                            where s.id = :surrogateId
                            """)
])

/**
 * GCBJSON holds the JSON clob returned by 8x procedure call
 */

@Entity
@Table(name = "GCBJSON")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
public class GeneralJson implements Serializable {
    @Id
    @Column(name = "GCBJSON_SURROGATE_ID")
    Long id

    /**
     * JSON Data as Clob
     */
    @Column(name = "GCBJSON_JSON_DATA")
    @Lob
    String jsonData

    /**
     * User who inserted the JSON
     */
   // @Column(name = "GCBJSON_USER_ID")
   // String lastModifiedBy

    /**
     * Last activity date
     */
    //@Column(name = "GCBJSON_ACTIVITY_DATE")
    //Date lastModified

    /**
     * Version of the JSON record
     */
    @Version
    @Column(name = "GCBJSON_VERSION")
    Long version

    /**
     * Data Origin column for GCBJSON
     */
    //@Column(name = "GCBJSON_DATA_ORIGIN")
    //String dataOrigin

    /**
     * VPDI CODE: MULTI-ENTITY PROCESSING CODE.
     */
    //@Column(name = "GCBJSON_VPDI_CODE")
    //String vpdiCode

    /**
     * Fetch JSON Data insert by 8x package functions
     * @param surrogateId
     * @return
     */
    static def fetchJsonData(Long surrogateId) {

        def result = GeneralJson.withSession { session ->
            session.getNamedQuery('GeneralJson.fetchJsonData')
                    .setLong('surrogateId', surrogateId)
                    .uniqueResult()
        }
        return result
    }


}
