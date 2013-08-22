-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_slbbldg_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated trigger for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER slbbldg_view_delete_trg
  INSTEAD OF DELETE ON sv_slbbldg
BEGIN
  gb_bldgdefinition.p_delete
    (p_bldg_code => :OLD.slbbldg_bldg_code,
     p_rowid => :OLD.slbbldg_v_rowid);
END;
/
