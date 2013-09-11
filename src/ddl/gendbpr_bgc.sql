-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM gendbpr_bgc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Banner XE
REM Create views.
REM AUDIT TRAIL END
REM
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect baninst1/&&baninst1_password
REM
start gv_gorintg_del_trg
start gv_gorintg_ins_trg
start gv_gorintg_upd_trg
start gv_gorcmsc_del_trg
start gv_gorcmsc_ins_trg
start gv_gorcmsc_upd_trg
start gv_gorcmsp_del_trg
start gv_gorcmsp_ins_trg
start gv_gorcmsp_upd_trg
start gv_gorcmdo_del_trg
start gv_gorcmdo_ins_trg
start gv_gorcmdo_upd_trg

start gv_gorvisa_del_trg
start gv_gorvisa_ins_trg
start gv_gorvisa_upd_trg

start gv_gobqstn_ins_trg
start gv_gobqstn_upd_trg
start gv_gobqstn_del_trg
start gv_gobansr_upd_trg
start gv_gobansr_ins_trg
start gv_gobansr_del_trg
