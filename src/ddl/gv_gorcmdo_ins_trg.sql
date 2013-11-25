REM *****************************************************************************************
REM * Copyright 2013 Ellucian Company L.P. and its affiliates.                              *
REM *****************************************************************************************
REM gv_gorcmdo_ins_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM RR 10-JUN-2013
REM 1. Generated trigger for Horizon API support. 
REM AUDIT TRAIL END 
REM

CREATE OR REPLACE TRIGGER gorcmdo_view_create_trg
  INSTEAD OF INSERT ON gv_gorcmdo
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.gorcmdo_surrogate_id);
  gfksjpa.setVersion(:NEW.gorcmdo_version);
  gb_cm_display_options.p_create
    (p_cmsc_code => :NEW.gorcmdo_cmsc_code,
     p_objs_name => :NEW.gorcmdo_objs_name,
     p_seq_no => :NEW.gorcmdo_seq_no,
     p_user_id => :NEW.gorcmdo_user_id,
     p_data_origin => :NEW.gorcmdo_data_origin,
     p_rowid_out => p_rowid_v);
END;
/
show errors
