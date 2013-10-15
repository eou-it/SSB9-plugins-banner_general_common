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
-- gv_gobqstn.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 08/01/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gobqstn AS SELECT
      gobqstn_id,
      gobqstn_desc,
      gobqstn_display_ind,
      gobqstn_surrogate_id,
      gobqstn_version,
      gobqstn_user_id,
      gobqstn_data_origin,
      gobqstn_activity_date,
      ROWID gobqstn_v_rowid
  FROM gobqstn;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gobqstn FOR gv_gobqstn;
