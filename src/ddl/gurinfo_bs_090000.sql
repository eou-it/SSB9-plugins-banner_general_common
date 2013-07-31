-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************


REM
REM GURINFO_banner_security_090000.sql
REM
REM AUDIT TRAIL: 9.0REM
REM Script for banner security grants for gurinfo
REM
REM AUDIT TRAIL END
REM

connect general/&&general_password
CREATE PUBLIC SYNONYM GURINFO FOR GURINFO;
GRANT INSERT, UPDATE, DELETE, SELECT ON GURINFO TO PUBLIC;

connect bansecr/&&bansecr_password
INSERT INTO GURAOBJ
(
  GURAOBJ_OBJECT,
  GURAOBJ_DEFAULT_ROLE,
  GURAOBJ_CURRENT_VERSION,
  GURAOBJ_SYSI_CODE,
  GURAOBJ_ACTIVITY_DATE,
  GURAOBJ_OWNER
)
SELECT 'GURINFO',
   'BAN_DEFAULT_M',
   '9.0',
   'G',
   Sysdate,
   'PUBLIC'
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM  GURAOBJ
                    WHERE GURAOBJ_OBJECT = 'GURINFO');

INSERT INTO GURAOBJ
(
  GURAOBJ_OBJECT,
  GURAOBJ_DEFAULT_ROLE,
  GURAOBJ_CURRENT_VERSION,
  GURAOBJ_SYSI_CODE,
  GURAOBJ_ACTIVITY_DATE,
  GURAOBJ_OWNER
)
SELECT 'GUAINFO',
   'BAN_DEFAULT_M',
   '9.0',
   'G',
   Sysdate,
   'PUBLIC'
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM  GURAOBJ
                    WHERE GURAOBJ_OBJECT = 'GUAINFO');

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
   'BAN_ADMIN_C' ,
   Sysdate,
   user
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM GURUOBJ
                    WHERE GURUOBJ_OBJECT = 'GURINFO'
                    AND GURUOBJ_USERID = 'BAN_ADMIN_C');

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
   'BAN_ADMIN_C' ,
   Sysdate,
   user
FROM DUAL
 WHERE NOT EXISTS ( SELECT 1
                    FROM GURUOBJ
                    WHERE GURUOBJ_OBJECT = 'GUAINFO'
                    AND GURUOBJ_USERID = 'BAN_ADMIN_C');

commit;
