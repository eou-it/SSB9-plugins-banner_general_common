-- dbeu_ext_gen_bgc.sql
-- V8.1
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
REM dbeu_ext_gen_bgc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated for Horizon API support
REM AUDIT TRAIL END
REM
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect dbeu_owner/&&dbeu_password
execute dbeu_util.extend_table('GENERAL','GORINTG','G',TRUE);
execute dbeu_util.extend_table('GENERAL','GURMAIL','G',FALSE);
execute dbeu_util.extend_table('PAYROLL','PTRTENR','P',FALSE);

execute dbeu_util.extend_table('GENERAL','GORRSQL','G',FALSE);
execute dbeu_util.extend_table('GENERAL','GORCMSC','G',TRUE);
execute dbeu_util.extend_table('GENERAL','GORCMDO','G',TRUE);
execute dbeu_util.extend_table('GENERAL','GORCMSP','G',TRUE);
execute dbeu_util.extend_table('GENERAL','GOTCMME','G',FALSE);
execute dbeu_util.extend_table('GENERAL','GURINFO','G',FALSE);

