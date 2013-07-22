-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


/**
View behind the SSRMEET in query for class search
 */

CREATE OR REPLACE FORCE VIEW GVQ_SSRMEET
(ssrmeet_surrogate_id,
ssrmeet_version ,
ssrmeet_term_code,
ssrmeet_crn,
ssrmeet_category,
ssrmeet_start_date,
ssrmeet_end_date,
ssrmeet_day_number,
ssrmeet_mon_day,
ssrmeet_tue_day,
ssrmeet_wed_day,
ssrmeet_thu_day,
ssrmeet_fri_day,
ssrmeet_sat_day,
ssrmeet_sun_day,
ssrmeet_begin_time,
ssrmeet_end_time,
ssrmeet_bldg_code,
ssrmeet_bldg_desc,
ssrmeet_room_code,
ssrmeet_func_code,
slbbldg_camp_code,
ssrmeet_over_ride,
ssrmeet_days_code,
ssrmeet_schd_code   ,
ssrmeet_mtyp_code  ,
ssrmeet_credit_hr_sess ,
ssrmeet_meet_no,
ssrmeet_hrs_week,
ssrmeet_term_crn)
AS
  SELECT ssrmeet.ssrmeet_surrogate_id,
    ssrmeet.ssrmeet_version ,
    SSRMEET.SSRMEET_TERM_CODE,
    SSRMEET.SSRMEET_CRN,
    ssrmeet.ssrmeet_catagory,
    SSRMEET.SSRMEET_START_DATE,
    SSRMEET.SSRMEET_END_DATE,
    SSRMEET.SSRMEET_DAY_NUMBER,
    DECODE(SSRMEET.SSRMEET_MON_DAY,'M','Y','N'),
    DECODE(SSRMEET.SSRMEET_TUE_DAY,'T','Y','N'),
    DECODE(SSRMEET.SSRMEET_WED_DAY,'W','Y','N'),
    DECODE(SSRMEET.SSRMEET_THU_DAY,'R','Y','N'),
    DECODE(SSRMEET.SSRMEET_FRI_DAY,'F','Y','N'),
    DECODE(SSRMEET.SSRMEET_SAT_DAY,'S','Y','N'),
    DECODE(SSRMEET.SSRMEET_SUN_DAY,'U','Y','N'),
    SSRMEET.SSRMEET_BEGIN_TIME,
    SSRMEET.SSRMEET_END_TIME,
    SSRMEET.SSRMEET_BLDG_CODE,
    (select stvbldg_desc from stvbldg where stvbldg_code = SSRMEET.SSRMEET_BLDG_CODE)  build_name,
    SSRMEET.SSRMEET_ROOM_CODE,
    SSRMEET.SSRMEET_FUNC_CODE,
    DECODE(SSRMEET.SSRMEET_BLDG_CODE,NULL,NULL,
    (SELECT SLBBLDG.SLBBLDG_CAMP_CODE
    FROM SLBBLDG
    WHERE SLBBLDG.SLBBLDG_BLDG_CODE = SSRMEET.SSRMEET_BLDG_CODE
    )),
    SSRMEET.SSRMEET_OVER_RIDE   ,
    SSRMEET_DAYS_CODE,
    SSRMEET_SCHD_CODE   ,
    SSRMEET_MTYP_CODE  ,
    SSRMEET_CREDIT_HR_SESS     ,
    SSRMEET_MEET_NO,
    SSRMEET_HRS_WEEK  ,
    ssrmeet_term_code || ssrmeet_crn
  FROM     SSRMEET
  WITH READ ONLY
   ;
COMMENT ON TABLE gvq_ssrmeet IS
  'Section Meeting Time View';
CREATE OR REPLACE PUBLIC SYNONYM gvq_ssrmeet FOR gvq_ssrmeet;
