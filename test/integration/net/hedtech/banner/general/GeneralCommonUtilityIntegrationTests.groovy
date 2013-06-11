/** *****************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.general

import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH

import net.hedtech.banner.general.system.SdaCrosswalkConversion
import net.hedtech.banner.testing.BaseIntegrationTestCase

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
        assertNotNull sdaxList["SCHBYDATEWEBREG"]

    }


    void testTwoThreadsGtvsdax() {
        Closure schedByDate = {
            def schByDate = GeneralCommonUtility.gtvsdaxForSession('SCHBYDATE', 'WEBREG')

        }
        Closure enrollmentDisplay = {
            def enrollmentDisplay = GeneralCommonUtility.gtvsdaxForSession('DISPENROLL', 'WEBREG')[0]

        }

        Thread registrationThread1 = new Thread(schedByDate as Runnable)
        Thread registrationThread2 = new Thread(enrollmentDisplay as Runnable)
        registrationThread1.start()
        Thread.yield()
        registrationThread2.start()
        registrationThread1.join()
        registrationThread2.join()
        registrationThread1.interrupt()
        registrationThread2.interrupt()

        def sdaxList = SCH.servletContext.getAttribute("gtvsdax")
        assertEquals 2, sdaxList.size()
        assertNotNull sdaxList['SCHBYDATEWEBREG']
        assertNotNull sdaxList['DISPENROLLWEBREG']
    }
}
