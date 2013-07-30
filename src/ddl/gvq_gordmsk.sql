-- *****************************************************************************************
-- * Copyright 2010-2013 Ellucian Company L.P. and its affiliates.                         *
-- *****************************************************************************************
-- View of masking rules for user
create or replace view gvq_gordmsk
(
    Gordmsk_objs_code,
          Gordmsk_Block_Name,
          Gordmsk_Column_Name,
          gordmsk_seqno,
          Gordmsk_Query_Column,
          Gordmsk_Display_Ind,
          Gordmsk_Conceal_Ind,
          Gordmsk_Data_Mask,
          Gordmsk_Mask_Direction,
          Gordmsk_Mask_Length,
          Gordmsk_Objs_Comp_Name,
          Gordmsk_Block_Comp_Name,
          gordmsk_column_comp_name,
          Gordmsk_Fgac_User_Id,
          gordmsk_all_user_ind,
          Gordmsk_Fbpr_Code,
          Gordmsk_Surrogate_Id,
          Gordmsk_Version  ,
          gordmsk_activity_date,
          gordmsk_user_id,
          gordmsk_data_origin
) as
Select   Gordmcl_objs_code,
          Gordmcl_Block_Name,
          Gordmcl_Column_Name,
          gordmsk_seqno,
          Gordmcl_Query_Column,
          Gordmsk_Display_Ind,
          Gordmsk_Conceal_Ind,
          Gordmsk_Data_Mask,
          Gordmsk_Mask_Direction,
          Gordmsk_Mask_Length,
          Gordmsk_Objs_Comp_Name,
          Gordmsk_Block_Comp_Name,
          gordmsk_column_comp_name,
          Gordmsk_Fgac_User_Id,
          gordmsk_all_user_ind,
          Gordmsk_Fbpr_Code,
          Gordmsk_Surrogate_Id,
          Gordmsk_Version ,
          gordmsk_activity_date,
          gordmsk_user_id,
          gordmsk_data_origin
  FROM gordmsk, gordmcl
  WHERE gordmcl_objs_code    = gordmsk_objs_code
    And Gordmcl_Block_Name   = Gordmsk_Block_Name
    And Gordmcl_Column_Name  = Gordmsk_Column_Name
    AND (gordmsk_fgac_user_id IS NULL OR gordmsk_fgac_user_id = USER)
    And (Gordmsk_Fbpr_Code    Is Null Or Gordmsk_Fbpr_Code In
          (Select Gorfbpr_Fbpr_Code From Gorfbpr
          Where Gorfbpr_Fgac_User_Id = User))
     WITH READ ONLY
   ;
COMMENT ON TABLE gvq_gordmsk IS
  'Object masking View';
CREATE OR REPLACE PUBLIC SYNONYM gvq_gordmsk FOR gvq_gordmsk;
