-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM gv_gorvisa_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Banner XE
REM Generated trigger for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER gorvisa_view_delete_trg
  INSTEAD OF DELETE ON gv_gorvisa
BEGIN
  gb_visa.p_delete (
     p_pidm            => :OLD.gorvisa_pidm     ,
     p_seq_no          => :OLD.gorvisa_seq_no   ,
     p_vtyp_code       => :OLD.gorvisa_vtyp_code,
     p_rowid           => :OLD.gorvisa_v_rowid);END;
/
show errors