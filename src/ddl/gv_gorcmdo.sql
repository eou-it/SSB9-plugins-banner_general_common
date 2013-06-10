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
-- gv_gorcmdo.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gorcmdo AS SELECT
      gorcmdo_cmsc_code,
      gorcmdo_objs_name,
      gorcmdo_seq_no,
      gorcmdo_surrogate_id,
      gorcmdo_version,
      gorcmdo_user_id,
      gorcmdo_data_origin,
      gorcmdo_activity_date,
      ROWID gorcmdo_v_rowid
  FROM gorcmdo;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gorcmdo FOR gv_gorcmdo;
