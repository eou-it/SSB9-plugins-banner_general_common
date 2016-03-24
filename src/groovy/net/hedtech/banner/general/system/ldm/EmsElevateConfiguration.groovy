/*********************************************************************************
 Copyright 2016-2016 Ellucian Company L.P. and its affiliates.
 **********************************************************************************/
package net.hedtech.banner.general.system.ldm

import net.hedtech.banner.general.system.ldm.v1.MessageConfig

/**
 * Configuration object for direct integration with Elevate or Pilot
 */
class EmsElevateConfiguration extends EmsConfiguration {

    MessageConfig messageInConfig
    MessageConfig messageOutConfig

}
