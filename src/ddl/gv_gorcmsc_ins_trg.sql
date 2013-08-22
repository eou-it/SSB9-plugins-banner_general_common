-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


--
/*******************************************************************************
Copyright 2013 Ellucian Company L.P. and its affiliates.
*******************************************************************************/
--
-- gv_gorcmsc_ins_trg.sql
--
-- AUDIT TRAIL: 9.0
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmsc_view_create_trg
  INSTEAD OF INSERT ON gv_gorcmsc
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorcmsc_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmsc_version);
  gb_cm_source_rules.p_create
    (p_cmsc_code => :NEW.gorcmsc_cmsc_code,
     p_online_match_ind => :NEW.gorcmsc_online_match_ind,
     p_entity_cde => :NEW.gorcmsc_entity_cde,
     p_user_id => :NEW.gorcmsc_user_id,
     p_atyp_code => :NEW.gorcmsc_atyp_code,
     p_tele_code => :NEW.gorcmsc_tele_code,
     p_emal_code => :NEW.gorcmsc_emal_code,
     p_data_origin => :NEW.gorcmsc_data_origin,
     p_transpose_date_ind => :NEW.gorcmsc_transpose_date_ind,
     p_transpose_name_ind => :NEW.gorcmsc_transpose_name_ind,
     p_alias_wildcard_ind => :NEW.gorcmsc_alias_wildcard_ind,
     p_length_override_ind => :NEW.gorcmsc_length_override_ind,
     p_api_failure_ind => :NEW.gorcmsc_api_failure_ind,
     p_rowid_out => p_rowid_v);
END;
/
show errors
