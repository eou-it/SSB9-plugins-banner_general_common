/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class ErpApiConfig {
    String erpname
    String username
    String password
    List<ApiResource> resourceMap

    ErpApiConfig () {
        resourceMap = []
        resourceMap << new ApiResource(
                'courses',
                1,
                '/courses/',
                null
        )
        resourceMap << new ApiResource(
                'sections',
                1,
                '/sections/',
                null
        )
        resourceMap << new ApiResource(
            'academic-levels',
            1,
            '/academic-levels/',
            ['HEDM.Level.STVLEVL']
        )
        resourceMap << new ApiResource (
            'buildings',
            1,
            '/buildings/',
            ['HEDM.Building.SLBBLDG',
             'HEDM.Building.SLBRDEF']
        )
        resourceMap << new ApiResource (
            'ethnicities',
            1,
            '/ethnicities/',
            ['HEDM.Ethnicity.STVETHN']
        )
        resourceMap << new ApiResource (
            'grade-schemes',
            1,
            '/grade-schemes/',
            ['HEDM.GradeMode.STVGMOD']
        )
        resourceMap << new ApiResource (
            'instructional-methods',
            1,
            '/instructional-methods/',
            ['HEDM.ScheduleType.STVSCHD']
        )
        resourceMap << new ApiResource (
            'marital-statuses',
            1,
            '/marital-statuses/',
            ['HEDM.MaritalStatus.STVMRTL']
        )
        resourceMap << new ApiResource(
            'organizations',
            1,
            '/organizations/',
            ['HEDM.College.STVCOLL']
        )
        //TODO: changing persons API version to 3. this needs to be reverted to 1.
        resourceMap << new ApiResource(
            'persons',
            3,
            '/persons/',
            ['HEDM.Person.GOREMAL',
            'HEDM.Person.GORPRAC',
            'HEDM.Person.SPBPERS',
            'HEDM.Person.SPRADDR',
            'HEDM.Person.SPRIDEN',
            'HEDM.Person.SPRTELE',
            'HEDM.Faculty.SIBINST']
        )
        resourceMap << new ApiResource(
            'races',
            1,
            '/races/',
            ['HEDM.Race.GORRACE']
        )
        resourceMap << new ApiResource(
            'restriction-types',
            1,
            '/restriction-types/',
            ['HEDM.HoldType.STVHLDD']
        )
        resourceMap << new ApiResource(
            'rooms',
            1,
            '/rooms/',
            ['HEDM.Room.SLBRDEF']
        )
        resourceMap << new ApiResource(
            'sites',
            1,
            '/sites/',
            ['HEDM.Campus.STVCAMP',
             'HEDM.Campus.SLBBLDG']
        )
        resourceMap << new ApiResource(
            'subjects',
            1,
            '/subjects/',
            ['HEDM.Subject.STVSUBJ']
        )
        resourceMap << new ApiResource(
            'academic-periods',
            3,
            '/academic-periods/',
            ['HEDM.AcademicPeriods.STVACYR',
             'HEDM.AcademicPeriods.SOBTERM',
             'HEDM.AcademicPeriods.SOBPTRM']
        )
        resourceMap << new ApiResource(
            'instructional-platforms',
            3,
            '/instructional-platforms/',
            ['HEDM.InstructionalPlatforms.GORINTG']
        )
        resourceMap << new ApiResource(
            'person-filters',
            3,
            '/person-filters/',
            ['HEDM.PersonFilters.GLBSLCT']
        )
        resourceMap << new ApiResource(
                'section-registrations',
                1,
                '/section-registrations/',
                null
        )
    }
}
