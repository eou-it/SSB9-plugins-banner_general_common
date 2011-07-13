--
--  Main common project schema maintenance script.
--

set scan on echo on termout on;

REM spool horizon_upgrade.lis


connect dbeu_owner/&&dbeu_password

start dbeu_ext_gen_bgc
start dbeu_ext_stu_bgc


connect baninst1/&&baninst1_password

start genview_bgc
start stuview_bgc


connect baninst1/&&baninst1_password
start gendbpr_bgc

connect baninst1/&&baninst1_password
start studbpr_bgc

spool off;
