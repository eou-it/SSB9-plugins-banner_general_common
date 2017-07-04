/*********************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.system

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.testing.BaseIntegrationTestCase
import org.junit.Before
import org.junit.Test

class EducationalInstitutionViewServiceIntegrationTests extends BaseIntegrationTestCase {
    EducationalInstitutionViewService educationalInstitutionViewService

    @Before
    public void setUp() {
        formContext = ['GUAGMNU']
        super.setUp()
    }


    @Test
    void testCreateNotAllowed() {
        shouldFail(ApplicationException) {
            educationalInstitutionViewService.create([EducationalInstitutionView: new EducationalInstitutionView()])
        }
    }

    @Test
    void testUpdateNotAllowed() {
        shouldFail(ApplicationException) {
            educationalInstitutionViewService.update([EducationalInstitutionView: new EducationalInstitutionView()])
        }
    }

    @Test
    void testDeleteNotAllowed() {
        shouldFail(ApplicationException) {
            educationalInstitutionViewService.delete([EducationalInstitutionView: new EducationalInstitutionView()])
        }
    }

    @Test
    void testReadAllowed() {
        EducationalInstitutionView educationalInstitutionView = EducationalInstitutionView.findAll()[0]
        EducationalInstitutionView objReadUsingService = educationalInstitutionViewService.read(educationalInstitutionView.id) as EducationalInstitutionView
        assertNotNull objReadUsingService
        assertEquals(educationalInstitutionView.id, objReadUsingService.id)
    }

    @Test
    void testCount() {
        def totalCount = educationalInstitutionViewService.countByCriteria([:])
        def countByDb=EducationalInstitutionView.count()
        assertEquals totalCount,countByDb
    }

    @Test
    void testFethAllWithParams() {
        List<EducationalInstitutionView> educationalInstitutionViews = educationalInstitutionViewService.fetchAllByCriteria([:],null,null,100,0)
        assertNotNull educationalInstitutionViews
        assertNotNull educationalInstitutionViews[0].id
    }

    @Test
    void testFethAllWithFilterCriteria() {
        List<EducationalInstitutionView> educationalInstitutionViews = educationalInstitutionViewService.fetchAllByCriteria(['type':'secondarySchool'],null,null,100,0)
        assertNotNull educationalInstitutionViews
        educationalInstitutionViews.each {educationalInstitutionView ->
            assertEquals educationalInstitutionView.type,'secondarySchool'
        }
    }

    @Test
    void testFethAllWithSortCriteria() {
        List<EducationalInstitutionView> educationalInstitutionViews = educationalInstitutionViewService.fetchAllByCriteria([:],'type','asc',100,0)
        assertNotNull educationalInstitutionViews
        assertEquals(educationalInstitutionViews.type.sort(), educationalInstitutionViews.type)
    }

    @Test
    void testFethAllInList() {
        List<EducationalInstitutionView> educationalInstitutionViews = educationalInstitutionViewService.fetchAllByCriteria([:])
        assertNotNull educationalInstitutionViews
        assertNotNull educationalInstitutionViews[0].id
    }
}
