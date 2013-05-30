-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sordegr_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sordegr_view_delete_trg
  INSTEAD OF DELETE ON sv_sordegr
BEGIN
  gb_pcol_degree.p_delete (
     p_pidm            => :OLD.sordegr_pidm       ,
     p_sbgi_code       => :OLD.sordegr_sbgi_code  ,
     p_degc_code       => :OLD.sordegr_degc_code  ,
     p_degr_seq_no     => :OLD.sordegr_degr_seq_no,
     p_rowid           => :OLD.sordegr_v_rowid);
END;
/
show errors