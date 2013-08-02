-- *****************************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************
REM
REM gurinfo_080602_02.sql
REM
-- AUDIT TRAIL: 8.6.2
-- 1. Script to add the primary key to the GURINFO table.
-- AUDIT TRAIL END
REM

ALTER TABLE GURINFO
    ADD CONSTRAINT PK_GURINFO
    PRIMARY KEY (
                 GURINFO_PAGE_NAME,
                 GURINFO_LABEL,
                 GURINFO_SEQUENCE_NUMBER,
                 GURINFO_ROLE_CODE,
                 GURINFO_LOCALE,
                 GURINFO_SOURCE_INDICATOR
                )
    USING INDEX
STORAGE (INITIAL     &MMEDINX_INITIAL_EXTENT
         NEXT        &MMEDINX_NEXT_EXTENT
         MINEXTENTS  &MMEDINX_MIN_EXTENTS
         MAXEXTENTS  &MMEDINX_MAX_EXTENTS
         PCTINCREASE &MMEDINX_PCT_INCREASE)
TABLESPACE           &MMEDINX_TABLESPACE_NAME
PCTFREE              &MMEDINX_PCT_FREE
;