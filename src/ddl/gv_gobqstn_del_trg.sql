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
-- gv_gobqstn_del_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 08/01/2013
--
--    Generated trigger for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gobqstn_view_delete_trg
  INSTEAD OF DELETE ON gv_gobqstn
BEGIN
  gb_pin_question.p_delete
    (p_id => :OLD.gobqstn_id,
     p_rowid => :OLD.gobqstn_v_rowid);
END;
/
show errors
