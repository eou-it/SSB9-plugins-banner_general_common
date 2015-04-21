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
            ['CDM.Level.STVLEVL']
        )
        resourceMap << new ApiResource (
            'buildings',
            1,
            '/buildings/',
            ['CDM.Building.SLBBLDG',
             'CDM.Building.SLBRDEF']
        )
        resourceMap << new ApiResource (
            'ethnicities',
            1,
            '/ethnicities/',
            ['CDM.Ethnicity.STVETHN']
        )
        resourceMap << new ApiResource (
            'grade-schemes',
            1,
            '/grade-schemes/',
            ['CDM.GradeMode.STVGMOD']
        )
        resourceMap << new ApiResource (
            'instructional-methods',
            1,
            '/instructional-methods/',
            ['CDM.ScheduleType.STVSCHD']
        )
        resourceMap << new ApiResource (
            'marital-statuses',
            1,
            '/marital-statuses/',
            ['CDM.MaritalStatus.STVMRTL']
        )
        resourceMap << new ApiResource( 
            'organizations',
            1,
            '/organizations/',
            ['CDM.College.STVCOLL']
        )
        resourceMap << new ApiResource( 
            'persons',
            1,
            '/persons/',
            ['CDM.Person.GOREMAL',
            'CDM.Person.GORPRAC',
            'CDM.Person.SPBPERS',
            'CDM.Person.SPRADDR',
            'CDM.Person.SPRIDEN',
            'CDM.Person.SPRTELE',
            'CDM.Faculty.SIBINST']
        )
        resourceMap << new ApiResource( 
            'races',
            1,
            '/races/',
            ['CDM.Race.GORRACE']
        )
        resourceMap << new ApiResource( 
            'restriction-types',
            1,
            '/restriction-types/',
            ['CDM.HoldType.STVHLDD']
        )
        resourceMap << new ApiResource( 
            'rooms',
            1,
            '/rooms/',
            ['CDM.Room.SLBRDEF']
        )
        resourceMap << new ApiResource( 
            'sites',
            1,
            '/sites/',
            ['CDM.Campus.STVCAMP',
             'CDM.Campus.SLBBLDG']
        )
        resourceMap << new ApiResource( 
            'subjects',
            1,
            '/subjects/',
            ['CDM.Subject.STVSUBJ']
        )
        resourceMap << new ApiResource(
                'section-registrations',
                1,
                '/section-registrations/',
                null
        )
    }
}
