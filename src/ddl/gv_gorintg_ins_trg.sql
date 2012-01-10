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
REM gv_gorintg_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER gorintg_view_create_trg
  INSTEAD OF INSERT ON gv_gorintg
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorintg_surrogate_id);
  gfksjpa.setVersion(:NEW.gorintg_version);
  gb_partner_rule.p_create
    (p_integration_cde => :NEW.gorintg_integration_cde,
     p_desc => :NEW.gorintg_desc,
     p_intp_code => :NEW.gorintg_intp_code,
     p_user_id => :NEW.gorintg_user_id,
     p_data_origin => :NEW.gorintg_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
