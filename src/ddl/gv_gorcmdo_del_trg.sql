REM *****************************************************************************************
REM * Copyright 2013 Ellucian Company L.P. and its affiliates.                              *
REM *****************************************************************************************
REM gv_gorcmdo_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM RR 10-JUN-2013
REM 1. Generated trigger for Horizon API support. 
REM AUDIT TRAIL END 
REM

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
