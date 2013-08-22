-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_slbrdef.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated view for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_slbrdef AS SELECT
      slbrdef_bldg_code,
      slbrdef_room_number,
      slbrdef_desc,
      slbrdef_capacity,
      slbrdef_maximum_capacity,
      slbrdef_rmst_code,
      slbrdef_rrcd_code,
      slbrdef_prcd_code,
      slbrdef_utility_rate,
      slbrdef_utility_rate_period,
      slbrdef_phone_area,
      slbrdef_phone_number,
      slbrdef_phone_extension,
      slbrdef_bcat_code,
      slbrdef_sex,
      slbrdef_room_type,
      slbrdef_priority,
      slbrdef_coll_code,
      slbrdef_dept_code,
      slbrdef_key_number,
      slbrdef_width,
      slbrdef_length,
      slbrdef_area,
      slbrdef_term_code_eff,
      slbrdef_pars_code,
      slbrdef_ctry_code_phone,
      slbrdef_surrogate_id,
      slbrdef_version,
      slbrdef_user_id,
      slbrdef_data_origin,
      slbrdef_activity_date,
      ROWID slbrdef_v_rowid
  FROM  slbrdef;
CREATE OR REPLACE PUBLIC SYNONYM sv_slbrdef FOR sv_slbrdef;
