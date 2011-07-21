--
--  Main common project schema maintenance script.
--

set scan on echo on termout on;

REM spool horizon_upgrade.lis

connect bansecr/&&bansecr_password

 insert into gurucls ( gurucls_userid, gurucls_class_code, gurucls_activity_date,
                      gurucls_user_id)
  select  'GRAILS_USER', 'BAN_PAYROLL_C', sysdate, user
    from dual
   where not exists (select 1 from gurucls
                      where gurucls_userid = 'GRAILS_USER'
                        and gurucls_class_code = 'BAN_PAYROLL_C');

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
