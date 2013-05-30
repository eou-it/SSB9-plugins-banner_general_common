-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM sv_sordegr_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER sordegr_view_create_trg
  INSTEAD OF INSERT ON sv_sordegr
DECLARE
  p_rowid_v VARCHAR2(100);
  p_degr_seq_no sordegr.sordegr_degr_seq_no%TYPE;
BEGIN
  gfksjpa.setId(:NEW.sordegr_surrogate_id);
  gfksjpa.setVersion(:NEW.sordegr_version);
  p_degr_seq_no := :NEW.sordegr_degr_seq_no;

  gb_pcol_degree.p_create (
     p_pidm              => :NEW.sordegr_pidm             ,
     p_sbgi_code         => :NEW.sordegr_sbgi_code        ,
     p_degc_code         => :NEW.sordegr_degc_code        ,
     p_attend_from       => :NEW.sordegr_attend_from      ,
     p_attend_to         => :NEW.sordegr_attend_to        ,
     p_hours_transferred => :NEW.sordegr_hours_transferred,
     p_gpa_transferred   => :NEW.sordegr_gpa_transferred  ,
     p_degc_date         => :NEW.sordegr_degc_date        ,
     p_degc_year         => :NEW.sordegr_degc_year        ,
     p_coll_code         => :NEW.sordegr_coll_code        ,
     p_honr_code         => :NEW.sordegr_honr_code        ,
     p_term_degree       => :NEW.sordegr_term_degree      ,
     p_egol_code         => :NEW.sordegr_egol_code        ,
     p_primary_ind       => :NEW.sordegr_primary_ind      ,
     p_data_origin       => :NEW.sordegr_data_origin      ,
     p_user_id           => :NEW.sordegr_user_id          ,
     p_degr_seq_no_inout => p_degr_seq_no                 ,
     p_rowid_out         => p_rowid_v);
END;
/
show errors