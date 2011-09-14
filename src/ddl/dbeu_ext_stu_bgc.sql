--
-- dbeu_table_extends.sql
--
-- V8.1
--
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
