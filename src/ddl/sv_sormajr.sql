-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sormajr.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_sormajr AS SELECT
      sormajr_pidm,
      sormajr_sbgi_code,
      sormajr_degc_code,
      sormajr_degr_seq_no,
      sormajr_majr_code_major,
      sormajr_surrogate_id,
      sormajr_version,
      sormajr_user_id,
      sormajr_data_origin,
      sormajr_activity_date,
      ROWID sormajr_v_rowid
  FROM sormajr;
CREATE OR REPLACE PUBLIC SYNONYM sv_sormajr FOR sv_sormajr;
