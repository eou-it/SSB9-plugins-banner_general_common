/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general

import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

class GeneralCommonUtilityIntegrationTests extends BaseIntegrationTestCase {


    protected void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    protected void tearDown() {
        super.tearDown()
    }


    void testGtvsdaxSet() {
        SCH.servletContext.removeAttribute("gtvsdax")

        def gtvsdaxValue = SdaCrosswalkConversion.fetchAllByInternalAndInternalGroup('SCHBYDATE', 'WEBREG')[0]?.external
        def schByDate = GeneralCommonUtility.gtvsdaxForSession('SCHBYDATE', 'WEBREG')
        assertEquals schByDate, gtvsdaxValue
        def sdaxList = SCH.servletContext.getAttribute("gtvsdax")
        assertNotNull sdaxList
        assertNotNull sdaxList.find { it.key == "SCHBYDATEWEBREG"}

    }
}
