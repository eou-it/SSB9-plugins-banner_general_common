-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM gv_gorintg_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER gorintg_view_delete_trg
  INSTEAD OF DELETE ON gv_gorintg
BEGIN
  gb_partner_rule.p_delete
    (p_integration_cde => :OLD.gorintg_integration_cde,
     p_rowid => :OLD.gorintg_v_rowid);
END;
/
