/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.groupsend

import groovy.transform.EqualsAndHashCode
import net.hedtech.banner.general.communication.template.CommunicationTemplate

import javax.persistence.*

/**
 * A group send is a template based communication job directed towards the pidms in a population.
 *
 */
@Entity
@DiscriminatorValue("GROUP_SEND")
@EqualsAndHashCode
class CommunicationGroupSend extends CommunicationJob {

    @JoinColumn(name="GCBCJOB_TEMPLATE_KEY" )
    @ManyToOne( fetch = FetchType.LAZY )
    CommunicationTemplate template;
}

