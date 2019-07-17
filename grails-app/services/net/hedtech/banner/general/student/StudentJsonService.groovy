/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.student

import grails.gorm.transactions.Transactional

@Transactional
class StudentJsonService {

    def fetchJsonData(Long id) {
        return StudentJson.fetchJsonData(id)
    }
}
