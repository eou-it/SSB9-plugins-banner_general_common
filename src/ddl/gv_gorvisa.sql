-- *****************************************************************************
-- * Copyright 2013 Ellucian Company L.P. and its affiliates.                  *
-- *****************************************************************************
REM
REM gv_gorvisa.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Generated view for Banner XE API support
REM AUDIT TRAIL END
REM
CREATE OR REPLACE FORCE VIEW gv_gorvisa AS SELECT
      gorvisa_pidm,
      gorvisa_seq_no,
      gorvisa_vtyp_code,
      gorvisa_visa_number,
      gorvisa_natn_code_issue,
      gorvisa_viss_code,
      gorvisa_visa_start_date,
      gorvisa_visa_expire_date,
      gorvisa_entry_ind,
      gorvisa_visa_req_date,
      gorvisa_visa_issue_date,
      gorvisa_pent_code,
      gorvisa_no_entries,
      gorvisa_user_id,
      gorvisa_activity_date,
      gorvisa_data_origin,
      gorvisa_surrogate_id,
      gorvisa_version,
      ROWID gorvisa_v_rowid
  FROM gorvisa;
CREATE OR REPLACE PUBLIC SYNONYM gv_gorvisa FOR gv_gorvisa;
