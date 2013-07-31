-- *****************************************************************************************
-- * Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.                    *
-- * This copyrighted software contains confidential and proprietary information of        *
-- * SunGard Higher Education and its subsidiaries. Any use of this software is limited    *
-- * solely to SunGard Higher Education licensees, and is further subject to the terms     *
-- * and conditions of one or more written license agreements between SunGard Higher       *
-- * Education and the licensee in question. SunGard is either a registered trademark or   *
-- * trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.*
-- * Banner and Luminis are either registered trademarks or trademarks of SunGard Higher   *
-- * Education in the U.S.A. and/or other regions and/or countries.                        *
-- *****************************************************************************************
REM
REM banner_upgrade.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Main common project schema maintenance script.
REM AUDIT TRAIL END
REM
set scan on echo on termout on;
spool horizon_upgrade_bgc.lis
connect bansecr/&&bansecr_password
 insert into gurucls ( gurucls_userid, gurucls_class_code, gurucls_activity_date,
                      gurucls_user_id)
  select  'GRAILS_USER', 'BAN_PAYROLL_C', sysdate, user
    from dual
   where not exists (select 1 from gurucls
                      where gurucls_userid = 'GRAILS_USER'
                        and gurucls_class_code = 'BAN_PAYROLL_C');
REM  Create tables
connect general/&&general_password
start grevtab_bgc

REM Security scripts
connect bansecr/&&bansecr_password
start gurinfo_bs_090000.sql

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
REM
REM Recompile invalid objects before doing seed data
REM
conn sys/u_pick_it as sysdba
execute utl_recomp.recomp_parallel();
start showinv
spool off;
