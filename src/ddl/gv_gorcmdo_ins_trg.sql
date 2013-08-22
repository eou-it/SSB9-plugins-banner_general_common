
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmdo_ins_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmdo_view_create_trg
  INSTEAD OF INSERT ON gv_gorcmdo
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorcmdo_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmdo_version);
  gb_cm_display_options.p_create
    (p_cmsc_code => :NEW.gorcmdo_cmsc_code,
     p_objs_name => :NEW.gorcmdo_objs_name,
     p_seq_no => :NEW.gorcmdo_seq_no,
     p_user_id => :NEW.gorcmdo_user_id,
     p_data_origin => :NEW.gorcmdo_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
show errors
