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
REM sv_slbrdef_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER slbrdef_view_delete_trg
  INSTEAD OF DELETE ON sv_slbrdef
BEGIN
  gb_roomdefinition.p_delete
    (p_bldg_code => :OLD.slbrdef_bldg_code,
     p_room_number => :OLD.slbrdef_room_number,
     p_term_code_eff => :OLD.slbrdef_term_code_eff,
     p_rowid => :OLD.slbrdef_v_rowid);
END;
/
