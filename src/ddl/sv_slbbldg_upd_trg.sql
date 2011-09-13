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


CREATE OR REPLACE TRIGGER slbbldg_view_update_trg
  INSTEAD OF UPDATE ON sv_slbbldg
BEGIN
  gfksjpa.setId(:OLD.slbbldg_surrogate_id);
  gfksjpa.setVersion(:NEW.slbbldg_version);
  gb_bldgdefinition.p_update
    (p_bldg_code => :NEW.slbbldg_bldg_code,
     p_camp_code => :NEW.slbbldg_camp_code,
     p_capacity => :NEW.slbbldg_capacity,
     p_maximum_capacity => :NEW.slbbldg_maximum_capacity,
     p_rrcd_code => :NEW.slbbldg_rrcd_code,
     p_prcd_code => :NEW.slbbldg_prcd_code,
     p_site_code => :NEW.slbbldg_site_code,
     p_street_line1 => :NEW.slbbldg_street_line1,
     p_street_line2 => :NEW.slbbldg_street_line2,
     p_street_line3 => :NEW.slbbldg_street_line3,
     p_city => :NEW.slbbldg_city,
     p_stat_code => :NEW.slbbldg_stat_code,
     p_zip => :NEW.slbbldg_zip,
     p_cnty_code => :NEW.slbbldg_cnty_code,
     p_phone_area => :NEW.slbbldg_phone_area,
     p_phone_number => :NEW.slbbldg_phone_number,
     p_phone_extension => :NEW.slbbldg_phone_extension,
     p_sex => :NEW.slbbldg_sex,
     p_coll_code => :NEW.slbbldg_coll_code,
     p_dept_code => :NEW.slbbldg_dept_code,
     p_key_number => :NEW.slbbldg_key_number,
     p_pars_code => :NEW.slbbldg_pars_code,
     p_data_origin => :NEW.slbbldg_data_origin,
     p_user_id => :NEW.slbbldg_user_id,
     p_ctry_code_phone => :NEW.slbbldg_ctry_code_phone,
     p_house_number => :NEW.slbbldg_house_number,
     p_street_line4 => :NEW.slbbldg_street_line4,
     p_rowid => :NEW.slbbldg_v_rowid);
END;
/
