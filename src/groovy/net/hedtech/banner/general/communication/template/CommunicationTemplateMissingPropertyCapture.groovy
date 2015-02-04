package net.hedtech.banner.general.communication.template

import net.hedtech.banner.exceptions.ApplicationException
import org.stringtemplate.v4.STErrorListener
import org.stringtemplate.v4.misc.ErrorType
import org.stringtemplate.v4.misc.STMessage

/**
 * Created by mbrzycki on 2/3/15.
 */
public class CommunicationTemplateMissingPropertyCapture implements STErrorListener {
    public HashSet<String> missingProperties = new HashSet<String>()

    @Override
    public void compileTimeError(STMessage stMessage) {
        throw new ApplicationException( CommunicationTemplate, "@@r1:compileErrorDuringParsing:${stMessage?.token?.toString()}@@" );
    }

    @Override
    public void runTimeError(STMessage stMessage) {
        if ( stMessage.error == ErrorType.NO_SUCH_ATTRIBUTE ) { // ignore these
            missingProperties.add(stMessage.arg);
        } else {
            throw new ApplicationException( CommunicationTemplate, "@@r1:runtimeErrorDuringParsing:${stMessage?.token?.toString()}@@" );
        }
    }

    @Override
    public void IOError(STMessage stMessage) {
        throw new ApplicationException( CommunicationTemplate, "@@r1:ioErrorDuringParsing:${stMessage.arg}@@" );
    }

    @Override
    public void internalError(STMessage stMessage) {
        throw new ApplicationException( CommunicationTemplate, "@@r1:internalErrorDuringParsing:${stMessage.arg}@@" );
    }
}