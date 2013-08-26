-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************


REM
REM guruobj_080602.sql
REM
-- AUDIT TRAIL: 8.6.2
-- 1. Script for banner security grants for GURINFO table and GUAINFO page.
-- AUDIT TRAIL END
REM

INSERT INTO GURUOBJ
(
  GURUOBJ_OBJECT,
  GURUOBJ_ROLE,
  GURUOBJ_USERID,
  GURUOBJ_ACTIVITY_DATE,
  GURUOBJ_USER_ID
)
SELECT 'GURINFO',
   'BAN_DEFAULT_M',
   'BAN_GENERAL_C' ,
   Sysdate,
   user
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM GURUOBJ
                    WHERE GURUOBJ_OBJECT = 'GURINFO'
                    AND GURUOBJ_USERID = 'BAN_GENERAL_C');

INSERT INTO GURUOBJ
(
  GURUOBJ_OBJECT,
  GURUOBJ_ROLE,
  GURUOBJ_USERID,
  GURUOBJ_ACTIVITY_DATE,
  GURUOBJ_USER_ID
)
SELECT 'GUAINFO',
   'BAN_DEFAULT_M',
   'BAN_GENERAL_C' ,
   Sysdate,
   user
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM GURUOBJ
                    WHERE GURUOBJ_OBJECT = 'GUAINFO'
                    AND GURUOBJ_USERID = 'BAN_GENERAL_C');

commit;
