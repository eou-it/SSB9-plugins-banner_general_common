-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorpcol.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_sorpcol AS SELECT
      sorpcol_pidm,
      sorpcol_sbgi_code,
      sorpcol_trans_recv_date,
      sorpcol_trans_rev_date,
      sorpcol_official_trans,
      sorpcol_admr_code,
      sorpcol_surrogate_id,
      sorpcol_version,
      sorpcol_user_id,
      sorpcol_data_origin,
      sorpcol_activity_date,
      ROWID sorpcol_v_rowid
  FROM sorpcol;
CREATE OR REPLACE PUBLIC SYNONYM sv_sorpcol FOR sv_sorpcol;
