-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM gv_gorintg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated view for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW gv_gorintg AS SELECT
      gorintg_integration_cde,
      gorintg_desc,
      gorintg_intp_code,
      gorintg_surrogate_id,
      gorintg_version,
      gorintg_user_id,
      gorintg_data_origin,
      gorintg_activity_date,
      ROWID gorintg_v_rowid
  FROM gorintg;
CREATE OR REPLACE PUBLIC SYNONYM gv_gorintg FOR gv_gorintg;
