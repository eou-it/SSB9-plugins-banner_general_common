CREATE OR REPLACE TRIGGER slbrdef_view_delete_trg
  INSTEAD OF DELETE ON sv_slbrdef
BEGIN
  gb_roomdefinition.p_delete
    (p_bldg_code => :OLD.slbrdef_bldg_code,
     p_room_number => :OLD.slbrdef_room_number,
     p_term_code_eff => :OLD.slbrdef_term_code_eff,
     p_rowid => :OLD.slbrdef_v_rowid);
END;
/
