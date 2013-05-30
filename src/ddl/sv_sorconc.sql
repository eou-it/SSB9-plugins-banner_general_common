-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorconc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_sorconc AS SELECT
      sorconc_pidm,
      sorconc_sbgi_code,
      sorconc_degc_code,
      sorconc_degr_seq_no,
      sorconc_majr_code_conc,
      sorconc_surrogate_id,
      sorconc_version,
      sorconc_user_id,
      sorconc_data_origin,
      sorconc_activity_date,
      ROWID sorconc_v_rowid
  FROM sorconc;
CREATE OR REPLACE PUBLIC SYNONYM sv_sorconc FOR sv_sorconc;
