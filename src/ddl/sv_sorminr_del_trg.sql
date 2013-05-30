-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorminr_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorminr_view_delete_trg
  INSTEAD OF DELETE ON sv_sorminr
BEGIN
  gb_pcol_minor.p_delete (
     p_pidm            => :OLD.sorminr_pidm           ,
     p_sbgi_code       => :OLD.sorminr_sbgi_code      ,
     p_degc_code       => :OLD.sorminr_degc_code      ,
     p_degr_seq_no     => :OLD.sorminr_degr_seq_no    ,
     p_majr_code_minor => :OLD.sorminr_majr_code_minor,
     p_rowid           => :OLD.sorminr_v_rowid);
END;
/
show errors