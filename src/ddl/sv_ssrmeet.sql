-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_ssrmeet.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW sv_ssrmeet AS SELECT
      ssrmeet_term_code,
      ssrmeet_crn,
      ssrmeet_days_code,
      ssrmeet_day_number,
      ssrmeet_begin_time,
      ssrmeet_end_time,
      ssrmeet_bldg_code,
      ssrmeet_room_code,
      ssrmeet_start_date,
      ssrmeet_end_date,
      ssrmeet_catagory,
      ssrmeet_sun_day,
      ssrmeet_mon_day,
      ssrmeet_tue_day,
      ssrmeet_wed_day,
      ssrmeet_thu_day,
      ssrmeet_fri_day,
      ssrmeet_sat_day,
      ssrmeet_schd_code,
      ssrmeet_over_ride,
      ssrmeet_credit_hr_sess,
      ssrmeet_meet_no,
      ssrmeet_hrs_week,
      ssrmeet_func_code,
      ssrmeet_comt_code,
      ssrmeet_schs_code,
      ssrmeet_mtyp_code,
      ssrmeet_surrogate_id,
      ssrmeet_version,
      ssrmeet_user_id,
      ssrmeet_data_origin,
      ssrmeet_activity_date,
      ROWID ssrmeet_v_rowid
  FROM  ssrmeet;
CREATE OR REPLACE PUBLIC SYNONYM sv_ssrmeet FOR sv_ssrmeet;
