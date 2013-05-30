-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sorminr_upd_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sorminr_view_update_trg
  INSTEAD OF UPDATE ON sv_sorminr
BEGIN
  gfksjpa.setId(:OLD.sorminr_surrogate_id);
  gfksjpa.setVersion(:NEW.sorminr_version);

  gb_pcol_minor.p_update (
     p_pidm            => :NEW.sorminr_pidm           ,
     p_sbgi_code       => :NEW.sorminr_sbgi_code      ,
     p_degc_code       => :NEW.sorminr_degc_code      ,
     p_degr_seq_no     => :NEW.sorminr_degr_seq_no    ,
     p_majr_code_minor => :NEW.sorminr_majr_code_minor,
     p_data_origin     => :NEW.sorminr_data_origin    ,
     p_user_id         => :NEW.sorminr_user_id        ,
     p_rowid           => :NEW.sorminr_v_rowid);
END;
/
show errors