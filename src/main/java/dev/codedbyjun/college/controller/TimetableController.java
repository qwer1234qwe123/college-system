package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.service.SubjectService;
import dev.codedbyjun.college.service.TimetableService;
import dev.codedbyjun.college.support.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** 시간표: 학생은 신청 과목, 교수는 담당 과목 */
@Controller
@RequiredArgsConstructor
public class TimetableController {

	private static final List<String> PERIOD_TIMES =
		List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");

	private final TimetableService timetableService;
	private final SubjectService subjectService;

	@GetMapping("/timetable")
	public String timetable(@RequestParam(required = false) String term, Authentication auth, Model model) {
		String t = Terms.orCurrent(term);
		boolean prof = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PROF"));
		model.addAttribute("term", t);
		model.addAttribute("termLabel", Terms.label(t));
		model.addAttribute("terms", Terms.options(subjectService.terms()));
		model.addAttribute("prof", prof);
		model.addAttribute("days", List.of("월", "화", "수", "목", "금"));
		model.addAttribute("periodTimes", PERIOD_TIMES);
		model.addAttribute("tt", prof
			? timetableService.forProfessor(auth.getName(), t)
			: timetableService.forStudent(auth.getName(), t));
		return "timetable";
	}
}
