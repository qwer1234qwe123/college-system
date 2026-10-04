-- =========================================================
-- College_System (학사정보 시스템) - MariaDB / MySQL 버전
-- 원본: MS SQL Server 스크립트 + 학사정보_테이블 설계도.xlsx
-- 변경점:
--   * FK 제약조건 실제로 추가
--   * 일련번호 컬럼 AUTO_INCREMENT 적용
--   * 과목코드 타입 varchar(20)으로 통일
--   * 비밀번호 varchar(100) (BCrypt 해시 60자 저장 가능)
--   * 교수 이메일 varchar(40)
--   * 수강신청 중복 방지(UNIQUE), 성적 1건 제한(UNIQUE)
-- =========================================================

CREATE DATABASE IF NOT EXISTS College_System
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE College_System;

-- 학과 정보
CREATE TABLE CS_Department_Info (
  CS_DI_Code   VARCHAR(20)  NOT NULL COMMENT '학과 코드',
  CS_DI_Name   VARCHAR(20)  NOT NULL COMMENT '학과 이름',
  CS_DI_Rdate  DATE         NOT NULL COMMENT '학과 생성날짜',
  PRIMARY KEY (CS_DI_Code)
) ENGINE=InnoDB COMMENT='학과 정보';

-- 학생 기본 정보
CREATE TABLE CS_Student_Info (
  CS_SI_Code       VARCHAR(20)  NOT NULL COMMENT '학번',
  CS_SI_Name       VARCHAR(20)  NOT NULL COMMENT '이름',
  CS_SI_Birth      DATE         NOT NULL COMMENT '생년월일',
  CS_SI_Addr       VARCHAR(100) NOT NULL COMMENT '주소',
  CS_SI_Class      CHAR(1)      NOT NULL COMMENT '학년',
  CS_SI_Gender     CHAR(1)      NOT NULL COMMENT '성별',
  CS_SI_Phone      VARCHAR(20)  NOT NULL COMMENT '전화번호',
  CS_SI_Email      VARCHAR(40)  NOT NULL COMMENT '이메일',
  CS_SI_Rdate      DATE         NOT NULL COMMENT '입학년도',
  CS_SI_Condition  VARCHAR(10)  NOT NULL COMMENT '학적상태',
  CS_SI_Password   VARCHAR(100) NOT NULL COMMENT '비밀번호(BCrypt)',
  PRIMARY KEY (CS_SI_Code)
) ENGINE=InnoDB COMMENT='학생 기본 정보';

-- 학생 소속 학과
CREATE TABLE CS_Student_Dept (
  CS_SD_Seq    INT          NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  CS_SI_Code   VARCHAR(20)  NOT NULL COMMENT '학생 코드',
  CS_DI_Code   VARCHAR(20)  NOT NULL COMMENT '학과 코드',
  CS_SD_Rdate  DATE         NOT NULL COMMENT '입력일자',
  PRIMARY KEY (CS_SD_Seq),
  CONSTRAINT FK_SD_Student FOREIGN KEY (CS_SI_Code) REFERENCES CS_Student_Info (CS_SI_Code),
  CONSTRAINT FK_SD_Dept    FOREIGN KEY (CS_DI_Code) REFERENCES CS_Department_Info (CS_DI_Code)
) ENGINE=InnoDB COMMENT='학생 소속 학과';

-- 교수 기본 정보
CREATE TABLE CS_Prof_Info (
  CS_PI_Code      VARCHAR(20)  NOT NULL COMMENT '교수 코드',
  CS_PI_Name      VARCHAR(20)  NOT NULL COMMENT '교수명',
  CS_PI_Email     VARCHAR(40)  NOT NULL COMMENT '이메일',
  CS_PI_Phone     VARCHAR(20)  NOT NULL COMMENT '전화번호',
  CS_PI_Rdate     DATE         NOT NULL COMMENT '임용일자',
  CS_PI_Password  VARCHAR(100) NOT NULL COMMENT '비밀번호(BCrypt)',
  PRIMARY KEY (CS_PI_Code)
) ENGINE=InnoDB COMMENT='교수 기본 정보';

-- 교수 소속 학과
CREATE TABLE CS_Prof_Dept (
  CS_PD_Number  INT          NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  CS_DI_Code    VARCHAR(20)  NOT NULL COMMENT '학과 코드',
  CS_PI_Code    VARCHAR(20)  NOT NULL COMMENT '교수 코드',
  CS_PD_Rdate   DATE         NOT NULL COMMENT '입력일자',
  CS_PD_Task    CHAR(1)      NOT NULL DEFAULT 'N' COMMENT '학과장 여부(Y/N)',
  PRIMARY KEY (CS_PD_Number),
  CONSTRAINT FK_PD_Dept FOREIGN KEY (CS_DI_Code) REFERENCES CS_Department_Info (CS_DI_Code),
  CONSTRAINT FK_PD_Prof FOREIGN KEY (CS_PI_Code) REFERENCES CS_Prof_Info (CS_PI_Code),
  CONSTRAINT CK_PD_Task CHECK (CS_PD_Task IN ('Y', 'N'))
) ENGINE=InnoDB COMMENT='교수 소속 학과';

