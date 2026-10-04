package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.dto.SubjectForm;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.service.SubjectService;
import dev.codedbyjun.college.support.Terms;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 과목 개설/관리 (교수 전용) */
@Controller
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

	private final SubjectService subjectService;
	private final DepartmentRepository departmentRepository;

	@GetMapping
	public String list(@RequestParam(required = false) String term,
					   @RequestParam(defaultValue = "false") boolean mine,
					   Authentication auth, Model model) {
		String t = Terms.orCurrent(term);
		model.addAttribute("term", t);
		model.addAttribute("termLabel", Terms.label(t));
		model.addAttribute("terms", Terms.options(subjectService.terms()));
		model.addAttribute("mine", mine);
		model.addAttribute("subjects", subjectService.list(t, mine ? auth.getName() : null));
		return "subjects/list";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		SubjectForm form = new SubjectForm();
		form.setTerm(Terms.current());
		form.setCredit(3);
		form.setCapacity(40);
		form.setMajorType("B");
		return showForm(model, form, false);
	}

	@PostMapping("/new")
	public String create(@Valid @ModelAttribute("form") SubjectForm form, BindingResult result,
						 Authentication auth, Model model, RedirectAttributes ra) {
		if (result.hasErrors()) {
			return showForm(model, form, false);
		}
		try {
			subjectService.create(form, auth.getName());
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return showForm(model, form, false);
		}
		ra.addFlashAttribute("message", "'" + form.getName() + "' 과목이 개설되었습니다.");
		return "redirect:/subjects?mine=true&term=" + form.getTerm();
	}

	@GetMapping("/{code}/edit")
	public String editForm(@PathVariable String code, Authentication auth, Model model, RedirectAttributes ra) {
		try {
			return showForm(model, subjectService.getForm(code, auth.getName()), true);
		} catch (IllegalStateException | IllegalArgumentException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/subjects";
		}
	}

	@PostMapping("/{code}/edit")
	public String edit(@PathVariable String code, @Valid @ModelAttribute("form") SubjectForm form,
					   BindingResult result, Authentication auth, Model model, RedirectAttributes ra) {
		form.setCode(code);
		if (result.hasErrors()) {
			return showForm(model, form, true);
		}
		try {
			subjectService.update(code, form, auth.getName());
		} catch (IllegalArgumentException | IllegalStateException e) {
			model.addAttribute("error", e.getMessage());
			return showForm(model, form, true);
		}
		ra.addFlashAttribute("message", "수정되었습니다.");
		return "redirect:/subjects?mine=true&term=" + form.getTerm();
	}

	@PostMapping("/{code}/delete")
	public String delete(@PathVariable String code, Authentication auth, RedirectAttributes ra) {
		try {
			subjectService.delete(code, auth.getName());
			ra.addFlashAttribute("message", "과목이 삭제되었습니다.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
		}
		return "redirect:/subjects?mine=true";
	}

	private String showForm(Model model, SubjectForm form, boolean edit) {
		model.addAttribute("form", form);
		model.addAttribute("edit", edit);
		model.addAttribute("departments", departmentRepository.findAll());
		return "subjects/form";
	}
}
