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




CREATE OR REPLACE TRIGGER slbrdef_view_create_trg
  INSTEAD OF INSERT ON sv_slbrdef
DECLARE
  p_rowid_v VARCHAR2(100);
BEGIN
  gfksjpa.setId(:NEW.slbrdef_surrogate_id);
  gfksjpa.setVersion(:NEW.slbrdef_version);
  gb_roomdefinition.p_create
    (p_bldg_code => :NEW.slbrdef_bldg_code,
     p_room_number => :NEW.slbrdef_room_number,
     p_desc => :NEW.slbrdef_desc,
     p_capacity => :NEW.slbrdef_capacity,
     p_maximum_capacity => :NEW.slbrdef_maximum_capacity,
     p_rmst_code => :NEW.slbrdef_rmst_code,
     p_rrcd_code => :NEW.slbrdef_rrcd_code,
     p_prcd_code => :NEW.slbrdef_prcd_code,
     p_utility_rate => :NEW.slbrdef_utility_rate,
     p_utility_rate_period => :NEW.slbrdef_utility_rate_period,
     p_phone_area => :NEW.slbrdef_phone_area,
     p_phone_number => :NEW.slbrdef_phone_number,
     p_phone_extension => :NEW.slbrdef_phone_extension,
     p_bcat_code => :NEW.slbrdef_bcat_code,
     p_sex => :NEW.slbrdef_sex,
     p_room_type => :NEW.slbrdef_room_type,
     p_priority => :NEW.slbrdef_priority,
     p_coll_code => :NEW.slbrdef_coll_code,
     p_dept_code => :NEW.slbrdef_dept_code,
     p_key_number => :NEW.slbrdef_key_number,
     p_width => :NEW.slbrdef_width,
     p_length => :NEW.slbrdef_length,
     p_area => :NEW.slbrdef_area,
     p_term_code_eff => :NEW.slbrdef_term_code_eff,
     p_pars_code => :NEW.slbrdef_pars_code,
     p_ctry_code_phone => :NEW.slbrdef_ctry_code_phone,
     p_rowid_out => p_rowid_v);
END;
/
