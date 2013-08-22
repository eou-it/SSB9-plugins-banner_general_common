--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmsp_ins_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmsp_view_create_trg
  INSTEAD OF INSERT ON gv_gorcmsp
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorcmsp_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmsp_version);
  gb_cm_source_priority.p_create
    (p_cmsc_code => :NEW.gorcmsp_cmsc_code,
     p_priority_no => :NEW.gorcmsp_priority_no,
     p_desc => :NEW.gorcmsp_desc,
     p_user_id => :NEW.gorcmsp_user_id,
     p_data_origin => :NEW.gorcmsp_data_origin,
     p_long_desc => :NEW.gorcmsp_long_desc,
     p_rowid_out => p_rowid_v);
END;
/
show errors
