-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
--
-- gv_gobumap_ins_trg.sql
--
--    Generated view for BXE API support
--
--
CREATE OR REPLACE TRIGGER gobumap_view_create_trg
  INSTEAD OF INSERT ON gv_gobumap
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gobumap_surrogate_id);
  gfksjpa.setVersion(:NEW.gobumap_version);
  gb_gobumap.p_create
    (p_udc_id => :NEW.gobumap_udc_id,
     p_pidm => :NEW.gobumap_pidm,
     p_create_date => :NEW.gobumap_create_date,
     p_user_id => :NEW.gobumap_user_id,
     p_data_origin => :NEW.gobumap_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
show errors
