-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorconc_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorconc_view_create_trg
  INSTEAD OF INSERT ON sv_sorconc
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.sorconc_surrogate_id);
  gfksjpa.setVersion(:NEW.sorconc_version);

  gb_pcol_concentration.p_create (
     p_pidm            => :NEW.sorconc_pidm          ,
     p_sbgi_code       => :NEW.sorconc_sbgi_code     ,
     p_degc_code       => :NEW.sorconc_degc_code     ,
     p_degr_seq_no     => :NEW.sorconc_degr_seq_no   ,
     p_majr_code_conc  => :NEW.sorconc_majr_code_conc,
     p_data_origin     => :NEW.sorconc_data_origin   ,
     p_user_id         => :NEW.sorconc_user_id       ,
     p_rowid_out       => p_rowid_v);
END;
/
show errors