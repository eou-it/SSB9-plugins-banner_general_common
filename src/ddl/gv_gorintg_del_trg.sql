CREATE OR REPLACE TRIGGER gorintg_view_delete_trg
  INSTEAD OF DELETE ON gv_gorintg
BEGIN
  gb_partner_rule.p_delete
    (p_integration_cde => :OLD.gorintg_integration_cde,
     p_rowid => :OLD.gorintg_v_rowid);
END;
/
