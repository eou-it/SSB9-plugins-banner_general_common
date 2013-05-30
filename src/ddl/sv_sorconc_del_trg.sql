-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorconc_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorconc_view_delete_trg
  INSTEAD OF DELETE ON sv_sorconc
BEGIN
  gb_pcol_concentration.p_delete (
     p_pidm            => :OLD.sorconc_pidm                  ,
     p_sbgi_code       => :OLD.sorconc_sbgi_code             ,
     p_degc_code       => :OLD.sorconc_degc_code     ,
     p_degr_seq_no     => :OLD.sorconc_degr_seq_no   ,
     p_majr_code_conc  => :OLD.sorconc_majr_code_conc,
     p_rowid           => :OLD.sorconc_v_rowid);
END;
/
show errors