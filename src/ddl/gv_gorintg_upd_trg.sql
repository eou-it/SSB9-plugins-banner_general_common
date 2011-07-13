CREATE OR REPLACE TRIGGER gorintg_view_update_trg
  INSTEAD OF UPDATE ON gv_gorintg
BEGIN
  gfksjpa.setId(:OLD.gorintg_surrogate_id);
  gfksjpa.setVersion(:NEW.gorintg_version);
  gb_partner_rule.p_update
    (p_integration_cde => :NEW.gorintg_integration_cde,
     p_desc => :NEW.gorintg_desc,
     p_intp_code => :NEW.gorintg_intp_code,
     p_user_id => :NEW.gorintg_user_id,
     p_data_origin => :NEW.gorintg_data_origin,
     p_rowid => :NEW.gorintg_v_rowid);
END;
/
