-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************REM
REM Upgrade GENERAL schema.
REM
set echo off
whenever oserror exit rollback;
whenever sqlerror exit rollback;
connect general/&&general_password

start msmltab.sql
start msmlinx.sql


start gurinfo_teardown_ext.sql
start gurinfo_090000_01.sql
start gurinfo_090000_02.sql


