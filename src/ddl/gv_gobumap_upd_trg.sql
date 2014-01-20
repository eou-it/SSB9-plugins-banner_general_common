-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
--
-- gv_gobumap_upd_trg.sql
--
--    Generated view for BXE API support
--
--
CREATE OR REPLACE TRIGGER gobumap_view_update_trg
  INSTEAD OF UPDATE ON gv_gobumap
BEGIN
  gfksjpa.setId(:OLD.gobumap_surrogate_id);
  gfksjpa.setVersion(:NEW.gobumap_version);
  gb_gobumap.p_update
    (p_udc_id => :NEW.gobumap_udc_id,
     p_pidm => :NEW.gobumap_pidm,
     p_create_date => :NEW.gobumap_create_date,
     p_user_id => :NEW.gobumap_user_id,
     p_data_origin => :NEW.gobumap_data_origin,
     p_rowid => :NEW.gobumap_v_rowid);
END;
/
show errors
