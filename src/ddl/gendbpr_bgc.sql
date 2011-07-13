--
--  Create views.
--
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect baninst1/&&baninst1_password

start gv_gorintg_del_trg
start gv_gorintg_ins_trg
start gv_gorintg_upd_trg
