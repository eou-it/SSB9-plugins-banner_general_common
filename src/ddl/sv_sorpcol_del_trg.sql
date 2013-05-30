-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorpcol_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorpcol_view_delete_trg
  INSTEAD OF DELETE ON sv_sorpcol
BEGIN
  gb_prior_college.p_delete (
     p_pidm      => :OLD.sorpcol_pidm     ,
     p_sbgi_code => :OLD.sorpcol_sbgi_code,
     p_rowid     => :OLD.sorpcol_v_rowid);
END;
/
show errors