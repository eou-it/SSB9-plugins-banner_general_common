--/*******************************************************************************
--Copyright 2013 Ellucian Company L.P. and its affiliates.
--*******************************************************************************/
--
-- gv_gorcmsc.sql
--
-- AUDIT TRAIL: 9.0
-- DBEU 06/06/2013
--
--    Generated view for Horizon API support
--
-- AUDIT TRAIL END
--
CREATE OR REPLACE FORCE VIEW gv_gorcmsc AS SELECT
      gorcmsc_cmsc_code,
      gorcmsc_online_match_ind,
      gorcmsc_entity_cde,
      gorcmsc_atyp_code,
      gorcmsc_tele_code,
      gorcmsc_emal_code,
      gorcmsc_transpose_date_ind,
      gorcmsc_transpose_name_ind,
      gorcmsc_alias_wildcard_ind,
      gorcmsc_length_override_ind,
      gorcmsc_api_failure_ind,
      gorcmsc_surrogate_id,
      gorcmsc_version,
      gorcmsc_user_id,
      gorcmsc_data_origin,
      gorcmsc_activity_date,
      ROWID gorcmsc_v_rowid
  FROM gorcmsc;
--
CREATE OR REPLACE PUBLIC SYNONYM gv_gorcmsc FOR gv_gorcmsc;
