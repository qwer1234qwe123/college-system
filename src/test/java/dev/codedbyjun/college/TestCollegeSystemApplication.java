package dev.codedbyjun.college;

import dev.codedbyjun.college.entity.*;
import dev.codedbyjun.college.repository.*;
import dev.codedbyjun.college.support.Terms;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

/**
 * DB 없이 화면 확인용 데모 실행 (H2 메모리 DB + 샘플 데이터).
 *   .\gradlew bootTestRun
 *   http://localhost:8095  (교수 P1 / 학생 S1, 비밀번호 demo1234)
 */
public class TestCollegeSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(CollegeSystemApplication::main)
			.with(DemoData.class)
			.run("--spring.profiles.active=test", "--server.port=8095");
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class DemoData {

		@Bean
		ApplicationRunner seed(DepartmentRepository depts, ProfessorRepository profs, StudentRepository students,
							   StudentDeptRepository studentDepts, SubjectRepository subjects,
							   AttendClassRepository attends, GradeRepository grades, PasswordEncoder encoder) {
			return args -> {
				String pw = encoder.encode("demo1234");
				Department cs = dept(depts, "CS", "컴퓨터공학과");
				dept(depts, "EE", "전자공학과");
				Professor p1 = prof(profs, "P1", "김교수", pw);
				Professor p2 = prof(profs, "P2", "이교수", pw);

				String[][] studentData = {
					{"S1", "홍길동", "재학"}, {"S2", "김철수", "재학"}, {"S3", "이영희", "재학"},
					{"S4", "박민수", "휴학"}, {"S5", "최지우", "졸업"}};
				Student[] st = new Student[studentData.length];
				for (int i = 0; i < studentData.length; i++) {
					st[i] = student(students, studentData[i][0], studentData[i][1], studentData[i][2], pw);
					StudentDept sd = new StudentDept();
					sd.setStudent(st[i]);
					sd.setDepartment(cs);
					sd.setRegDate(LocalDate.now());
					studentDepts.save(sd);
				}

				String term = Terms.current();
				Subject ds = subject(subjects, "CS201", "자료구조", p1, cs, "A", 3, 40, term, new int[][]{{1, 1, 2}, {3, 1, 1}});
				Subject os = subject(subjects, "CS301", "운영체제", p1, cs, "A", 3, 2, term, new int[][]{{2, 3, 5}});
				Subject db = subject(subjects, "CS302", "데이터베이스", p2, cs, "B", 3, 30, term, new int[][]{{4, 6, 8}});
				subject(subjects, "GE101", "글쓰기와 소통", p2, cs, "C", 2, 50, term, new int[][]{{5, 2, 3}});
				subject(subjects, "GE205", "철학의 이해", p2, cs, "D", 2, 30, term, new int[][]{{1, 4, 5}});
				subject(subjects, "CS305", "웹프로그래밍", p1, cs, "B", 3, 30, term, new int[][]{{3, 6, 8}});

				AttendClass a1 = attend(attends, st[0], ds);
				attend(attends, st[0], os);
				attend(attends, st[0], db);
				AttendClass a4 = attend(attends, st[1], ds);
				attend(attends, st[1], os);
				attend(attends, st[2], ds);
				grade(grades, a1, 96);
				grade(grades, a4, 82);

				// 지난 학기 성적
				String prev = term.endsWith("2") ? term.substring(0, 4) + "-1" : (Integer.parseInt(term.substring(0, 4)) - 1) + "-2";
				Subject old1 = subject(subjects, "CS101", "프로그래밍기초", p1, cs, "A", 3, 40, prev, new int[][]{{1, 1, 3}});
				Subject old2 = subject(subjects, "GE001", "대학영어", p2, cs, "C", 2, 40, prev, new int[][]{{2, 1, 2}});
				grade(grades, attend(attends, st[0], old1), 91);
				grade(grades, attend(attends, st[0], old2), 78);
			};
		}

		private Department dept(DepartmentRepository repo, String code, String name) {
			Department d = new Department();
			d.setCode(code);
			d.setName(name);
			d.setRegDate(LocalDate.now());
			return repo.save(d);
		}

		private Professor prof(ProfessorRepository repo, String code, String name, String pw) {
			Professor p = new Professor();
			p.setCode(code);
			p.setName(name);
			p.setEmail(code.toLowerCase() + "@college.dev");
			p.setPhone("010-1234-5678");
			p.setRegDate(LocalDate.now());
			p.setPassword(pw);
			return repo.save(p);
		}

		private Student student(StudentRepository repo, String code, String name, String status, String pw) {
			Student s = new Student();
			s.setCode(code);
			s.setName(name);
			s.setBirth(LocalDate.of(2005, 3, 1));
			s.setAddr("서울특별시 강남구 테헤란로 1");
			s.setGrade("2");
			s.setGender("M");
			s.setPhone("010-1111-2222");
			s.setEmail(code.toLowerCase() + "@college.dev");
			s.setEntranceDate(LocalDate.of(2025, 3, 2));
			s.setStatus(status);
			s.setPassword(pw);
			return repo.save(s);
		}

		private Subject subject(SubjectRepository repo, String code, String name, Professor p, Department d, String type,
								int credit, int capacity, String term, int[][] times) {
			Subject s = new Subject();
			s.setCode(code);
			s.setName(name);
			s.setProfessor(p);
			s.setDepartment(d);
			s.setMajorType(type);
			s.setCredit(credit);
			s.setCapacity(capacity);
			s.setRoom("공학관 " + (300 + Math.abs(code.hashCode() % 20)));
			s.setTerm(term);
			s.setOpenDate(LocalDate.now());
			for (int[] t : times) {
				s.getTimes().add(new TimeTable(s, t[0], t[1], t[2]));
			}
			return repo.save(s);
		}

		private AttendClass attend(AttendClassRepository repo, Student s, Subject sub) {
			AttendClass a = new AttendClass();
			a.setStudent(s);
			a.setSubject(sub);
			a.setApplyDate(LocalDate.now());
			return repo.save(a);
		}

		private void grade(GradeRepository repo, AttendClass a, int score) {
			Grade g = new Grade();
			g.setAttendClass(a);
			g.setScore(score);
			g.setInputDate(LocalDate.now());
			repo.save(g);
		}
	}
}
