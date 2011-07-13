CREATE OR REPLACE FORCE VIEW gv_gorintg AS SELECT
      gorintg_integration_cde,
      gorintg_desc,
      gorintg_intp_code,
      gorintg_surrogate_id,
      gorintg_version,
      gorintg_user_id,
      gorintg_data_origin,
      gorintg_activity_date,
      ROWID gorintg_v_rowid
  FROM general.gorintg;
CREATE OR REPLACE PUBLIC SYNONYM gv_gorintg FOR gv_gorintg;
