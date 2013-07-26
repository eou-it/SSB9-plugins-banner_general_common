-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM gv_gorvisa_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER gorvisa_view_create_trg
  INSTEAD OF INSERT ON gv_gorvisa
DECLARE
  p_rowid_v VARCHAR2(100);
  p_seq_no gorvisa.gorvisa_seq_no%TYPE;
BEGIN
  gfksjpa.setId(:NEW.gorvisa_surrogate_id);
  gfksjpa.setVersion(:NEW.gorvisa_version);
  p_seq_no := :NEW.gorvisa_seq_no;

  gb_visa.p_create (
     p_pidm             => :NEW.gorvisa_pidm            ,
     p_vtyp_code        => :NEW.gorvisa_vtyp_code       ,
     p_visa_number      => :NEW.gorvisa_visa_number     ,
     p_natn_code_issue  => :NEW.gorvisa_natn_code_issue ,
     p_viss_code        => :NEW.gorvisa_viss_code       ,
     p_visa_start_date  => :NEW.gorvisa_visa_start_date ,
     p_visa_expire_date => :NEW.gorvisa_visa_expire_date,
     p_entry_ind        => :NEW.gorvisa_entry_ind       ,
     p_user_id          => :NEW.gorvisa_user_id         ,
     p_visa_req_date    => :NEW.gorvisa_visa_req_date   ,
     p_visa_issue_date  => :NEW.gorvisa_visa_issue_date ,
     p_pent_code        => :NEW.gorvisa_pent_code       ,
     p_no_entries       => :NEW.gorvisa_no_entries      ,
     p_data_origin      => :NEW.gorvisa_data_origin     ,
     p_seq_no_inout     => p_seq_no                     ,
     p_rowid_out        => p_rowid_v);
END;
/
show errors