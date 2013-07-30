-- dbeu_ext_gen_bgc.sql
-- V8.1
-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
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
execute dbeu_util.extend_table('GENERAL','GOTCMME','G',FALSE)

execute dbeu_util.extend_table('GENERAL','GORVISA','G',TRUE)
execute dbeu_util.extend_table('GENERAL','GORDOCM','G',FALSE)
execute dbeu_util.extend_table('GENERAL','GOBINTL','G',FALSE)

execute dbeu_util.extend_table('GENERAL','GORDMSK','G',TRUE)

