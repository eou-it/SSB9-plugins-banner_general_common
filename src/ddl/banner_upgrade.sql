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
