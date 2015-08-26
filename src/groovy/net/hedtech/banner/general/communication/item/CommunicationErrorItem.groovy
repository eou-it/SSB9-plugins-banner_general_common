package net.hedtech.banner.general.communication.item

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.CommunicationErrorCode

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table
import javax.persistence.Version

/**
 * Communication Error Item - Details of a communication that had to be sent to a recipient and an error encountered in the process.
 */
@Entity
@EqualsAndHashCode
@ToString
@Table(name = "gvq_error_log_detail")
@NamedQueries(value = [
        @NamedQuery( name = "CommunicationErrorItem.fetchByReferenceId",
                query = """ FROM CommunicationErrorItem item
                    WHERE item.referenceId = :referenceId_ """
        )
])
class CommunicationErrorItem implements Serializable {

    /**
     * Surrogate Id of the corresponding group send record
     */
    @Id
    @Column(name = "REFERENCE_ID")
    String referenceId

    /**
     * VERSION: Optimistic lock token.
     */
    @Version
    @Column(name = "VERSION")
    Long version

    /**
     * The name of the communication job
     */
    @Column(name = "job_name")
    String jobName

    /**
     * The error code
     */
    @Column(name = "error_code")
    CommunicationErrorCode errorCode

    /**
     * The error text or stacktrace
     */
    @Column(name = "error_text")
    String errorText

    public static fetchByReferenceId(String referenceId) {
        def results
        CommunicationErrorItem.withSession { session ->
            results = session.getNamedQuery( 'CommunicationErrorItem.fetchByReferenceId' )
                    .setParameter( 'referenceId_', referenceId )
                    .list()[0]
        }
        return results
    }
}
