-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************

REM
REM gurinfo_090000_01.sql
REM
REM Script for GURINFO table creation
REM AUDIT TRAIL END
REM
connect general/&&general_password
CREATE TABLE GURINFO
(
  GURINFO_PAGE_NAME          VARCHAR2(20) NOT NULL,
  GURINFO_TEXT_TYPE          VARCHAR2(20) NOT NULL,
  GURINFO_SEQUENCE_NUMBER    NUMBER(5) NOT NULL,
  GURINFO_PERSONA            VARCHAR2(100) NOT NULL,
  GURINFO_TEXT               VARCHAR2(4000) NOT NULL,
  GURINFO_LOCALE             VARCHAR2(20) NOT NULL,
  GURINFO_COMMENT            VARCHAR2(200) NOT NULL,
  GURINFO_START_DATE         DATE, 
  GURINFO_END_DATE           DATE,
  GURINFO_SOURCE_INDICATOR   VARCHAR2(2) NOT NULL,
  GURINFO_DATA_ORIGIN        VARCHAR2(30),
  GURINFO_VPDI_CODE          VARCHAR2(6),
  GURINFO_USER_ID            VARCHAR2(30) NOT NULL,
  GURINFO_ACTIVITY_DATE      DATE,
  GURINFO_SURROGATE_ID       NUMBER(19),
  GURINFO_VERSION            NUMBER(19)
)
STORAGE (INITIAL      &MSMLTAB_INITIAL_EXTENT
         NEXT         &MSMLTAB_NEXT_EXTENT
         MINEXTENTS   &MSMLTAB_MIN_EXTENTS
         MAXEXTENTS   &MSMLTAB_MAX_EXTENTS
         PCTINCREASE  &MSMLTAB_PCT_INCREASE)
TABLESPACE            &MSMLTAB_TABLESPACE_NAME
PCTFREE               &MSMLTAB_PCT_FREE
PCTUSED               &MSMLTAB_PCT_USED;
