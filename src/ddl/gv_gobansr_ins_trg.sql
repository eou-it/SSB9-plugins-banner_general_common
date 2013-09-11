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
-- gv_gobansr_ins_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 08/01/2013
--
--    Generated trigger for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gobansr_view_create_trg
  INSTEAD OF INSERT ON gv_gobansr
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gobansr_surrogate_id);
  gfksjpa.setVersion(:NEW.gobansr_version);
  gb_pin_answer.p_create
    (p_pidm => :NEW.gobansr_pidm,
     p_num => :NEW.gobansr_num,
     p_gobqstn_id => :NEW.gobansr_gobqstn_id,
     p_qstn_desc => :NEW.gobansr_qstn_desc,
     p_ansr_desc => :NEW.gobansr_ansr_desc,
     p_ansr_salt => :NEW.gobansr_ansr_salt,
     p_user_id => :NEW.gobansr_user_id,
     p_data_origin => :NEW.gobansr_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
show errors
