/*********************************************************************************
 Copyright 2017 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.communication.template

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.communication.item.CommunicationChannel
import net.hedtech.banner.general.communication.parameter.CommunicationParameterType

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.NamedQueries
import javax.persistence.NamedQuery
import javax.persistence.Table

@Entity
@Table(name = "GVQ_GCBTPPM")
@EqualsAndHashCode
@ToString
@NamedQueries(value = [
        @NamedQuery(name = "CommunicationTemplateParameterView.fetchByTemplateId",
                query = """ FROM CommunicationTemplateParameterView a
                            WHERE  a.templateId = :templateId
                        """)
])
class CommunicationTemplateParameterView implements Serializable {

    /**
     * ID of the Communication Template
     */
    @Id
    @Column(name = "template_id")
    Long templateId

    /*
     *  Name of the communication template.
    */
    @Column(name = "template_name")
    String templateName

    /*
     * The communication channel for the template
     */
    @Column(name = "communication_channel")
    @Enumerated(EnumType.STRING)
    CommunicationChannel communicationChannel

    /**
     * ID of the Communication Parameter
     */
    @Id
    @Column(name = "parameter_id")
    Long parameterId

    /**
     * Name of the parameter.
     */
    @Column(name = "parameter_name")
    String parameterName

    /**
     * Title of the parameter.
     */
    @Column(name = "parameter_title")
    String parameterTitle

    /**
     * Type of the parameter.
     */
    @Column(name = "parameter_type")
    @Enumerated(EnumType.STRING)
    CommunicationParameterType parameterType

    /**
     * Fetch all the communication parameters for a given template
     * @param templateId the Template ID
     * @return List of parameters for the given template
     */
    public static List<CommunicationTemplateParameterView> fetchByTemplateId(Long templateId) {

        def params =
                CommunicationTemplateParameterView.withSession { session ->
                    session.getNamedQuery('CommunicationTemplateParameterView.fetchByTemplateId')
                            .setLong('templateId', templateId)
                            .list()
        }
        return params
    }
}
