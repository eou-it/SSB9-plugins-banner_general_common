create or replace
TRIGGER baninst1.ssrmeet_view_update_trg
  INSTEAD OF UPDATE ON baninst1.sv_ssrmeet
BEGIN
  gfksjpa.setId(:OLD.ssrmeet_surrogate_id);
  gfksjpa.setVersion(:NEW.ssrmeet_version);
  gb_classtimes.p_update
    (p_term_code => :NEW.ssrmeet_term_code,
     p_crn => :NEW.ssrmeet_crn,
     p_days_code => :NEW.ssrmeet_days_code,
     p_day_number => :NEW.ssrmeet_day_number,
     p_begin_time => :NEW.ssrmeet_begin_time,
     p_end_time => :NEW.ssrmeet_end_time,
     p_bldg_code => :NEW.ssrmeet_bldg_code,
     p_room_code => :NEW.ssrmeet_room_code,
     p_start_date => to_date( :NEW.ssrmeet_start_date,  g$_date.get_nls_date_format),
     p_end_date => to_date(:NEW.ssrmeet_end_date,  g$_date.get_nls_date_format),
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
     p_rowid => :NEW.ssrmeet_v_rowid);
END;
/