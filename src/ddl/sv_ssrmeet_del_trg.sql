-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM sv_ssrmeet_del_trg.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated trigger for Horizon API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE TRIGGER ssrmeet_view_delete_trg
  INSTEAD OF DELETE ON sv_ssrmeet
BEGIN
  gb_classtimes.p_delete
    (p_rowid => :OLD.ssrmeet_v_rowid);
END;
/
