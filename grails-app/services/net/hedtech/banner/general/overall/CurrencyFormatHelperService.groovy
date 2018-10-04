/********************************************************************************
 Copyright 2016-2018 Ellucian Company L.P. and its affiliates.
 ********************************************************************************/
package net.hedtech.banner.general.overall

import net.hedtech.banner.general.system.InstitutionalDescription
import org.springframework.web.context.request.RequestContextHolder

class CurrencyFormatHelperService {

    def currencyFormatService

    /**
     * Formats a Number to currency.  This is null safe.
     */
    def formatCurrency(amount) {
        def formattedAmount

        if (amount instanceof Number) {
            formattedAmount = currencyFormatService.format(getCurrencyCode(),
                    (amount instanceof BigDecimal ? amount : BigDecimal.valueOf(amount)))
        }

        return formattedAmount
    }

    public static getCurrencyCode() {
        def currencyCode
        def session = RequestContextHolder?.currentRequestAttributes()?.request?.session

        if (session?.getAttribute("baseCurrencyCode")) {
            currencyCode = session.getAttribute("baseCurrencyCode")
        } else {
            currencyCode = InstitutionalDescription.fetchByKey()?.baseCurrCode
            session.setAttribute("baseCurrencyCode", currencyCode)
        }

        return currencyCode
    }
}
