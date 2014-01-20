-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
--
-- gv_gobumap.sql
--
--    Generated view for BXE API support
--
--
CREATE OR REPLACE FORCE VIEW gv_gobumap AS SELECT
      gobumap_udc_id,
      gobumap_pidm,
      gobumap_create_date,
      gobumap_surrogate_id,
      gobumap_version,
      gobumap_user_id,
      gobumap_data_origin,
      gobumap_activity_date,
      ROWID gobumap_v_rowid
  FROM gobumap;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gobumap FOR gv_gobumap;
