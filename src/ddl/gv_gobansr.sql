--
-- *****************************************************************************
-- *                                                                           *
-- * Copyright 2010 SunGard. All rights reserved.                              *
-- *                                                                           *
-- * SunGard or its subsidiaries in the U.S. and other countries is the owner  *
-- * of numerous marks, including "SunGard," the SunGard logo, "Banner,"       *
-- * "PowerCAMPUS," "Advance," "Luminis," "UDC," and "Unified Digital Campus." *
-- * Other names and marks used in this material are owned by third parties.   *
-- *                                                                           *
-- * This [site/software] contains confidential and proprietary information of *
-- * SunGard and its subsidiaries. Use of this [site/software] is limited to   *
-- * SunGard Higher Education licensees, and is subject to the terms and       *
-- * conditions of one or more written license agreements between SunGard      *
-- * Higher Education and the licensee in question.                            *
-- *                                                                           *
-- *****************************************************************************
--
-- gv_gobansr.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 08/01/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gobansr AS SELECT
      gobansr_pidm,
      gobansr_num,
      gobansr_gobqstn_id,
      gobansr_qstn_desc,
      gobansr_ansr_desc,
      gobansr_ansr_salt,
      gobansr_surrogate_id,
      gobansr_version,
      gobansr_user_id,
      gobansr_data_origin,
      gobansr_activity_date,
      ROWID gobansr_v_rowid
  FROM gobansr;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gobansr FOR gv_gobansr;
