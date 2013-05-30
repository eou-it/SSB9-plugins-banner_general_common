-- *****************************************************************************************
-- * Copyright 2009-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
REM
REM dbeu_ext_stu_bgc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Generated for Horizon API support
REM AUDIT TRAIL END
REM
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect dbeu_owner/&&dbeu_password
execute dbeu_util.extend_table('SATURN','SLBBLDG','S',TRUE);
execute dbeu_util.extend_table('SATURN','SLBRDEF','S',TRUE);
execute dbeu_util.extend_table('SATURN','SLRRDEF','S',TRUE);
execute dbeu_util.extend_table('SATURN','SLRRUSE','S',FALSE);
execute dbeu_util.extend_table('SATURN','SSRMEET','S',TRUE);
execute dbeu_util.extend_table('SATURN','SSRXLST','S',FALSE);
execute dbeu_util.extend_table('SATURN','SOBSEQN','S',FALSE);
execute dbeu_util.extend_table('SATURN','SLRBCAT','S',FALSE);

execute dbeu_util.extend_table('SATURN','SOBSBGI','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBCMT','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBCNT','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBACD','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBDMO','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBETH','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBCHR','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBDEG','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBDPL','S',FALSE);
execute dbeu_util.extend_table('SATURN','SORBTST','S',FALSE);

execute dbeu_util.extend_table('SATURN','SORPCOL','S',TRUE);
execute dbeu_util.extend_table('SATURN','SORCONC','S',TRUE);
execute dbeu_util.extend_table('SATURN','SORDEGR','S',TRUE);
execute dbeu_util.extend_table('SATURN','SORMAJR','S',TRUE);
execute dbeu_util.extend_table('SATURN','SORMINR','S',TRUE);
