-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM gv_gorvisa_upd_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 05/15/2013
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER gorvisa_view_update_trg
  INSTEAD OF UPDATE ON gv_gorvisa
BEGIN
  gfksjpa.setId(:OLD.gorvisa_surrogate_id);
  gfksjpa.setVersion(:NEW.gorvisa_version);

  gb_visa.p_update (
     p_pidm             => :NEW.gorvisa_pidm            ,
     p_seq_no           => :NEW.gorvisa_seq_no          ,
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
     p_rowid            => :NEW.gorvisa_v_rowid);
END;
/
show errors