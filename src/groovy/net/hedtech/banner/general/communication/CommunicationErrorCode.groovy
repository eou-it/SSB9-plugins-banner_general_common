package net.hedtech.banner.general.communication

/**
 * An enumeration of error codes for communication jobs and items.
 * These values can never change.
 */
enum CommunicationErrorCode implements Serializable {

    INVALID_DATA_FIELD, //Failure evaluating a data field query

    /** Email error codes **/
    EMAIL_SERVER_CONNECTION_FAILED, //Failure connecting to the email server
    EMAIL_SERVER_AUTHENTICATION_FAILED, //Failure authenticating to the email server
    EMPTY_SENDER_ADDRESS, //No sender email address exists
    EMPTY_RECIPIENT_ADDRESS, //No recipient email address exists
    INVALID_EMAIL_ADDRESS, //Email address is not valid
    EMPTY_EMAIL_SUBJECT, //Email subject is empty
    EMAIL_CONTENT_TOO_LARGE, //Email content is too large

    /** Mobile Notification error codes **/
    EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL,
    EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME,
    EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY,
    EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER,

    UNKNOWN_ERROR; //Unknown Error

    /**
     * Returns a set of all CommunicationErrorCode enum values.
     * @return Set<CommunicationErrorCode> the set of CommunicationErrorCode
     */
    public Set<CommunicationErrorCode> set() {
        return EnumSet.range( CommunicationErrorCode.INVALID_DATA_FIELD, CommunicationErrorCode.UNKNOWN_ERROR );
    }
}