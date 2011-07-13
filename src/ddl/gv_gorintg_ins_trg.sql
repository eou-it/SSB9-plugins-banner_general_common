CREATE OR REPLACE TRIGGER gorintg_view_create_trg
  INSTEAD OF INSERT ON gv_gorintg
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorintg_surrogate_id);
  gfksjpa.setVersion(:NEW.gorintg_version);
  gb_partner_rule.p_create
    (p_integration_cde => :NEW.gorintg_integration_cde,
     p_desc => :NEW.gorintg_desc,
     p_intp_code => :NEW.gorintg_intp_code,
     p_user_id => :NEW.gorintg_user_id,
     p_data_origin => :NEW.gorintg_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
