--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmsp_del_trg.sql
--
-- AUDIT TRAIL: 9.0
-- DBEU 06/06/2013
--
--    Generated trigger for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmsp_view_delete_trg
  INSTEAD OF DELETE ON gv_gorcmsp
BEGIN
  gb_cm_source_priority.p_delete
    (p_cmsc_code => :OLD.gorcmsp_cmsc_code,
     p_priority_no => :OLD.gorcmsp_priority_no,
     p_rowid => :OLD.gorcmsp_v_rowid);
END;
/
show errors
