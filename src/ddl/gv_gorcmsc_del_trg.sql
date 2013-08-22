--
/*******************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--
-- gv_gorcmsc_del_trg.sql
--
-- AUDIT TRAIL: 9.0
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmsc_view_delete_trg
  INSTEAD OF DELETE ON gv_gorcmsc
BEGIN
  gb_cm_source_rules.p_delete
    (p_cmsc_code => :OLD.gorcmsc_cmsc_code,
     p_rowid => :OLD.gorcmsc_v_rowid);
END;
/
show errors
