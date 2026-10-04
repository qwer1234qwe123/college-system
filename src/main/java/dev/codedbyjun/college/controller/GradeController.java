package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.service.GradeService;
import dev.codedbyjun.college.service.SubjectService;
import dev.codedbyjun.college.support.Terms;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class GradeController {

	private final GradeService gradeService;
	private final SubjectService subjectService;

	/** 성적 입력 폼: name="scores[수강신청번호]" */
	@Getter
	@Setter
	public static class GradeForm {
		private Map<Integer, Integer> scores = new HashMap<>();
	}

	// ---------- 교수: 내 강의 / 성적 입력 ----------

	@GetMapping("/lectures")
	public String lectures(@RequestParam(required = false) String term, Authentication auth, Model model) {
		String t = Terms.orCurrent(term);
		model.addAttribute("term", t);
		model.addAttribute("termLabel", Terms.label(t));
		model.addAttribute("terms", Terms.options(subjectService.terms()));
		model.addAttribute("lectures", gradeService.lectures(auth.getName(), t));
		return "lectures/list";
	}

	@GetMapping("/lectures/{code}/grades")
	public String roster(@PathVariable String code, Authentication auth, Model model, RedirectAttributes ra) {
		try {
			model.addAttribute("roster", gradeService.roster(code, auth.getName()));
			return "lectures/grades";
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/lectures";
		}
	}

	@PostMapping("/lectures/{code}/grades")
	public String save(@PathVariable String code, @ModelAttribute GradeForm form, BindingResult result,
					   Authentication auth, RedirectAttributes ra) {
		if (result.hasErrors()) {
			ra.addFlashAttribute("error", "점수는 0~100 사이의 숫자로 입력하세요.");
			return "redirect:/lectures/" + code + "/grades";
		}
		try {
			int saved = gradeService.save(code, auth.getName(), form.getScores());
			ra.addFlashAttribute("message", "성적이 저장되었습니다. (입력 " + saved + "명)");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/lectures/" + code + "/grades";
	}

	// ---------- 학생: 내 성적 ----------

	@GetMapping("/my/grades")
	public String myGrades(Authentication auth, Model model) {
		model.addAttribute("report", gradeService.report(auth.getName()));
		return "my/grades";
	}
}
