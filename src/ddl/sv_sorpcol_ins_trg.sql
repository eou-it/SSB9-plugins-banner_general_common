-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorpcol_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorpcol_view_create_trg
  INSTEAD OF INSERT ON sv_sorpcol
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.sorpcol_surrogate_id);
  gfksjpa.setVersion(:NEW.sorpcol_version);

  gb_prior_college.p_create (
     p_pidm            => :NEW.sorpcol_pidm           ,
     p_sbgi_code       => :NEW.sorpcol_sbgi_code      ,
     p_trans_recv_date => :NEW.sorpcol_trans_recv_date,
     p_trans_rev_date  => :NEW.sorpcol_trans_rev_date ,
     p_official_trans  => :NEW.sorpcol_official_trans ,
     p_admr_code       => :NEW.sorpcol_admr_code      ,
     p_data_origin     => :NEW.sorpcol_data_origin    ,
     p_user_id         => :NEW.sorpcol_user_id        ,
     p_rowid_out       => p_rowid_v);
END;
/
show errors