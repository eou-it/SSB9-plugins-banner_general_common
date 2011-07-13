CREATE OR REPLACE TRIGGER slbbldg_view_delete_trg
  INSTEAD OF DELETE ON sv_slbbldg
BEGIN
  gb_bldgdefinition.p_delete
    (p_bldg_code => :OLD.slbbldg_bldg_code,
     p_rowid => :OLD.slbbldg_v_rowid);
END;
/
