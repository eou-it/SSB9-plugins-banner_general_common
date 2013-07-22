-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM studbpr_bgc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated view for Horizon API support
REM AUDIT TRAIL END
REM
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM
REM Create triggers
REM
start sv_slbbldg_del_trg
start sv_slbbldg_ins_trg
start sv_slbbldg_upd_trg
REM
start sv_slbrdef_del_trg
start sv_slbrdef_ins_trg
start sv_slbrdef_upd_trg
REM
start sv_ssrmeet_del_trg
start sv_ssrmeet_ins_trg
start sv_ssrmeet_upd_trg
REM
start sv_sorpcol_ins_trg
start sv_sorpcol_upd_trg
start sv_sorpcol_del_trg
REM
start sv_sorconc_ins_trg
start sv_sorconc_upd_trg
start sv_sorconc_del_trg
REM
start sv_sordegr_ins_trg
start sv_sordegr_upd_trg
start sv_sordegr_del_trg
REM
start sv_sormajr_ins_trg
start sv_sormajr_upd_trg
start sv_sormajr_del_trg
REM
start sv_sorminr_ins_trg
start sv_sorminr_upd_trg
start sv_sorminr_del_trg
