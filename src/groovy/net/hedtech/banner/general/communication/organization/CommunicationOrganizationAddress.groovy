/*********************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ********************************************************************************* */
package net.hedtech.banner.general.communication.organization

import javax.persistence.*

/**
 * OrganizationAddressConfig.
 /**
 * Contains address configuration information for an organization within the BCM subsystem.
 * The BCM system allows Organization entities to be associated with address information.  Once
 * properly associated, an Organization can then be used as the source for sending BCM communications (on any
 * available channel).  Such communications sent out for an Organization will generate commlogs that default to
 * being 'owned' by the sending organization.  The concept extends to the interaction log subsystem, in that
 * interaction logs must also be 'owned' by a single Organization.
 *
 * The configuration information contained in an OrganizationAddressConfig specifies how messages will be sent on the
 * supported channels, and how resources (such as mailboxes) will be monitored for responses and how those responses
 * will be assigned to Organizations.
 *
 * The current implementation contains addressing information for both letter messages (physical address) and
 * email messages (EmailConfig).  The letter message addressing is simple; the config contains a physical address that
 * can be used in the outgoing letter.
 *
 * Email configuration is more complex, as it controls both outgoing messages, and multiple mailboxes that must be
 * monitored for replies or errors (bouncebacks).  See the EmailConfig class for details on how the EmailConfig
 * influences both send and receipt of email messages.
 *
 * The existence of an OrganizationAddressConfig instance asserts that the Organization has provided all necessary
 * addressing information to be able to send (and, if supported for the channel) receive message on all channels,
 * including necessary information to assign commlogs and interactions created on both inbound and outbound messages
 * to an Organization.
 * @author Shane Riddell
 * @author Ed Delaney
 */
@Entity
@Table(name = "GCROADR")
class CommunicationOrganizationAddress implements Serializable {

    /**
     *  Immutable unique key
     */
    @Id
    @SequenceGenerator(name = "GCROADR_SEQ_GEN", allocationSize = 1, sequenceName = "GCROADR_SURROGATE_ID_SEQUENCE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GCROADR_SEQ_GEN")
    @Column(name = "GCROADR_SURROGATE_ID")
    Long Id

    /*  @Embedded
      @AttributeOverrides({
          @AttributeOverride(name = "defaultFromAddress", column = @Column(name = "email_default_from_address")),
          @AttributeOverride(name = "defaultDisplayName", column = @Column(name = "email_default_display_name"))
      })
      @AssociationOverrides({
          @AssociationOverride(name = "sender", joinColumns = @JoinColumn(name = "email_sender_mailboxacct_id")),
          @AssociationOverride(name = "replyTo", joinColumns = @JoinColumn(name = "email_replyto_mailboxacct_id"))
      })
  */
    private CommunicationOrganizationEmailContact emailContact = new CommunicationOrganizationEmailContact();

    /**
     * Embedded letter address object.
     */
/*    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street1", column = @Column(name = "ltr_street1")),
        @AttributeOverride(name = "street2", column = @Column(name = "ltr_street2")),
        @AttributeOverride(name = "city", column = @Column(name = "ltr_city")),
        @AttributeOverride(name = "state", column = @Column(name = "ltr_state")),
        @AttributeOverride(name = "country", column = @Column(name = "ltr_country")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "ltr_postalcode"))
    })
 */
   // private CommunicationLetterAddress letterAddress = new CommunicationLetterAddress();

    /**
     * Foreign key reference to the Organization (REL_ORGANIZATION) for which this address configuration is associated.
     */
    @Column(name = "GCROADR_ORG_ID")
    Long organizationId

    /**
     * Street line 1 to be used for letters.
     */
    @Column(name = "GCROADR_LTR_STREET1")
    String ltrStreet1

    /**
     * Street line 2 to be used for letters.
     */
    @Column(name = "GCROADR_LTR_STREET2")
    String ltrStreet2

    /**
     * City to be used for letters.
     */
    @Column(name = "GCROADR_LTR_CITY")
    String ltrCity

    /**
     * State to be used for letters.
     */
    @Column(name = "GCROADR_LTR_STATE")
    String ltrState

    /**
     * Country to be used for letters.
     */
    @Column(name = "GCROADR_LTR_COUNTRY")
    String ltrCountry

    /**
     * Postal code to be used for letters.
     */
    @Column(name = "GCROADR_LTR_POSTALCODE")
    String ltrPostalcode

    /**
     * Default FROM email address.
     */
    @Column(name = "GCROADR_EMAIL_DEFAULT_FROM")
    String emailDefaultFrom

    /**
     * Mailbox account used for sending email.
     */
    @Column(name = "GCROADR_EMAIL_SENDER_ACCT_ID")
    Long emailSenderAcctId

    /**
     * Mailbox account used for monitoring replies.
     */
    @Column(name = "GCROADR_EMAIL_REPLYTO_ACCT_ID")
    Long emailReplyToAcctId

    /**
     * The user friendly name of the return email address.
     */
    @Column(name = "GCROADR_EMAIL_DISPLAY_NAME")
    String emailDisplayName

    /**
     *  Optimistic lock token.
     */
    @Column(name = "GCROADR_VERSION")
    Long version

    /**
     *  The user ID of the person who inserted or last updated this record.
     */
    @Column(name = "GCROADR_USER_ID")
    String lastModifiedBy

    /**
     *  Date that record was created or last updated.
     */
    @Column(name = "GCROADR_ACTIVITY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date lastModified

    /**
     *  Source system that created or updated the data.
     */
    @Column(name = "GCROADR_DATA_ORIGIN")
    String dataOrigin
}
