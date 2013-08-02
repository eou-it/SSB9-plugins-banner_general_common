-- *****************************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************

REM
REM gurinfo_080602_01.sql
REM
-- AUDIT TRAIL: 8.6.2
-- 1. New table to store info text information for Banner XE.
-- AUDIT TRAIL END
REM

CREATE TABLE GURINFO
(
  GURINFO_PAGE_NAME          VARCHAR2(20)   NOT NULL,
  GURINFO_LABEL              VARCHAR2(20)   NOT NULL,
  GURINFO_TEXT_TYPE          VARCHAR2(20)   NOT NULL,
  GURINFO_SEQUENCE_NUMBER    NUMBER(5)      NOT NULL,
  GURINFO_ROLE_CODE          VARCHAR2(30)   NOT NULL,
  GURINFO_TEXT               VARCHAR2(4000) NOT NULL,
  GURINFO_LOCALE             VARCHAR2(20)   NOT NULL,
  GURINFO_COMMENT            VARCHAR2(200)  NOT NULL,
  GURINFO_SOURCE_INDICATOR   VARCHAR2(1)    DEFAULT 'B' NOT NULL,
  GURINFO_ACTIVITY_DATE      DATE           NOT NULL,
  GURINFO_START_DATE         DATE           NULL,
  GURINFO_END_DATE           DATE           NULL,
  GURINFO_SURROGATE_ID       NUMBER(19)     NULL,
  GURINFO_VERSION            NUMBER(19)     NULL,
  GURINFO_USER_ID            VARCHAR2(30)   NULL,
  GURINFO_DATA_ORIGIN        VARCHAR2(30)   NULL,
  GURINFO_VPDI_CODE          VARCHAR2(6)    NULL
)
STORAGE (INITIAL      &MSMLTAB_INITIAL_EXTENT
         NEXT         &MSMLTAB_NEXT_EXTENT
         MINEXTENTS   &MSMLTAB_MIN_EXTENTS
         MAXEXTENTS   &MSMLTAB_MAX_EXTENTS
         PCTINCREASE  &MSMLTAB_PCT_INCREASE)
TABLESPACE            &MSMLTAB_TABLESPACE_NAME
PCTFREE               &MSMLTAB_PCT_FREE
PCTUSED               &MSMLTAB_PCT_USED;


CREATE PUBLIC SYNONYM GURINFO FOR GURINFO;
GRANT INSERT, UPDATE, DELETE, SELECT ON GURINFO TO PUBLIC;
