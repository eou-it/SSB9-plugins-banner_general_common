-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************
--
-- Revert the table.  Remove for final delivery.
--

WHENEVER SQLERROR CONTINUE;

connect bansecr/&&bansecr_password
DELETE FROM  GURAOBJ WHERE GURAOBJ_OBJECT = 'GURINFO';
DELETE FROM  GURAOBJ WHERE GURAOBJ_OBJECT = 'GUAINFO';
DELETE FROM  GURUOBJ WHERE GURUOBJ_OBJECT = 'GURINFO' AND GURUOBJ_USERID = 'BAN_ADMIN_C';
DELETE FROM  GURUOBJ WHERE GURUOBJ_OBJECT = 'GUAINFO' AND GURUOBJ_USERID = 'BAN_ADMIN_C';

connect general/&&general_password
DROP TABLE GURINFO CASCADE CONSTRAINTS;
DROP PUBLIC SYNONYM GURINFO;
DROP SEQUENCE GURINFO_SURROGATE_ID_SEQUENCE;
DROP PUBLIC SYNONYM GURINFO_SURROGATE_ID_SEQUENCE;

connect dbeu_owner/&&dbeu_password
WHENEVER SQLERROR CONTINUE;
execute dbeu_util.teardown_table('GENERAL','GURINFO');
delete dbeu_commands where owner = 'GENERAL' AND TABLE_NAME = 'GURINFO';
/
delete dbeu_command_txt where owner = 'GENERAL' AND TABLE_NAME = 'GURINFO'  ;
/
commit;
