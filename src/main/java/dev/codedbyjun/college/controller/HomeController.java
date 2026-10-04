package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.repository.ProfessorRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import dev.codedbyjun.college.repository.SubjectRepository;
import dev.codedbyjun.college.service.GradeService;
import dev.codedbyjun.college.service.StudentService;
import dev.codedbyjun.college.support.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private static final DateTimeFormatter TODAY_FORMAT =
		DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN);

	private final StudentService studentService;
	private final GradeService gradeService;
	private final StudentRepository studentRepository;
	private final DepartmentRepository departmentRepository;
	private final ProfessorRepository professorRepository;
	private final SubjectRepository subjectRepository;
	private final AttendClassRepository attendClassRepository;

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/")
	public String home(Authentication auth, Model model) {
		String term = Terms.current();
		model.addAttribute("today", LocalDate.now().format(TODAY_FORMAT));
		model.addAttribute("termLabel", Terms.label(term));

		boolean prof = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
		if (prof) {
			model.addAttribute("studentCount", studentRepository.count());
			model.addAttribute("deptCount", departmentRepository.count());
			model.addAttribute("profCount", professorRepository.count());
			model.addAttribute("subjectCount", subjectRepository.findByTermOrderByCodeAsc(term).size());
		} else {
			String code = auth.getName();
			model.addAttribute("mySubjectCount", attendClassRepository.findByStudentCodeAndSubjectTerm(code, term).size());
			model.addAttribute("myCredits", attendClassRepository.sumCredits(code, term));
			GradeService.Report report = gradeService.report(code);
			model.addAttribute("myGpa", report.gpa());
			model.addAttribute("myEarned", report.earnedCredits());
		}
		return "index";
	}

	/** 학생 본인 정보 */
	@GetMapping("/me")
	public String me(Authentication auth, Model model) {
		model.addAttribute("detail", studentService.get(auth.getName()));
		return "students/detail";
	}
}
