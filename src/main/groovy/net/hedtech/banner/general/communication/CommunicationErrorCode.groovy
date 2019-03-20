/*********************************************************************************
 Copyright 2016-2019 Ellucian Company L.P. and its affiliates.
 *********************************************************************************/
package net.hedtech.banner.general.communication

/**
 * An enumeration of error codes for communication jobs and items.
 * These values can never change.
 */
enum CommunicationErrorCode implements Serializable {

    INVALID_DATA_FIELD, //Failure evaluating a data field query
    MISSING_DATA_FIELD, // sql returns no data.
    DATA_FIELD_SQL_ERROR,

    /** Email error codes **/
    EMAIL_SERVER_CONNECTION_FAILED, //Failure connecting to the email server

    EMAIL_SERVER_AUTHENTICATION_FAILED, //Failure authenticating to the email server
    EMAIL_SERVER_USER_NOT_AUTHORIZED,  // Server does not allow this user to have access
    EMAIL_SERVER_AUTHENTICATION_FAILED_UNKNOWN, // Unknown cause to authentication failure
    EMPTY_SENDER_ADDRESS, //No sender email address exists
    INVALID_SENDER_MAILBOX, // Sender mailbox does not exist or is invalid
    INVALID_SENDER_MAILBOX_TYPE, //sender mailbox type not fond or is invalid
    EMPTY_RECIPIENT_ADDRESS, //No recipient email address exists
    INVALID_EMAIL_ADDRESS, //Email address is not valid
    EMPTY_EMAIL_SUBJECT, //Email subject is empty
    EMAIL_CONTENT_TOO_LARGE, //Email content is too large

    EMAIL_SERVER_SETTINGS_NOT_FOUND,
    EMAIL_SERVER_HOST_NOT_FOUND,
    EMAIL_SERVER_PORT_INVALID,
    EMAIL_SERVER_SECURITY_PROTOCOL_INVALID,
    EMAIL_SERVER_TYPE_INVALID,
    INVALID_RECEIVER_ADDRESS, // Email receiver address invalid

    TEMPLATE_NOT_FOUND,
    TO_FIELD_EMPTY,
    SUBJECT_FIELD_EMPTY,
    SERVER_PROPERTIES_NOT_FOUND,
    TEMPLATE_ERROR_UNKNOWN,
    FIELD_REQUIRED,

    /** Mobile Notification error codes **/
    EMPTY_MOBILE_NOTIFICATION_ENDPOINT_URL,
    EMPTY_MOBILE_NOTIFICATION_APPLICATION_NAME,
    EMPTY_MOBILE_NOTIFICATION_APPLICATION_KEY,
    EMPTY_MOBILE_NOTIFICATION_EXTERNAL_USER,
    INVALID_MOBILE_NOTIFICATION_ENDPOINT_URL,
    INVALID_MOBILE_NOTIFICATION_APPLICATION_NAME_OR_KEY,
    UNKNOWN_MOBILE_NOTIFICATION_APPLICATION_ENDPOINT,
    MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_UNKNOWN_HOST,
    MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_HOST_REFUSED,
    MOBILE_NOTIFICATION_APPLICATION_ENDPOINT_SSL_UNVERIFIED,
    MOBILE_NOTIFICATION_POSSIBLE_SEND_ERROR,

    /** Letter error codes **/
    EMPTY_LETTER_TO_ADDRESS,
    EMPTY_LETTER_CONTENT,

    /**  Organization Configuration error codes **/
    ORGANIZATION_NOT_FOUND,
    ORGANIZATION_HOST_NAME_INVALID,
    ORGANIZATION_PORT_NUMBER_INVALID,
    ORGANIZATION_SECURITY_PROTOCOL_NOT_FOUND,
    ORGANIZATION_TYPE_NOT_FOUND,

    /**  Certification errors  **/
    UNKNOWN_CERTIFICATION_ERROR,
    CERTIFICATION_PATH_NOT_FOUND,
    CERTIFICATION_FAILED,

    /** Quartz Scheduler Errors**/
    SCHEDULER_ERROR,

    UNKNOWN_ERROR_EMAIL,
    UNKNOWN_ERROR_MOBILE,
    UNKNOWN_ERROR; //Unknown Error

    /**
     * Returns a set of all CommunicationErrorCode enum values.
     * @return Set<CommunicationErrorCode> the set of CommunicationErrorCode
     */
    public Set<CommunicationErrorCode> set() {
        return EnumSet.range( CommunicationErrorCode.INVALID_DATA_FIELD, CommunicationErrorCode.UNKNOWN_ERROR );
    }
}
