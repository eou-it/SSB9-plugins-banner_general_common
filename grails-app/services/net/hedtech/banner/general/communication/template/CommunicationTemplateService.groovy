/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */

package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.folder.CommunicationFolder
import net.hedtech.banner.service.ServiceBase
import org.stringtemplate.v4.ST

import java.util.regex.Pattern

class CommunicationTemplateService extends ServiceBase {

    def preCreate( domainModelOrMap ) {
        CommunicationTemplate template = (domainModelOrMap instanceof Map ? domainModelOrMap?.domainModel : domainModelOrMap) as CommunicationTemplate
        template.folder = (template.folder ?: domainModelOrMap.folder)


        if (template.getName() == null)
            throw new ApplicationException( CommunicationTemplate, "@@r1:nameCannotBeNull@@" )

        if (template.fetchByTemplateNameAndFolderName( template.name, template.folder.name )) {
            throw new ApplicationException( CommunicationTemplate, "@@r1:not.unique.message:" + template.name + " name@@" )
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
//        Pattern pattern = Pattern.compile( /\$(\w*)\$/ );
        Pattern pattern = Pattern.compile( /\$(\w+)[.]|(\w+?)\$/ );
        def List<String> runTimeParms = []
        def matcher = pattern.matcher( statement )

        while (matcher.find()) {
            runTimeParms << matcher.group( 2 )

        }
        runTimeParms.removeAll( Collections.singleton( null ) );
        runTimeParms.unique( false )
    }
/*
    final int ID = 25


    char delimiter = '$'
    ST st = new org.stringtemplate.v4.ST( statement, delimiter, delimiter );

    def dataFieldNames = []



    st.impl.ast.getChildren ( ).each {

    if ( it != null ) {
        CommonTree child = it as CommonTree
        if (child.toString().equals( "EXPR" )) {
            if (child.getChildCount() == 1) {
                CommonTree expressionChild = child.getChild( 0 )
                if (expressionChild.getToken().getType() == ID) {
                    dataFieldNames.add( expressionChild.toString() )
                } else if (expressionChild.toString().equals( "PROP" )) {
                    if (expressionChild.getChildCount() == 2) {
                        dataFieldNames.add(
                                expressionChild.getChild( 0 ).toString() +
                                        "." +
                                        expressionChild.getChild( 1 ).toString() )
                    }
                }
            }
        }
    }
}

System.out.println( dataFieldNames )

*/

}
