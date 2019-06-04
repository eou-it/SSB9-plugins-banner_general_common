package net.hedtech.banner.general.communication.template

import groovy.util.logging.Slf4j
import net.hedtech.banner.exceptions.ApplicationException
import org.stringtemplate.v4.STErrorListener
import org.stringtemplate.v4.misc.ErrorType
import org.stringtemplate.v4.misc.STMessage

/**
 * Created by mbrzycki on 2/3/15.
 */
@Slf4j
public class CommunicationTemplateMissingPropertyCapture implements STErrorListener {
    public HashSet<String> missingProperties = new HashSet<String>()

    @Override
    public void compileTimeError(STMessage stMessage) {
        log.error(stMessage.cause)
        throw new ApplicationException( CommunicationTemplate, "@@r1:compileErrorDuringParsing:${stripSurprise( stMessage.arg )}@@" );
    }

    @Override
    public void runTimeError(STMessage stMessage) {
        log.error(stMessage.cause)
        if ( stMessage.error == ErrorType.NO_SUCH_ATTRIBUTE ) { // ignore these
            missingProperties.add(stMessage.arg);
        } else {
            throw new ApplicationException( CommunicationTemplate, "@@r1:runtimeErrorDuringParsing:${stMessage.arg}@@" );
        }
    }

    @Override
    public void IOError(STMessage stMessage) {
        log.error(stMessage.cause)
        throw new ApplicationException( CommunicationTemplate, "@@r1:ioErrorDuringParsing:${stMessage.arg}@@" );
    }

    @Override
    public void internalError(STMessage stMessage) {
        log.error(stMessage.cause)
        throw new ApplicationException( CommunicationTemplate, "@@r1:internalErrorDuringParsing:${stMessage.arg}@@" );
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