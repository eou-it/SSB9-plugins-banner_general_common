REM 
REM /*******************************************************************************
REM Copyright 2013 Ellucian Company L.P. and its affiliates.
REM *******************************************************************************/
REM 
REM  gv_gorcmsp_del_trg.sql
REM 
REM  AUDIT TRAIL: 9.0
REM  DBEU 06/06/2013
REM 
REM     Generated trigger for Banner XE API support
REM 
REM  AUDIT TRAIL END
REM 
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
