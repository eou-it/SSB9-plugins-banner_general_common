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


CREATE OR REPLACE TRIGGER slbbldg_view_delete_trg
  INSTEAD OF DELETE ON sv_slbbldg
BEGIN
  gb_bldgdefinition.p_delete
    (p_bldg_code => :OLD.slbbldg_bldg_code,
     p_rowid => :OLD.slbbldg_v_rowid);
END;
/
