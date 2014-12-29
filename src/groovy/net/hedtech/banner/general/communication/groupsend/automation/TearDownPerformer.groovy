/*******************************************************************************

 � 2007 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATIION AND IS NOT
 TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF, NOR USED FOR ANY PURPOSE OTHER 
 THAN THAT WHICH IT IS SPECIFICALLY PROVIDED WITHOUT THE WRITTEN PERMISSION OF 
 THE SAID COMPANY
 *******************************************************************************/
package net.hedtech.banner.general.communication.groupsend.automation;

/**
 * Interface for objects that perform teardown activities in support of unit testing.
 * @author Shane Riddell
 */
public interface TearDownPerformer {

    /**
     * Performs teardown activities.
     */
    public void execute() throws Exception;
}
