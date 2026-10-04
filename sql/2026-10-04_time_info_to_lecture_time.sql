-- CS_Time_Info 를 "학생별 시간표"에서 "과목 강의시간"으로 변경
-- 학생 시간표는 수강신청(CS_AttendClass_Info) + 강의시간(CS_Time_Info) 조인으로 만든다.
--   CS_TI_Day  : 1=월 2=화 3=수 4=목 5=금
--   CS_TI_Time : 교시 범위 "시작-끝" (예: 1-3 = 1~3교시)
-- 실행: mysql -u college -p College_System < 2026-10-04_time_info_to_lecture_time.sql

USE College_System;

ALTER TABLE CS_Time_Info
  DROP FOREIGN KEY FK_TI_Student;

ALTER TABLE CS_Time_Info
  DROP COLUMN CS_SI_Code,
  DROP COLUMN CS_TI_Credit,
  MODIFY CS_TI_Day  CHAR(1)     NOT NULL COMMENT '강의 요일 (1=월 ~ 5=금)',
  MODIFY CS_TI_Time VARCHAR(10) NOT NULL COMMENT '교시 범위 (예: 1-3)',
  COMMENT = '과목 강의시간';
