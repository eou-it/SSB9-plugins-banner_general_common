-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmdo_del_trg.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated trigger for Banner XE API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE TRIGGER gorcmdo_view_delete_trg
  INSTEAD OF DELETE ON gv_gorcmdo
BEGIN
  gb_cm_display_options.p_delete
    (p_cmsc_code => :OLD.gorcmdo_cmsc_code,
     p_objs_name => :OLD.gorcmdo_objs_name,
     p_rowid => :OLD.gorcmdo_v_rowid);
END;
/
show errors
