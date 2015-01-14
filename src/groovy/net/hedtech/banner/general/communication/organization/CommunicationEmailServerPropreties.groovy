package net.hedtech.banner.general.communication.organization

/**
 * Created by mbrzycki on 1/14/15.
 */
class CommunicationEmailServerPropreties {
    Long id
    String smtpHost
    String smtpPort
//    String transportProtocol // smtp, pop3, imap, etc
    String securityProtocol // none, ssl, etc.
}
