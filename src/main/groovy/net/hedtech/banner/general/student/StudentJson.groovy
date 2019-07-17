/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.student

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.persistence.*

@NamedQueries(value = [
        @NamedQuery(name = "StudentJson.fetchJsonData",
                query = """select s.jsonData 
                            from StudentJson s 
                            where s.id = :surrogateId
                            """)
])

/**
 * To fetch JSON data from 8x packages.
 */

@Entity
@Table(name = "GCBJSON")
@ToString(includeNames = true, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
public class StudentJson implements Serializable {
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
     * Fetch JSON Data insert by 8x package functions
     * @param surrogateId
     * @return
     */
   static def fetchJsonData(Long surrogateId) {

       def result = StudentJson.withSession { session ->
           session.getNamedQuery('StudentJson.fetchJsonData')
                   .setLong('surrogateId', surrogateId)
                   .uniqueResult()
       }
       return result
   }


}
