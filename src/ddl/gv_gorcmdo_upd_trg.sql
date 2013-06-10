--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmdo_upd_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated trigger for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmdo_view_update_trg
  INSTEAD OF UPDATE ON gv_gorcmdo
BEGIN
  gfksjpa.setId(:OLD.gorcmdo_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmdo_version);
  gb_cm_display_options.p_update
    (p_cmsc_code => :NEW.gorcmdo_cmsc_code,
     p_objs_name => :NEW.gorcmdo_objs_name,
     p_seq_no => :NEW.gorcmdo_seq_no,
     p_user_id => :NEW.gorcmdo_user_id,
     p_data_origin => :NEW.gorcmdo_data_origin,
     p_rowid => :NEW.gorcmdo_v_rowid);
END;
/
show errors
