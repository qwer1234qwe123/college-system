package dev.codedbyjun.college;

import dev.codedbyjun.college.entity.Department;
import dev.codedbyjun.college.entity.Professor;
import dev.codedbyjun.college.entity.Student;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.repository.ProfessorRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import dev.codedbyjun.college.support.Terms;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 과목 개설 → 수강신청 → 시간표 → 성적 입력 → 성적 조회 전체 흐름 (H2 메모리 DB) */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FeatureFlowTests {

	@Autowired MockMvc mvc;
	@Autowired DepartmentRepository departmentRepository;
	@Autowired ProfessorRepository professorRepository;
	@Autowired StudentRepository studentRepository;

	final String term = Terms.current();
	final RequestPostProcessor prof = user("P1").roles("PROF");
	final RequestPostProcessor prof2 = user("P2").roles("PROF");
	final RequestPostProcessor s1 = user("S1").roles("STUDENT");
	final RequestPostProcessor s2 = user("S2").roles("STUDENT");
	final RequestPostProcessor s3 = user("S3").roles("STUDENT");

	@BeforeAll
	void seed() {
		Department d = new Department();
		d.setCode("CS");
		d.setName("컴퓨터공학과");
		d.setRegDate(LocalDate.now());
		departmentRepository.save(d);
		professorRepository.save(prof("P1", "김교수"));
		professorRepository.save(prof("P2", "이교수"));
		studentRepository.save(student("S1", "홍길동", "재학"));
		studentRepository.save(student("S2", "김철수", "재학"));
		studentRepository.save(student("S3", "박휴학", "휴학"));
	}

	@Test @Order(1)
	void 과목_개설() throws Exception {
		mvc.perform(get("/subjects").with(prof)).andExpect(status().isOk());
		mvc.perform(get("/subjects/new").with(prof)).andExpect(status().isOk())
			.andExpect(content().string(containsString("강의시간")));

		createSubject("CS101", "자료구조", "1", "1", "1", "2", 3);   // 정원 1명, 월 1-2
		createSubject("CS102", "운영체제", "40", "1", "2", "3", 3);  // 월 2-3 (CS101과 겹침)
		createSubject("CS103", "알고리즘", "40", "2", "1", "3", 3);  // 화 1-3

		// 강의시간 없이 개설 → 폼 다시 표시 + 에러
		mvc.perform(post("/subjects/new").with(prof).with(csrf())
				.param("code", "CS199").param("name", "시간없음").param("deptCode", "CS")
				.param("credit", "3").param("majorType", "A").param("room", "301")
				.param("capacity", "10").param("term", term))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("강의시간을 1개 이상 입력하세요.")));

		mvc.perform(get("/subjects").param("mine", "true").with(prof)).andExpect(status().isOk())
			.andExpect(content().string(containsString("자료구조")))
			.andExpect(content().string(containsString("월 1-2교시")));

		// 다른 교수는 수정 불가
		mvc.perform(get("/subjects/CS101/edit").with(prof2))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("error", "본인이 담당하는 과목만 수정할 수 있습니다."));
		mvc.perform(get("/subjects/CS101/edit").with(prof)).andExpect(status().isOk());
	}

	@Test @Order(2)
	void 수강신청() throws Exception {
		mvc.perform(get("/enroll").with(s1)).andExpect(status().isOk())
			.andExpect(content().string(containsString("자료구조")));

		enroll(s1, "CS101").andExpect(flash().attribute("message", "'자료구조' 수강신청 완료"));
		enroll(s1, "CS101").andExpect(flash().attribute("error", "이미 신청한 과목입니다."));
		enroll(s1, "CS102").andExpect(flash().attribute("error", "'자료구조' 과목과 시간이 겹칩니다. (월 1-2교시)"));
		enroll(s1, "CS103").andExpect(flash().attribute("message", "'알고리즘' 수강신청 완료"));
		enroll(s2, "CS101").andExpect(flash().attribute("error", "'자료구조' 과목은 정원(1명)이 찼습니다."));
		enroll(s3, "CS103").andExpect(flash().attribute("error", "재학 중인 학생만 수강신청할 수 있습니다. (현재: 휴학)"));

		// 정원 찬 과목은 아직 신청 안 한 학생에게 '마감'으로 보임
		mvc.perform(get("/enroll").with(s2)).andExpect(status().isOk())
			.andExpect(content().string(containsString("마감")));

		// 교수는 수강신청 페이지 접근 불가
		mvc.perform(get("/enroll").with(prof)).andExpect(status().isForbidden());
	}

	@Test @Order(3)
	void 시간표() throws Exception {
		mvc.perform(get("/timetable").with(s1)).andExpect(status().isOk())
			.andExpect(content().string(containsString("자료구조")))
			.andExpect(content().string(containsString("알고리즘")))
			.andExpect(content().string(containsString("grid-column:2;grid-row:2 / 4")))
			.andExpect(content().string(not(containsString("운영체제"))));
		mvc.perform(get("/timetable").with(prof)).andExpect(status().isOk())
			.andExpect(content().string(containsString("운영체제")));
	}

	@Test @Order(4)
	void 성적_입력과_조회() throws Exception {
		mvc.perform(get("/lectures").with(prof)).andExpect(status().isOk())
			.andExpect(content().string(containsString("자료구조")));
		mvc.perform(get("/lectures/CS101/grades").with(prof)).andExpect(status().isOk())
			.andExpect(content().string(containsString("홍길동")));
		mvc.perform(get("/lectures/CS101/grades").with(prof2))
			.andExpect(status().is3xxRedirection());

		String seqParam = findScoreParam();
		mvc.perform(post("/lectures/CS101/grades").with(prof).with(csrf()).param(seqParam, "150"))
			.andExpect(flash().attribute("error", "홍길동 학생의 점수가 0~100 범위를 벗어났습니다."));
		mvc.perform(post("/lectures/CS101/grades").with(prof).with(csrf()).param(seqParam, "97"))
			.andExpect(flash().attribute("message", "성적이 저장되었습니다. (입력 1명)"));

		mvc.perform(get("/my/grades").with(s1)).andExpect(status().isOk())
			.andExpect(content().string(containsString("A+")))
			.andExpect(content().string(containsString("4.50")));

		// 성적 입력된 과목은 취소 불가, 성적 없는 과목은 취소 가능
		mvc.perform(post("/enroll/CS101/cancel").with(s1).with(csrf()).param("term", term))
			.andExpect(flash().attribute("error", "성적이 입력된 과목은 취소할 수 없습니다."));
		mvc.perform(post("/enroll/CS103/cancel").with(s1).with(csrf()).param("term", term))
			.andExpect(flash().attribute("message", "'알고리즘' 수강신청을 취소했습니다."));

		// 수강생 있는 과목 삭제 불가
		mvc.perform(post("/subjects/CS101/delete").with(prof).with(csrf()))
			.andExpect(flash().attribute("error", "수강 신청한 학생이 있는 과목은 삭제할 수 없습니다."));
		mvc.perform(post("/subjects/CS103/delete").with(prof).with(csrf()))
			.andExpect(flash().attribute("message", "과목이 삭제되었습니다."));
	}

	@Test @Order(5)
	void 기존_화면() throws Exception {
		for (String url : new String[]{"/", "/students", "/students/S1", "/students/new", "/departments"}) {
			mvc.perform(get(url).with(prof)).andExpect(status().isOk());
		}
		for (String url : new String[]{"/", "/me", "/my/grades", "/timetable"}) {
			mvc.perform(get(url).with(s1)).andExpect(status().isOk());
		}
		mvc.perform(get("/login")).andExpect(status().isOk());
	}

	// ---------- helpers ----------

	private void createSubject(String code, String name, String capacity, String day, String start, String end, int credit)
		throws Exception {
		mvc.perform(post("/subjects/new").with(prof).with(csrf())
				.param("code", code).param("name", name).param("deptCode", "CS")
				.param("credit", String.valueOf(credit)).param("majorType", "A").param("room", "공학관301")
				.param("capacity", capacity).param("term", term)
				.param("slots[0].day", day).param("slots[0].start", start).param("slots[0].end", end)
				.param("slots[1].day", "").param("slots[2].day", ""))
			.andExpect(status().is3xxRedirection())
			.andExpect(flash().attribute("message", "'" + name + "' 과목이 개설되었습니다."));
	}

	private org.springframework.test.web.servlet.ResultActions enroll(RequestPostProcessor who, String code) throws Exception {
		return mvc.perform(post("/enroll/" + code).with(who).with(csrf()).param("term", term))
			.andExpect(status().is3xxRedirection());
	}

	/** 성적 입력 화면에서 input name="scores[번호]" 추출 */
	private String findScoreParam() throws Exception {
		String html = mvc.perform(get("/lectures/CS101/grades").with(prof)).andReturn().getResponse().getContentAsString();
		var m = java.util.regex.Pattern.compile("name=\"(scores\\[\\d+])\"").matcher(html);
		if (!m.find()) {
			throw new AssertionError("score input not found");
		}
		return m.group(1);
	}

	private Professor prof(String code, String name) {
		Professor p = new Professor();
		p.setCode(code);
		p.setName(name);
		p.setEmail(code + "@test.dev");
		p.setPhone("010-0000-0000");
		p.setRegDate(LocalDate.now());
		p.setPassword("{noop}unused");
		return p;
	}

	private Student student(String code, String name, String status) {
		Student s = new Student();
		s.setCode(code);
		s.setName(name);
		s.setBirth(LocalDate.of(2005, 3, 1));
		s.setAddr("서울");
		s.setGrade("1");
		s.setGender("M");
		s.setPhone("010-1111-2222");
		s.setEmail(code + "@test.dev");
		s.setEntranceDate(LocalDate.of(2025, 3, 2));
		s.setStatus(status);
		s.setPassword("{noop}unused");
		return s;
	}
}
