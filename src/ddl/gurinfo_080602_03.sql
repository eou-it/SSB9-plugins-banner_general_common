-- *****************************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************

REM
REM gurinfo_080602_04.sql
REM
-- AUDIT TRAIL: 8.6.2
-- 1. New table to store info text information for Banner XE.
-- AUDIT TRAIL END
REM

COMMENT ON TABLE  GURINFO IS
'Information Text Table.';
COMMENT ON COLUMN GURINFO.GURINFO_PAGE_NAME IS
'PAGE NAME: Name of associated page.';
COMMENT ON COLUMN GURINFO.GURINFO_LABEL IS
'LABEL: Short label, used to select which set of text items to print for a page.';
COMMENT ON COLUMN GURINFO.GURINFO_TEXT_TYPE IS
'TEXT TYPE: Type of text.';
COMMENT ON COLUMN GURINFO.GURINFO_SEQUENCE_NUMBER IS
'SEQUENCE NUMBER : Sequence number for this text item.';
COMMENT ON COLUMN GURINFO.GURINFO_ROLE_CODE IS
'ROLE CODE: Role associated with the text.';
COMMENT ON COLUMN GURINFO.GURINFO_TEXT IS
'TEXT: Text to be displayed on web page when this item is selected.';
COMMENT ON COLUMN GURINFO.GURINFO_LOCALE IS
'LOCALE: Locale of the text';
COMMENT ON COLUMN GURINFO.GURINFO_COMMENT IS
'COMMENT: Comment about this text item.';
COMMENT ON COLUMN GURINFO.GURINFO_SOURCE_INDICATOR IS
'SOURCE INDICATOR : Source Indicator: This field indicates if the row is (B)aseline or (L)ocal. The default value is B.';
COMMENT ON COLUMN GURINFO.GURINFO_ACTIVITY_DATE IS
'ACTIVITY DATE: The date that information in this record was entered or last updated.';
COMMENT ON COLUMN GURINFO.GURINFO_START_DATE IS
'START DATE: The date from when the text should be displayed.';
COMMENT ON COLUMN GURINFO.GURINFO_END_DATE IS
'END DATE: The date until when the text should be displayed.';
COMMENT ON COLUMN GURINFO.GURINFO_SURROGATE_ID IS
'SURROGATE ID: Immutable unique key';
COMMENT ON COLUMN GURINFO.GURINFO_VERSION IS
'VERSION: Optimistic lock token.';
COMMENT ON COLUMN GURINFO.GURINFO_USER_ID IS
'USER ID: The user ID of the person who inserted or last updated this record.';
COMMENT ON COLUMN GURINFO.GURINFO_DATA_ORIGIN IS
'DATA ORIGIN: Source system that created or updated the data.';
COMMENT ON COLUMN GURINFO.GURINFO_VPDI_CODE IS
'VPDI CODE: Multi-entity processing code.';









