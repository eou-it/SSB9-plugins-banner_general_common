/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.person.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.general.system.ldm.v4.EmailTypeDetails

/**
 * Person Email Decorator
 */
@EqualsAndHashCode(includeFields = true)
@ToString(includeNames = true, includeFields = true)
class EmailV6 {

   EmailTypeDetails type
   String preference
   String address

}

