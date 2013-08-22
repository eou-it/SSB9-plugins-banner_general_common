-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_slbbldg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated view for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_slbbldg AS SELECT
      slbbldg_bldg_code,
      slbbldg_camp_code,
      slbbldg_capacity,
      slbbldg_maximum_capacity,
      slbbldg_rrcd_code,
      slbbldg_prcd_code,
      slbbldg_site_code,
      slbbldg_street_line1,
      slbbldg_street_line2,
      slbbldg_street_line3,
      slbbldg_city,
      slbbldg_stat_code,
      slbbldg_zip,
      slbbldg_cnty_code,
      slbbldg_phone_area,
      slbbldg_phone_number,
      slbbldg_phone_extension,
      slbbldg_sex,
      slbbldg_coll_code,
      slbbldg_dept_code,
      slbbldg_key_number,
      slbbldg_pars_code,
      slbbldg_ctry_code_phone,
      slbbldg_house_number,
      slbbldg_street_line4,
      slbbldg_surrogate_id,
      slbbldg_version,
      slbbldg_user_id,
      slbbldg_data_origin,
      slbbldg_activity_date,
      ROWID slbbldg_v_rowid
  FROM  slbbldg;
CREATE OR REPLACE PUBLIC SYNONYM sv_slbbldg FOR sv_slbbldg;
