/*******************************************************************************
 Copyright 2019 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/
package net.hedtech.banner.general.common

import oracle.jdbc.OracleTypes

enum PlsqlDataType {

    IDENT_ARR(OracleTypes.VARCHAR, 30),
    VC_ARR(OracleTypes.VARCHAR, 4000),
    TAB_TYPE(OracleTypes.VARCHAR, 1000),
    CHAR_ARR(OracleTypes.VARCHAR, 1),
    TABLE_TYPE(OracleTypes.VARCHAR, 18);

    private int sqlType
    private int maxLen

    private PlsqlDataType(int sqlType, int maxLen) {
        this.sqlType = sqlType
        this.maxLen = maxLen
    }

    int getSqlType() {
        return sqlType
    }

    int getMaxLen() {
        return maxLen
    }

}