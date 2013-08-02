-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************


REM
REM guraobj_080602.sql
REM
-- AUDIT TRAIL: 8.6.2
-- 1. Script for banner security grants for GURINFO table and GUAINFO page.
-- AUDIT TRAIL END
REM

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

commit;
