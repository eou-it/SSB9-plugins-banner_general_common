--
--  Create views.
--
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect baninst1/&&baninst1_password
REM
REM Create triggers
REM

start sv_slbbldg_del_trg
start sv_slbbldg_ins_trg
start sv_slbbldg_upd_trg

start sv_slbrdef_del_trg
start sv_slbrdef_ins_trg
start sv_slbrdef_upd_trg

start sv_ssrmeet_del_trg
start sv_ssrmeet_ins_trg
start sv_ssrmeet_upd_trg
