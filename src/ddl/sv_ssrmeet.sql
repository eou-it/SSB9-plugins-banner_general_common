-- **************************************************************************************
-- * Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.                 *
-- * This copyrighted software contains confidential and proprietary information of     *
-- * SunGard Higher Education and its subsidiaries. Any use of this software is limited *
-- * solely to SunGard Higher Education licensees, and is further subject to the terms  *
-- * and conditions of one or more written license agreements between SunGard Higher    *
-- * Education and the licensee in question. SunGard, Banner and Luminis are either     *
-- * registered trademarks or trademarks of SunGard Higher Education in the U.S.A.      *
-- * and/or other regions and/or countries.                                             *
-- **************************************************************************************


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
  FROM saturn.ssrmeet;
CREATE OR REPLACE PUBLIC SYNONYM sv_ssrmeet FOR baninst1.sv_ssrmeet;
