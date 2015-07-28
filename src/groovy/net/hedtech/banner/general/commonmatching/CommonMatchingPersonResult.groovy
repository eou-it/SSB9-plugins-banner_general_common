/*******************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.commonmatching

import javax.persistence.*

@Entity
@Table(name = "GVQ_GOVCMRT")
@NamedQueries(value = [
        @NamedQuery(name = "CommonMatchingPersonResult.fetchAllMatchResults",
                query = """FROM CommonMatchingPersonResult a
                           WHERE a.resultIndicator = 'M'""")])
class CommonMatchingPersonResult implements Serializable {

    /**
     *    PIDM: The PIDM returned as matched or suspense for the rule as a result of the Common Matching process.
     */
    @Id
    @Column(name = "SPRIDEN_SURROGATE_ID")
    Long id

    /**
     *    PIDM: The PIDM returned as matched or suspense for the rule as a result of the Common Matching process.
     */
    @Column(name = "GOVCMRT_PIDM")
    Integer pidm


    /**
     *      GOVCMRT_SPRIDEN_ID_ROWID       ID ROWID: The ROWID of the spriden record returned for the ID match.
     */
    @Column(name = "GOVCMRT_SPRIDEN_ID_ROWID")
    String idRowid

    /**
     *      GOVCMRT_SPRIDEN_NAME_ROWID     NAME ROWID: The ROWID of the spriden record returned for the NAME match.
     */
    @Column(name = "GOVCMRT_SPRIDEN_NAME_ROWID")
    String nameRowid

    /**
     *    GOVCMRT_SPRADDR_ROWID          ADDRESS ROWID: The ROWID of the spraddr record returned for an address match.
     */
    @Column(name = "GOVCMRT_SPRADDR_ROWID")
    String addressRowid

    /**
     *    GOVCMRT_GOREMAL_ROWID          E-MAIL ROWID: The ROWID of the goremal record returned for an e-mail match.
     */
    @Column(name = "GOVCMRT_GOREMAL_ROWID ")
    String emailRowid

    /**
     *   OVCMRT_GORADID_ROWID          ADDITIONAL ID ROWID: The ROWID of the goradid record returned for an additional ID match.
     */
    @Column(name = "GOVCMRT_GORADID_ROWID")
    String additionalIdRowid

    /**
     *     GOVCMRT_SPRTELE_ROWID          TELEPHONE ROWID: The ROWID of the sprtele record returned for a telephone match
     */
    @Column(name = "GOVCMRT_SPRTELE_ROWID")
    String telephoneRowid

    /**
     *      GOVCMRT_CMSC_CODE              SOURCE CODE: The source code used by the Common Matching process.
     */
    @Column(name = "GOVCMRT_CMSC_CODE")
    String commonMatchingSource

    /**
     *      GOVCMRT_CMSR_PRIORITY_NO       PRIORITY NUMBER: Priority number of rule to be processed.
     */
    @Column(name = "GOVCMRT_CMSR_PRIORITY_NO")
    Integer commonMatchingPriority

    /**
     *     GOVCMRT_RESULT_TYPE            RESULT TYPE: Primary match that generated the result.
     */
    @Column(name = "GOVCMRT_RESULT_TYPE")
    String resultType

    /**
     *     GOVCMRT_MESSAGE                MESSAGE: The results of the Common Matching identifying match conditions that are met or unmet.
     */
    @Column(name = "GOVCMRT_MESSAGE")
    String message

    /**
     *   GOVCMRT_RESULT_IND             RESULT INDICATOR: Match result for the row.
     */
    @Column(name = "GOVCMRT_RESULT_IND")
    String resultIndicator

    /**
     *   GOVCMRT_NAME                   NAME: The name for the spriden ROWID if not null, otherwise the current spriden name for the PIDM returned, needed for sorting.
     */
    @Column(name = "GOVCMRT_NAME")
    String name

    /**
     *   GOVCMRT_ID                     ID: The ID for the spriden ROWID if not null, otherwise the current spriden id for the PIDM returned, needed for sorting.
     */
    @Column(name = "GOVCMRT_ID")
    String bannerId

    @Column(name = "SPRIDEN_DATA_ORIGIN")
    String dataOrigin
    @Column(name="SPRIDEN_LAST_NAME")
    String lastName
    @Column(name="SPRIDEN_FIRST_NAME")
    String firstName
    @Column(name="SPRIDEN_MI")
    String mi



    public static List fetchAllMatchResults(def params = [:]) {

        def pidmsres = []

        pidmsres = CommonMatchingPersonResult.withSession
                { session ->
                    org.hibernate.Query query1 = session.getNamedQuery('CommonMatchingPersonResult.fetchAllMatchResults')
                    def orderBy
                    if (params.sort) {
                        orderBy = " order by a." + params.sort
                    }
                    else {
                        orderBy = " order by a.lastName, a.firstName, a.mi, a.bannerId"
                    }
                    org.hibernate.Query query = session.createQuery(query1.getQueryString() + orderBy)

                    def max
                    def offset
                    if (params.max) {
                        if (params.max instanceof String) max = params.max.toInteger()
                        else max = params.max
                        query.setMaxResults(max)
                    }
                    if (params.offset) {
                        if (params.offset instanceof String) offset = params.offset.toInteger()
                        else offset = params.offset
                        query.setFirstResult(offset)
                    }
                    query.list()
                }

        return pidmsres
    }


    @Override
    public String toString() {
        return "CommonMatchingPersonResult{" +
                "id='" + id + '\'' +
                ", pidm='" + pidm + '\'' +
                ", idRowid='" + idRowid + '\'' +
                ", nameRowid='" + nameRowid + '\'' +
                ", addressRowid='" + addressRowid + '\'' +
                ", emailRowid='" + emailRowid + '\'' +
                ", additionalIdRowid='" + additionalIdRowid + '\'' +
                ", telephoneRowid='" + telephoneRowid + '\'' +
                ", commonMatchingSource='" + commonMatchingSource + '\'' +
                ", commonMatchingPriority=" + commonMatchingPriority +
                ", resultType='" + resultType + '\'' +
                ", message='" + message + '\'' +
                ", resultIndicator='" + resultIndicator + '\'' +
                ", name='" + name + '\'' +
                ", matchedId='" + matchedId + '\'' +
                ", dataOrigin='" + dataOrigin + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", mi='" + mi + '\'' +
                ", bannerId='" + bannerId + '\'' +
                '}';
    }
}
