--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmsp_upd_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmsp_view_update_trg
  INSTEAD OF UPDATE ON gv_gorcmsp
BEGIN
  gfksjpa.setId(:OLD.gorcmsp_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmsp_version);
  gb_cm_source_priority.p_update
    (p_cmsc_code => :NEW.gorcmsp_cmsc_code,
     p_priority_no => :NEW.gorcmsp_priority_no,
     p_desc => :NEW.gorcmsp_desc,
     p_user_id => :NEW.gorcmsp_user_id,
     p_data_origin => :NEW.gorcmsp_data_origin,
     p_long_desc => :NEW.gorcmsp_long_desc,
     p_rowid => :NEW.gorcmsp_v_rowid);
END;
/
show errors
