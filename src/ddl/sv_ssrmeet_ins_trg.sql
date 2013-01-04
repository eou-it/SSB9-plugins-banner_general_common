-- *****************************************************************************************
-- * Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.                    *
-- * This copyrighted software contains confidential and proprietary information of        *
-- * SunGard Higher Education and its subsidiaries. Any use of this software is limited    *
-- * solely to SunGard Higher Education licensees, and is further subject to the terms     *
-- * and conditions of one or more written license agreements between SunGard Higher       *
-- * Education and the licensee in question. SunGard is either a registered trademark or   *
-- * trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.*
-- * Banner and Luminis are either registered trademarks or trademarks of SunGard Higher   *
-- * Education in the U.S.A. and/or other regions and/or countries.                        *
-- *****************************************************************************************
REM
REM sv_ssrmeet_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
create or replace
TRIGGER ssrmeet_view_create_trg
  INSTEAD OF INSERT ON sv_ssrmeet
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.ssrmeet_surrogate_id);
  gfksjpa.setVersion(:NEW.ssrmeet_version);
  gb_classtimes.p_create
    (p_term_code => :NEW.ssrmeet_term_code,
     p_crn => :NEW.ssrmeet_crn,
     p_days_code => :NEW.ssrmeet_days_code,
     p_day_number => :NEW.ssrmeet_day_number,
     p_begin_time =>  :NEW.ssrmeet_begin_time,
     p_end_time => :NEW.ssrmeet_end_time,
     p_bldg_code => :NEW.ssrmeet_bldg_code,
     p_room_code => :NEW.ssrmeet_room_code,
     p_start_date => :NEW.ssrmeet_start_date,
     p_end_date => :NEW.ssrmeet_end_date,
     p_catagory => :NEW.ssrmeet_catagory,
     p_sun_day => :NEW.ssrmeet_sun_day,
     p_mon_day => :NEW.ssrmeet_mon_day,
     p_tue_day => :NEW.ssrmeet_tue_day,
     p_wed_day => :NEW.ssrmeet_wed_day,
     p_thu_day => :NEW.ssrmeet_thu_day,
     p_fri_day => :NEW.ssrmeet_fri_day,
     p_sat_day => :NEW.ssrmeet_sat_day,
     p_schd_code => :NEW.ssrmeet_schd_code,
     p_over_ride => :NEW.ssrmeet_over_ride,
     p_credit_hr_sess => :NEW.ssrmeet_credit_hr_sess,
     p_meet_no => :NEW.ssrmeet_meet_no,
     p_hrs_week => :NEW.ssrmeet_hrs_week,
     p_func_code => :NEW.ssrmeet_func_code,
     p_comt_code => :NEW.ssrmeet_comt_code,
     p_schs_code => :NEW.ssrmeet_schs_code,
     p_mtyp_code => :NEW.ssrmeet_mtyp_code,
     p_data_origin => :NEW.ssrmeet_data_origin,
     p_user_id => :NEW.ssrmeet_user_id,
     p_rowid_out => p_rowid_v);
END;
/
