-- **************************************************************************************
-- * Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.                 *
-- * This copyrighted software contains confidential and proprietary information of     *
-- * SunGard Higher Education and its subsidiaries. Any use of this software is limited *
-- * solely to SunGard Higher Education licensees, and is further subject to the terms  *
-- * and conditions of one or more written license agreements between SunGard Higher    *
-- * Education and the licensee in question. SunGard, Banner and Luminis are either     *
-- * registered trademarks or trademarks of SunGard Higher Education in the U.S.A.      *
-- * and/or other regions and/or countries.                                             *
-- **************************************************************************************


--
--  Create views.
--
whenever oserror exit rollback;
whenever sqlerror exit rollback;
REM connect baninst1/&&baninst1_password
REM
REM Create triggers
REM

start sv_slbbldg_del_trg
start sv_slbbldg_ins_trg
start sv_slbbldg_upd_trg

start sv_slbrdef_del_trg
start sv_slbrdef_ins_trg
start sv_slbrdef_upd_trg

start sv_ssrmeet_del_trg
start sv_ssrmeet_ins_trg
start sv_ssrmeet_upd_trg
