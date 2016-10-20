/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.overall.ldm.v6

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import net.hedtech.banner.exceptions.ApplicationException

@ToString(includeFields = true, includeNames = true)
@EqualsAndHashCode
public class PersonVisaDecorator {

    String id
    Person person

    @ToString(includeFields = true, includeNames = true)
    @EqualsAndHashCode
    public static class Person {

        String id
    }

    VisaType visaType

    @ToString(includeFields = true, includeNames = true)
    @EqualsAndHashCode
    public static class VisaType {

        PersonVisaCategory category

        String getCategory() {
            return category ? category.value : null
        }

        void setCategory(String category) {
            this.category = PersonVisaCategory.getByValue(category)
            if (!this.category) {
                throw new ApplicationException("personvisa", "invalid.category.${category}")
            }
        }
        Detail detail

        @ToString(includeFields = true, includeNames = true)
        @EqualsAndHashCode
        public static class Detail {

            String id
        }

    }

    String visaId
    PersonVisaStatus status

    String getStatus() {
        return status ? status.value : null
    }

    void setStatus(String status) {
        this.status = PersonVisaStatus.getByValue(status)
        if (!this.status) {
            throw new ApplicationException("personvisa", "invalid.status")
        }
    }
    Date requestedOn
    Date issuedOn
    String expiresOn
    List<Entries> entries

    @ToString(includeFields = true, includeNames = true)
    @EqualsAndHashCode
    public static class Entries {

        Date enteredOn
    }

}

