--
--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmsp.sql
--
-- AUDIT TRAIL: 9.0
-- DBEU 06/06/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gorcmsp AS SELECT
      gorcmsp_cmsc_code,
      gorcmsp_priority_no,
      gorcmsp_desc,
      gorcmsp_long_desc,
      gorcmsp_surrogate_id,
      gorcmsp_version,
      gorcmsp_user_id,
      gorcmsp_data_origin,
      gorcmsp_activity_date,
      ROWID gorcmsp_v_rowid
  FROM gorcmsp;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gorcmsp FOR gv_gorcmsp;
