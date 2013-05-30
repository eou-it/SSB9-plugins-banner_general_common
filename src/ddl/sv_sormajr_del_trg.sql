-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sormajr_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sormajr_view_delete_trg
  INSTEAD OF DELETE ON sv_sormajr
BEGIN
  gb_pcol_major.p_delete (
     p_pidm            => :OLD.sormajr_pidm           ,
     p_sbgi_code       => :OLD.sormajr_sbgi_code      ,
     p_degc_code       => :OLD.sormajr_degc_code      ,
     p_degr_seq_no     => :OLD.sormajr_degr_seq_no    ,
     p_majr_code_major => :OLD.sormajr_majr_code_major,
     p_rowid           => :OLD.sormajr_v_rowid);
END;
/
show errors