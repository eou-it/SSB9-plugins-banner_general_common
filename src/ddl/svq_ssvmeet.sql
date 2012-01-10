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
/** *****************************************************************************
 � 2011 SunGard Higher Education.  All Rights Reserved.
  CONFIDENTIAL BUSINESS INFORMATION
  THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
  AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
  NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
  WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
/**
View behind the SSAMATX query only page for advanced query of available meeting times
 */
# Script Name : svq_ssvmeet.sql
# AUDIT TRAIL   9.0
# Generated view for Horizon API support
# Audit Trail End
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