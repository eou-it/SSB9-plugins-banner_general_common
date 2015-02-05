/*********************************************************************************
 Copyright 2015 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication.field

import net.hedtech.banner.exceptions.ApplicationException
import net.hedtech.banner.general.communication.template.CommunicationTemplate
import org.stringtemplate.v4.STErrorListener
import org.stringtemplate.v4.misc.ErrorType
import org.stringtemplate.v4.misc.STMessage

/**
 * Call back functions for handling errors and missing attribute in the communication field formatter
 */
public class CommunicationFieldMissingPropertyCapture implements STErrorListener {
    public HashSet<String> missingProperties = new HashSet<String>()

    @Override
    public void compileTimeError(STMessage stMessage) {
        throw new ApplicationException( CommunicationField, "@@r1:compileErrorDuringParsing:${stripSurprise( stMessage.arg )}@@" );
    }

    @Override
    public void runTimeError(STMessage stMessage) {
        if ( stMessage.error == ErrorType.NO_SUCH_ATTRIBUTE ) { // ignore these
            missingProperties.add(stMessage.arg);
        } else {
            throw new ApplicationException( CommunicationField, "@@r1:runtimeErrorDuringParsing:${stMessage.arg}@@" );
        }
    }

    @Override
    public void IOError(STMessage stMessage) {
        throw new ApplicationException( CommunicationField, "@@r1:ioErrorDuringParsing:${stMessage.arg}@@" );
    }

    @Override
    public void internalError(STMessage stMessage) {
        throw new ApplicationException( CommunicationField, "@@r1:internalErrorDuringParsing:${stMessage.arg}@@" );
    }


    private String stripSurprise( String s ) {
        final CharSequence surpriseString = "' came as a complete surprise to me"
        if (s) {
            return s.replace( surpriseString, "'" )
        } else {
            return s
        }
    }

}