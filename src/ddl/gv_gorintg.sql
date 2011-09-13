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
  FROM general.gorintg;
CREATE OR REPLACE PUBLIC SYNONYM gv_gorintg FOR gv_gorintg;
