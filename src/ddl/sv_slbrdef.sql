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
  FROM saturn.slbrdef;
CREATE OR REPLACE PUBLIC SYNONYM sv_slbrdef FOR sv_slbrdef;
