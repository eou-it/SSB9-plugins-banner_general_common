-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_slbrdef_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated trigger for Banner XE API support
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
