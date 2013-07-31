-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************

REM
REM gurinfo_090000_02.sql
REM
REM Comments for GURINFO Table
REM AUDIT TRAIL END
REM

COMMENT ON TABLE  GURINFO IS
'Information Text Editor   Table.';
COMMENT ON COLUMN GURINFO.GURINFO_SURROGATE_ID IS
'SURROGATE ID: Immutable unique key';
COMMENT ON COLUMN GURINFO.GURINFO_PAGE_NAME IS
'PAGE NAME :Page Name';
COMMENT ON COLUMN GURINFO.GURINFO_SEQUENCE_NUMBER IS
'SEQUENCE NUMBER :';
COMMENT ON COLUMN GURINFO.GURINFO_PERSONA IS
'PERSONA: person';
COMMENT ON COLUMN GURINFO.GURINFO_TEXT IS
'TEXT: text';
COMMENT ON COLUMN GURINFO.GURINFO_COMMENT IS
'COMMENT:comment ';
COMMENT ON COLUMN GURINFO.GURINFO_LOCALE IS
'LOCALE: locale';
COMMENT ON COLUMN GURINFO.GURINFO_SOURCE_INDICATOR IS
'SOURCE INDICATOR : source indicator';
COMMENT ON COLUMN GURINFO.GURINFO_DATA_ORIGIN IS
'DATA ORIGIN: data origin';
COMMENT ON COLUMN GURINFO.GURINFO_VPDI_CODE IS
'VPDI CODE: VPID code';
COMMENT ON COLUMN GURINFO.GURINFO_USER_ID IS
'USER ID: The user ID of the person who inserted or last updated this record.';
COMMENT ON COLUMN GURINFO.GURINFO_ACTIVITY_DATE IS
'ACTIVITY DATE: The date that information in this record was entered or last updated.';
COMMENT ON COLUMN GURINFO.GURINFO_VERSION IS
'VERSION: Optimistic lock token.';









