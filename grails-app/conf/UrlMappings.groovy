/*********************************************************************************
 Copyright 2009-2013 Ellucian Company L.P. and its affiliates.
  **********************************************************************************/



 /**
  * Specifies all of the URL mappings supported by the application.
  */
class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
