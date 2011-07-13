CREATE OR REPLACE TRIGGER baninst1.ssrmeet_view_delete_trg
  INSTEAD OF DELETE ON baninst1.sv_ssrmeet
BEGIN
  gb_classtimes.p_delete
    (p_rowid => :OLD.ssrmeet_v_rowid);
END;
/
