REM *****************************************************************************************
REM * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
REM *****************************************************************************************
REM gv_gorcmdo.sql
REM
REM AUDIT TRAIL: 9.0
REM RR 10-JUN-2013
REM 1. Generated view for Horizon API support. 
REM AUDIT TRAIL END 
REM

CREATE OR REPLACE FORCE VIEW gv_gorcmdo AS SELECT
      gorcmdo_cmsc_code,
      gorcmdo_objs_name,
      gorcmdo_seq_no,
      gorcmdo_surrogate_id,
      gorcmdo_version,
      gorcmdo_user_id,
      gorcmdo_data_origin,
      gorcmdo_activity_date,
      ROWID gorcmdo_v_rowid
  FROM gorcmdo;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gorcmdo FOR gv_gorcmdo;
