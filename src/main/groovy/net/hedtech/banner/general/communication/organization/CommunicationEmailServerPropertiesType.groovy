/** *****************************************************************************
 © 2015 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.organization
/**
 * Defines the type of Email server properties.
 * CommunicationEmailServerProperties instances must be of a specific type; the type tells the CommunicationService
 * how to identify the external mail server for send or receive operations.
 *
 * Note that the string representations of this enum are persisted in the database.  Do not change the types unless
 * you also handle migration of existing data.
 */
public enum CommunicationEmailServerPropertiesType {
    Send,
    Receive;
}
