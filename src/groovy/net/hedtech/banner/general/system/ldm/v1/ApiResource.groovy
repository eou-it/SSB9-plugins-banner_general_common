/*********************************************************************************
 Copyright 2014-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm.v1

class ApiResource {
    String resource
    String path
    List<String> events
    String apiDeployment
	List<String> rootEntityList

    ApiResource(String resource, String path, List<String> events, String apiDeployment, List<String> rootEntityList = null) {
        this.resource = resource
        this.path = path
        this.events = events
        this.apiDeployment = apiDeployment
		this.rootEntityList = rootEntityList
    }
}
