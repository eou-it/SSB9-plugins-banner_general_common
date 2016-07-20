/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/

package net.hedtech.banner.general.person.ldm

import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before

class NonPersonRoleCompositeServiceIntegrationTests extends BaseIntegrationTestCase {


    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


}
