/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.antlr.runtime.tree.CommonTree
import org.stringtemplate.v4.ST
import org.stringtemplate.v4.STGroup

class CommunicationTemplateService extends ServiceBase {
    def dataFieldNames = []


    def preCreate( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)


        if (template.getName() == null)
            throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) {
            throw new ApplicationException( CommunicationTemplate, "@@r1:templateExists@@" + template.name + " name@@" )
        }
    }


    def preUpdate( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)

        if (template.getName() == null)
            throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.existsAnotherNameFolder( template.id, template.name, template.folder.name ))
            throw new ApplicationException( CommunicationFolder, "@@r1:not.unique.message:" + template.name + " name@@" )
    }

    /**
     *  Extracts all parameter strings delimited by $. These can be either $foo.bar$ or just $foo$, will extract foo.
     * @param template statement
     * @return set of unique string variables found in the template string
     */
    List<String> extractTemplateVariables( String statement ) {

        final int ID = 25 // This is the ST constant for an ID token
        char delimiter = '$'
        /* TODO: get a listener working so  you can trap rendering errors */
        STGroup group = new STGroup( delimiter, delimiter )
        CommunicationStringTemplateErrorListener errorListener = new CommunicationStringTemplateErrorListener()
        group.setListener( errorListener )
        group.defineTemplate( "foo", statement )
        ST st = new org.stringtemplate.v4.ST( statement, delimiter, delimiter );

        /* Each chunk of the ast is returned by getChildren, then examineNodes recursively walks
        down the branch looking for ID tokens to place in the global dataFieldNames */

        st.impl.ast.getChildren().each {
            if (it != null) {
                CommonTree child = it as CommonTree
                examineNodes( child )
            }
        }

        dataFieldNames.unique( false )

    }


    def examineNodes( CommonTree treeNode ) {
        final int ID = 25

        if (treeNode) {
            //println "token type = " + treeNode.getToken().getType() + " text= " + treeNode.getToken().getText()
            if (treeNode.getToken().getType() == ID) {
                //println "ChildIndex= " + treeNode.childIndex + "string= " + treeNode.toStringTree()
                dataFieldNames.add( treeNode.toString() )
            }
            treeNode.getChildren().each {
                CommonTree nextNode = it as CommonTree
                examineNodes( nextNode )
            }

        }
    }
}


