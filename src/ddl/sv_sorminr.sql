-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorminr.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_sorminr AS SELECT
      sorminr_pidm,
      sorminr_sbgi_code,
      sorminr_degc_code,
      sorminr_degr_seq_no,
      sorminr_majr_code_minor,
      sorminr_surrogate_id,
      sorminr_version,
      sorminr_user_id,
      sorminr_data_origin,
      sorminr_activity_date,
      ROWID sorminr_v_rowid
  FROM sorminr;
CREATE OR REPLACE PUBLIC SYNONYM sv_sorminr FOR sv_sorminr;
