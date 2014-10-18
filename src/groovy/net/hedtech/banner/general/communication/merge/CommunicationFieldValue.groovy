/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.merge

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Embeddable


@Embeddable
@EqualsAndHashCode
@ToString
class CommunicationFieldValue implements Serializable {

    @Column(name = "GCRFVAL_VALUE")
    String value

    /**
     * Controls whether any additional escaping is done on the values when mail-merged into templates.  If true, the value will be html-escaped when mail-merged into a template content block that is an html document, if false, it will be merged as-is.
     */
    @Type(type = "yes_no")
    @Column(name = "GCRFVAL_RENDER_AS_HTML")
    Boolean renderAsHtml

}