/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import org.stringtemplate.v4.STErrorListener
import org.stringtemplate.v4.misc.ErrorType
import org.stringtemplate.v4.misc.STMessage

class CommunicationStringTemplateErrorListener implements STErrorListener {

    @Override
    void compileTimeError( STMessage stMessage ) {
        throw new ApplicationException( CommunicationTemplateService, stMessage.toString() );
    }


    @Override
    void runTimeError( STMessage stMessage ) {
        if (msg.error != ErrorType.NO_SUCH_PROPERTY) { // ignore these
            throw new ApplicationException( CommunicationTemplateService, stMessage.toString() );
        }
    }


    @Override
    void IOError( STMessage stMessage ) {
        throw new ApplicationException( CommunicationTemplateService, stMessage.toString() );
    }


    @Override
    void internalError( STMessage stMessage ) {
        throw new ApplicationException( CommunicationTemplateService, stMessage.toString() );
    }


    public void error( String s ) { error( s, null ); }


    public void error( String s, Throwable e ) {
        System.err.println( s );
        if (e != null) {
            throw new Exception( msg.toString() );
        }
    }
}