-- 과목 정보
CREATE TABLE CS_Subject_Info (
  CS_SBI_Code    VARCHAR(20)  NOT NULL COMMENT '과목 코드',
  CS_SBI_Name    VARCHAR(20)  NOT NULL COMMENT '과목명',
  CS_PI_Code     VARCHAR(20)  NOT NULL COMMENT '담당 교수',
  CS_DI_Code     VARCHAR(20)  NOT NULL COMMENT '개설 학과',
  CS_SBI_Date    DATE         NOT NULL COMMENT '개설 일자',
  CS_SBI_Credit  INT          NOT NULL COMMENT '이수 학점',
  CS_SBI_Major   CHAR(1)      NOT NULL COMMENT 'A=전공필수, B=전공선택, C=교양필수, D=교양선택',
  CS_SBI_Info    VARCHAR(10)  NOT NULL COMMENT '강의실 정보',
  CS_SBI_Num     INT          NOT NULL COMMENT '수강 최대 인원',
  CS_SBI_Term    VARCHAR(10)  NOT NULL COMMENT '개설 학기',
  PRIMARY KEY (CS_SBI_Code),
  CONSTRAINT FK_SBI_Prof FOREIGN KEY (CS_PI_Code) REFERENCES CS_Prof_Info (CS_PI_Code),
  CONSTRAINT FK_SBI_Dept FOREIGN KEY (CS_DI_Code) REFERENCES CS_Department_Info (CS_DI_Code),
  CONSTRAINT CK_SBI_Major CHECK (CS_SBI_Major IN ('A', 'B', 'C', 'D'))
) ENGINE=InnoDB COMMENT='과목 정보';

-- 수강 신청
CREATE TABLE CS_AttendClass_Info (
  CS_AI_Seq    INT          NOT NULL AUTO_INCREMENT COMMENT '일련 번호',
  CS_SBI_Code  VARCHAR(20)  NOT NULL COMMENT '과목 코드',
  CS_SI_Code   VARCHAR(20)  NOT NULL COMMENT '학생 코드',
  CS_CI_Date   DATE         NOT NULL COMMENT '신청 일자',
  PRIMARY KEY (CS_AI_Seq),
  UNIQUE KEY UQ_AI_Subject_Student (CS_SBI_Code, CS_SI_Code),
  CONSTRAINT FK_AI_Subject FOREIGN KEY (CS_SBI_Code) REFERENCES CS_Subject_Info (CS_SBI_Code),
  CONSTRAINT FK_AI_Student FOREIGN KEY (CS_SI_Code)  REFERENCES CS_Student_Info (CS_SI_Code)
) ENGINE=InnoDB COMMENT='수강 신청';

-- 성적 정보
CREATE TABLE CS_Grade_Info (
  CS_GI_Seq    INT   NOT NULL AUTO_INCREMENT COMMENT '일련 번호',
  CS_AI_Seq    INT   NOT NULL COMMENT '수강 신청 일련 번호',
  CS_GI_Grade  INT   NOT NULL COMMENT '성적',
  CS_GI_Date   DATE  NOT NULL COMMENT '성적 입력 일자',
  PRIMARY KEY (CS_GI_Seq),
  UNIQUE KEY UQ_GI_Attend (CS_AI_Seq),
  CONSTRAINT FK_GI_Attend FOREIGN KEY (CS_AI_Seq) REFERENCES CS_AttendClass_Info (CS_AI_Seq)
) ENGINE=InnoDB COMMENT='성적 정보';

-- 과목 강의시간 (학생 시간표는 수강신청 + 강의시간 조인)
-- (2026-10-04 변경: 학생코드/전체신청학점 컬럼 제거 → sql/2026-10-04_time_info_to_lecture_time.sql)
CREATE TABLE CS_Time_Info (
  CS_TI_Num     INT          NOT NULL AUTO_INCREMENT COMMENT '일련 번호',
  CS_SBI_Code   VARCHAR(20)  NOT NULL COMMENT '과목 코드',
  CS_TI_Day     CHAR(1)      NOT NULL COMMENT '강의 요일 (1=월 ~ 5=금)',
  CS_TI_Time    VARCHAR(10)  NOT NULL COMMENT '교시 범위 (예: 1-3)',
  PRIMARY KEY (CS_TI_Num),
  CONSTRAINT FK_TI_Subject FOREIGN KEY (CS_SBI_Code) REFERENCES CS_Subject_Info (CS_SBI_Code)
) ENGINE=InnoDB COMMENT='과목 강의시간';
