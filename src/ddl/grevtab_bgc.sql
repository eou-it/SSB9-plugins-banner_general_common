-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************REM
REM Upgrade GENERAL schema.
REM
set echo off
whenever oserror exit rollback;
whenever sqlerror exit rollback;

start mmedinx.sql
start msmlinx.sql
start msmltab.sql

start gurinfo_teardown_ext.sql

connect general/&&general_password
start gurinfo_080602_01.sql
start gurinfo_080602_02.sql
start gurinfo_080602_03.sql


