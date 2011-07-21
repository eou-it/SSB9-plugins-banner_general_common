--
--  Main common project schema maintenance script.
--

set scan on echo on termout on;

REM spool horizon_upgrade.lis

connect dbeu_owner/&&dbeu_password

start dbeu_ext_gen

spool off;
