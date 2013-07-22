-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
 REM
REM svq_ssvmeet.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM View behind the SSAMATX query only page for advanced query of available meeting times
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW SVQ_SSVMEET
(ssrmeet_surrogate_id,
ssrmeet_version ,
ssrmeet_term_code,
ssrmeet_crn,
ssbsect_subj_code,
ssbsect_crse_numb,
ssbsect_ptrm_code,
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
ssrmeet_room_code,
ssrmeet_func_code,
ssrmeet_user_id,
ssrmeet_activity_date,
slbbldg_camp_code,
ssrmeet_over_ride,
ssrxlst_xlst_group)
AS
  SELECT ssrmeet.ssrmeet_surrogate_id,
    ssrmeet.ssrmeet_version ,
    SSRMEET.SSRMEET_TERM_CODE,
    SSRMEET.SSRMEET_CRN,
    SSBSECT.SSBSECT_SUBJ_CODE,
    SSBSECT.SSBSECT_CRSE_NUMB,
    SSBSECT.SSBSECT_PTRM_CODE,
    SSRMEET.SSRMEET_START_DATE,
    SSRMEET.SSRMEET_END_DATE,
    SSRMEET.SSRMEET_DAY_NUMBER,
    SSRMEET.SSRMEET_MON_DAY,
    SSRMEET.SSRMEET_TUE_DAY,
    SSRMEET.SSRMEET_WED_DAY,
    SSRMEET.SSRMEET_THU_DAY,
    SSRMEET.SSRMEET_FRI_DAY,
    SSRMEET.SSRMEET_SAT_DAY,
    SSRMEET.SSRMEET_SUN_DAY,
    SSRMEET.SSRMEET_BEGIN_TIME,
    SSRMEET.SSRMEET_END_TIME,
    SSRMEET.SSRMEET_BLDG_CODE,
    SSRMEET.SSRMEET_ROOM_CODE,
    SSRMEET.SSRMEET_FUNC_CODE,
    SSRMEET.SSRMEET_USER_ID,
    SSRMEET.SSRMEET_ACTIVITY_DATE,
    DECODE(SSRMEET.SSRMEET_BLDG_CODE,NULL,NULL,
    (SELECT SLBBLDG.SLBBLDG_CAMP_CODE
    FROM SLBBLDG
    WHERE SLBBLDG.SLBBLDG_BLDG_CODE = SSRMEET.SSRMEET_BLDG_CODE
    )),
    SSRMEET.SSRMEET_OVER_RIDE,
    SUBSTR(SSKSELS.F_GET_SSRXLST_GROUP(SSRMEET.SSRMEET_TERM_CODE, SSRMEET.SSRMEET_CRN),1,2)
  FROM SSBSECT,
    SSRMEET
  WHERE SSBSECT.SSBSECT_TERM_CODE = SSRMEET.SSRMEET_TERM_CODE
  AND SSBSECT.SSBSECT_CRN         = SSRMEET.SSRMEET_CRN
  UNION ALL
  SELECT ssrmeet.ssrmeet_surrogate_id,
    ssrmeet.ssrmeet_version ,
    SSRMEET.SSRMEET_TERM_CODE,
    SSRMEET.SSRMEET_CRN,
    NULL,
    NULL,
    NULL,
    SSRMEET.SSRMEET_START_DATE,
    SSRMEET.SSRMEET_END_DATE,
    SSRMEET.SSRMEET_DAY_NUMBER,
    SSRMEET.SSRMEET_MON_DAY,
    SSRMEET.SSRMEET_TUE_DAY,
    SSRMEET.SSRMEET_WED_DAY,
    SSRMEET.SSRMEET_THU_DAY,
    SSRMEET.SSRMEET_FRI_DAY,
    SSRMEET.SSRMEET_SAT_DAY,
    SSRMEET.SSRMEET_SUN_DAY,
    SSRMEET.SSRMEET_BEGIN_TIME,
    SSRMEET.SSRMEET_END_TIME,
    SSRMEET.SSRMEET_BLDG_CODE,
    SSRMEET.SSRMEET_ROOM_CODE,
    SSRMEET.SSRMEET_FUNC_CODE,
    SSRMEET.SSRMEET_USER_ID,
    SSRMEET.SSRMEET_ACTIVITY_DATE,
    DECODE(SSRMEET.SSRMEET_BLDG_CODE,NULL,NULL,
    (SELECT SLBBLDG.SLBBLDG_CAMP_CODE
    FROM SLBBLDG
    WHERE SLBBLDG.SLBBLDG_BLDG_CODE = SSRMEET.SSRMEET_BLDG_CODE
    )),
    SSRMEET.SSRMEET_OVER_RIDE,
    NULL
  FROM SSRMEET
  WHERE NOT EXISTS
    (SELECT 'X'
    FROM SSBSECT
    WHERE SSBSECT.SSBSECT_TERM_CODE = SSRMEET.SSRMEET_TERM_CODE
    AND SSBSECT.SSBSECT_CRN         = SSRMEET.SSRMEET_CRN
    ) ;
COMMENT ON TABLE svq_ssvmeet IS
  'Section Meeting Time View';
CREATE OR REPLACE PUBLIC SYNONYM svq_ssvmeet FOR svq_ssvmeet;
