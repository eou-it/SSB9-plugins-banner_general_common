--
-- dbeu_table_extends.sql
--
-- V8.1
--
-- *****************************************************************************
-- *                                                                           *
-- * Copyright 2011 SunGard. All rights reserved.                              *
-- *                                                                           *
-- * SunGard or its subsidiaries in the U.S. and other countries is the owner  *
-- * of numerous marks, including 'SunGard,' the SunGard logo, 'Banner,'       *
-- * 'PowerCAMPUS,' 'Advance,' 'Luminis,' 'UDC,' and 'Unified Digital Campus.' *
-- * Other names and marks used in this material are owned by third parties.   *
-- *                                                                           *
-- * This [site/software] contains confidential and proprietary information of *
-- * SunGard and its subsidiaries. Use of this [site/software] is limited to   *
-- * SunGard Higher Education licensees, and is subject to the terms and       *
-- * conditions of one or more written license agreements between SunGard      *
-- * Higher Education and the licensee in question.                            *
-- *                                                                           *
-- *****************************************************************************
--
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect dbeu_owner/&&dbeu_password
execute dbeu_util.extend_table('PAYROLL','PTRTENR','P',FALSE);
