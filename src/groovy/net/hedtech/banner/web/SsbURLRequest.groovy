/*******************************************************************************
 Copyright 2009-2012 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.web

class SsbURLRequest {
    private static final String SSB_BASE_URL = "ssb"
    private static final String SLASH = "/"

    public String getControllerNameFromPath(String url) {
        if (url != null && url.contains(SSB_BASE_URL)) {
            url = url.substring(url.indexOf(SSB_BASE_URL) + SSB_BASE_URL.length() + 1);
            if (url.contains(SLASH)) {
                url = url.substring(0, url.indexOf(SLASH))
            }
        }
        return url
    }
}
