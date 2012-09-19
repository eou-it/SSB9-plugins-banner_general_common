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
REM gv_gorintg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW gv_gorintg AS SELECT
      gorintg_integration_cde,
      gorintg_desc,
      gorintg_intp_code,
      gorintg_surrogate_id,
      gorintg_version,
      gorintg_user_id,
      gorintg_data_origin,
      gorintg_activity_date,
      ROWID gorintg_v_rowid
  FROM gorintg;
CREATE OR REPLACE PUBLIC SYNONYM gv_gorintg FOR gv_gorintg;
