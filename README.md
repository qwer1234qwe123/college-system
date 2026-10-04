# 학사정보시스템 (College System)

Spring Boot 4.1 + JPA + MariaDB + Thymeleaf + Spring Security

- 배포 주소: https://codedbyjun.dev/college
- DB: MariaDB `College_System` (테이블 생성 스크립트: `sql/college_system_mariadb.sql`)

## 기능

| 역할 | 메뉴 | 내용 |
|---|---|---|
| 교수 | 학생 관리 | 학생 등록 · 조회 · 수정 · 삭제 |
| 교수 | 학과 관리 | 학과 목록 · 추가 |
| 교수 | 과목 관리 | 과목 개설 (강의시간 최대 3개, 1\~9교시), 본인 과목만 수정/삭제 |
| 교수 | 성적 입력 | 내 강의별 수강생 점수 입력 (0\~100, 등급 자동 표시) |
| 학생 | 수강신청 | 신청/취소, 정원 · 중복 · 시간 겹침 · 학기당 21학점 · 재학 여부 체크 |
| 학생 | 내 성적 | 학기별 성적, 4.5 만점 평점 |
| 공통 | 시간표 | 학생: 신청 과목 / 교수: 담당 과목 주간 시간표 |

- 학기 표기: `2026-1` (1\~6월), `2026-2` (7\~12월)
- 등급: 95↑ A+, 90 A0, 85 B+, 80 B0, 75 C+, 70 C0, 65 D+, 60 D0, 미만 F

## 구조

```
src/main/java/dev/codedbyjun/college
├── entity/       테이블 9개 매핑 (CS_*_Info 등)
├── repository/   Spring Data JPA
├── service/      학생 · 과목 · 수강신청 · 성적 · 시간표 로직
├── controller/   화면 컨트롤러
├── dto/          화면 입력값/조회 결과
├── support/      학기(Terms), 등급 환산(GradeScale)
└── security/     로그인 (학생/교수), 초기 교수 계정 생성
```
