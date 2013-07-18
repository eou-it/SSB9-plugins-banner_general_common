-- *****************************************************************************************
-- * Copyright 2012 Ellucian Company L.P. and its affiliates.                              *
-- *****************************************************************************************
--
-- SVQ_SSRMEET_TIMECONFLICT.sql
--
-- AUDIT TRAIL: 9.0
--    Generated view for Horizon API support
-- AUDIT TRAIL END
--

CREATE OR REPLACE Force VIEW SVQ_SSRMEET_TIMECONFLICT (
SSRMEET_SURROGATE_ID,
SSRMEET_VERSION,
SSRMEET_CRN,
SSRMEET_TERM_CODE ,
ssrmeet_crn_conflict)
AS
 Select a.SSRMEET_SURROGATE_ID,
       a.SSRMEET_VERSION,
       a.ssrmeet_crn,
       a.ssrmeet_term_code ,
       b.ssrmeet_crn
      From  Ssrmeet B,  Ssrmeet A
      Where    b.ssrmeet_term_code = a.ssrmeet_term_code
        And  (((A.Ssrmeet_Start_Date Between
           b.SSRMEET_START_DATE AND b.SSRMEET_END_DATE)
         Or  (A.Ssrmeet_End_Date Between
            B.Ssrmeet_Start_Date And B.Ssrmeet_End_Date ))
         OR  ((b.SSRMEET_START_DATE BETWEEN
            A.Ssrmeet_Start_Date And A.Ssrmeet_End_Date)
         OR  (A.SSRMEET_END_DATE BETWEEN
            A.Ssrmeet_Start_Date And A.Ssrmeet_End_Date)))
        And  (Decode(A.Ssrmeet_Mon_Day, 'M', 'Y', 'X') = Decode(B.Ssrmeet_Mon_Day, 'M', 'Y', 'N')
         Or  Decode(A.Ssrmeet_Tue_Day, 'T', 'Y', 'X') = Decode(B.Ssrmeet_Tue_Day, 'T', 'Y', 'N')
         Or  Decode(A.Ssrmeet_Wed_Day, 'W', 'Y', 'X') = Decode(B.Ssrmeet_Wed_Day, 'W', 'Y', 'N')
         Or  Decode(A.Ssrmeet_Thu_Day, 'R', 'Y', 'X') = Decode(B.Ssrmeet_Thu_Day, 'R', 'Y', 'N')
         Or  Decode(A.Ssrmeet_Fri_Day, 'F', 'Y', 'X') = Decode(B.Ssrmeet_Fri_Day, 'F', 'Y', 'N')
         Or  Decode(A.Ssrmeet_Sat_Day , 'S', 'Y', 'X') = Decode(B.Ssrmeet_Sat_Day , 'S', 'Y', 'N')
         OR  decode(A.SSRMEET_SUN_DAY, 'U', 'Y', 'X') = decode(B.SSRMEET_SUN_DAY, 'U', 'Y', 'N'))
        And  (((Nvl(A.Ssrmeet_Begin_Time, To_Char(0)) Between
           Nvl(B.Ssrmeet_Begin_Time, To_Char(0)) And Nvl(
           b.SSRMEET_END_TIME, TO_CHAR(0)))
         Or  (Nvl(A.Ssrmeet_End_Time, To_Char(0)) Between Nvl(
            B.Ssrmeet_Begin_Time, To_Char(0)) And Nvl(
            B.Ssrmeet_End_Time, To_Char(0))))
         OR  ((NVL(b.SSRMEET_BEGIN_TIME, TO_CHAR(0)) BETWEEN
            NVL(A.SSRMEET_BEGIN_TIME, TO_CHAR(0)) AND NVL(
            A.Ssrmeet_End_Time, To_Char(0)))
         OR  (NVL(b.SSRMEET_END_TIME, TO_CHAR(0)) BETWEEN NVL(
            A.SSRMEET_BEGIN_TIME, TO_CHAR(0)) AND NVL(
            A.Ssrmeet_End_Time, To_Char(0)))))
        and A.ssrmeet_term_code = b.ssrmeet_term_code
        And A.Ssrmeet_Crn <> b.Ssrmeet_Crn
  WITH READ ONLY;

CREATE OR REPLACE PUBLIC SYNONYM SVQ_SSRMEET_TIMECONFLICT FOR SVQ_SSRMEET_TIMECONFLICT ;