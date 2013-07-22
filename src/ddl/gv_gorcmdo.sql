--
-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


--
-- gv_gorcmdo.sql
--
-- AUDIT TRAIL: 8.x
-- DBEU 06/06/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gorcmdo AS SELECT
      gorcmdo_cmsc_code,
      gorcmdo_objs_name,
      gorcmdo_seq_no,
      gorcmdo_surrogate_id,
      gorcmdo_version,
      gorcmdo_user_id,
      gorcmdo_data_origin,
      gorcmdo_activity_date,
      ROWID gorcmdo_v_rowid
  FROM gorcmdo;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gorcmdo FOR gv_gorcmdo;
