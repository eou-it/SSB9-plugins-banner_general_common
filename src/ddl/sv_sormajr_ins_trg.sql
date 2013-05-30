-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sormajr_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sormajr_view_create_trg
  INSTEAD OF INSERT ON sv_sormajr
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.sormajr_surrogate_id);
  gfksjpa.setVersion(:NEW.sormajr_version);

  gb_pcol_major.p_create (
     p_pidm            => :NEW.sormajr_pidm           ,
     p_sbgi_code       => :NEW.sormajr_sbgi_code      ,
     p_degc_code       => :NEW.sormajr_degc_code      ,
     p_degr_seq_no     => :NEW.sormajr_degr_seq_no    ,
     p_majr_code_major => :NEW.sormajr_majr_code_major,
     p_data_origin     => :NEW.sormajr_data_origin    ,
     p_user_id         => :NEW.sormajr_user_id        ,
     p_rowid_out       => p_rowid_v);
END;
/
show errors