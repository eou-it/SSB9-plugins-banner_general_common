-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sordegr.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_sordegr AS SELECT
      sordegr_pidm,
      sordegr_sbgi_code,
      sordegr_degc_code,
      sordegr_degr_seq_no,
      sordegr_attend_from,
      sordegr_attend_to,
      sordegr_hours_transferred,
      sordegr_gpa_transferred,
      sordegr_degc_date,
      sordegr_degc_year,
      sordegr_coll_code,
      sordegr_honr_code,
      sordegr_term_degree,
      sordegr_egol_code,
      sordegr_primary_ind,
      sordegr_surrogate_id,
      sordegr_version,
      sordegr_user_id,
      sordegr_data_origin,
      sordegr_activity_date,
      ROWID sordegr_v_rowid
  FROM sordegr;
CREATE OR REPLACE PUBLIC SYNONYM sv_sordegr FOR sv_sordegr;
