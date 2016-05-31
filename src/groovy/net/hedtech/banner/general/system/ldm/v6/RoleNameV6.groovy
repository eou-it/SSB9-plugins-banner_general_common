/*******************************************************************************
 Copyright 2016 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.system.ldm.v6

enum RoleNameV6 {

    STUDENT("student"),
    INSTRUCTOR("instructor"),
    EMPLOYEE("employee"),
    VENDOR("vendor"),
    ALUMNI("alumni"),
    PROSPECTIVE_STUDENT("prospectiveStudent"),
    ADVISOR("advisor")

    private final String value


    RoleNameV6(String value) { this.value = value }


    public String getValue() { return value }


    public static RoleNameV6 getByValue(String value) {
        Iterator itr = RoleNameV6.values().iterator()
        while (itr.hasNext()) {
            RoleNameV6 roleName = itr.next()
            if (roleName.value == value) {
                return roleName
            }
        }
        return null
    }

}
