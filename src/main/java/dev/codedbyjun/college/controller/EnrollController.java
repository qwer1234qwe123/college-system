package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.service.EnrollService;
import dev.codedbyjun.college.service.SubjectService;
import dev.codedbyjun.college.support.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 수강신청 (학생 전용) */
@Controller
@RequestMapping("/enroll")
@RequiredArgsConstructor
public class EnrollController {

	private final EnrollService enrollService;
	private final SubjectService subjectService;

	@GetMapping
	public String list(@RequestParam(required = false) String term, Authentication auth, Model model) {
		String t = Terms.orCurrent(term);
		var rows = enrollService.rows(auth.getName(), t);
		model.addAttribute("term", t);
		model.addAttribute("termLabel", Terms.label(t));
		model.addAttribute("terms", Terms.options(subjectService.terms()));
		model.addAttribute("rows", rows);
		model.addAttribute("myRows", rows.stream().filter(EnrollService.EnrollRow::applied).toList());
		model.addAttribute("myCredits", enrollService.myCredits(auth.getName(), t));
		model.addAttribute("maxCredits", EnrollService.MAX_CREDITS);
		return "enroll/list";
	}

	@PostMapping("/{code}")
	public String apply(@PathVariable String code, @RequestParam String term, Authentication auth, RedirectAttributes ra) {
		try {
			String name = enrollService.apply(auth.getName(), code);
			ra.addFlashAttribute("message", "'" + name + "' 수강신청 완료");
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		} catch (DataIntegrityViolationException e) {
			// UNIQUE(과목, 학생) 제약에 걸린 경우 (동시에 두 번 누른 경우 등)
			ra.addFlashAttribute("error", "이미 신청한 과목입니다.");
		}
		return "redirect:/enroll?term=" + Terms.orCurrent(term);
	}

	@PostMapping("/{code}/cancel")
	public String cancel(@PathVariable String code, @RequestParam String term, Authentication auth, RedirectAttributes ra) {
		try {
			String name = enrollService.cancel(auth.getName(), code);
			ra.addFlashAttribute("message", "'" + name + "' 수강신청을 취소했습니다.");
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/enroll?term=" + Terms.orCurrent(term);
	}
}
