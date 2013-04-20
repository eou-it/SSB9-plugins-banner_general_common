-- *****************************************************************************************
-- * Copyright 2011-2012 Ellucian Company L.P. and its affiliates.                                                         *
-- *****************************************************************************************






col owner format a10;
col object_name format a30;
col object_type format a15
col owner format a10;
      select owner, object_name,
             object_type,
             last_ddl_time
      from   sys.dba_objects
      where  status = 'INVALID'
order by owner, object_name
/
