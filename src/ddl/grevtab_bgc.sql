-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************REM
REM Upgrade GENERAL schema.
REM

whenever oserror exit rollback;
whenever sqlerror exit rollback;

start mmedinx.sql
start msmlinx.sql
start msmltab.sql
