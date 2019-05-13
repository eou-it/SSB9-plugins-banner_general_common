/** *****************************************************************************
 © 2014 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package net.hedtech.banner.general.communication.email

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

/**
 * Represents the final merged template after all the recipient data is applied to the template.
 * This is the object that will be send to the email service.
 */
@ToString
@EqualsAndHashCode
class CommunicationMergedEmailTemplate implements Serializable {
    String toList
    String subject
    String content
}
