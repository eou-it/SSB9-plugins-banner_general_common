-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************


REM
REM stuview_bgc.sql
REM
REM AUDIT TRAIL: 9.0
REM 1. Horizon
REM Main common project schema maintenance script.
REM AUDIT TRAIL END
REM
start svq_ssvmeet
start sv_ssrmeet
start sv_slbrdef
start sv_slbbldg
REM
start sv_sorpcol
start sv_sorconc
start sv_sordegr
start sv_sormajr
start sv_sorminr

start svq_ssrmeet_timclt.sql
