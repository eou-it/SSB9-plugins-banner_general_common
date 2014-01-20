-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
--
-- gv_gobumap_del_trg.sql
--
--    Generated view for BXE API support
--
--
CREATE OR REPLACE TRIGGER gobumap_view_delete_trg
  INSTEAD OF DELETE ON gv_gobumap
BEGIN
  gb_gobumap.p_delete
    (p_udc_id => :OLD.gobumap_udc_id,
     p_rowid => :OLD.gobumap_v_rowid);
END;
/
show errors
