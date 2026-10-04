package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.dto.StudentForm;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 학생 관리 (교수 전용) */
@Controller
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

	private final StudentService studentService;
	private final DepartmentRepository departmentRepository;

	@GetMapping
	public String list(@RequestParam(required = false) String keyword, Model model) {
		model.addAttribute("students", studentService.search(keyword));
		model.addAttribute("keyword", keyword);
		return "students/list";
	}

	@GetMapping("/{code}")
	public String detail(@PathVariable String code, Model model) {
		model.addAttribute("detail", studentService.get(code));
		return "students/detail";
	}

	@GetMapping("/new")
	public String createForm(Model model) {
		StudentForm form = new StudentForm();
		form.setStatus("재학");
		form.setGrade("1");
		return showForm(model, form, false);
	}

	@PostMapping("/new")
	public String create(@Valid @ModelAttribute("form") StudentForm form, BindingResult result,
						 Model model, RedirectAttributes ra) {
		if (result.hasErrors()) {
			return showForm(model, form, false);
		}
		try {
			studentService.create(form);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return showForm(model, form, false);
		}
		ra.addFlashAttribute("message", "학생이 등록되었습니다.");
		return "redirect:/students/" + form.getCode();
	}

	@GetMapping("/{code}/edit")
	public String editForm(@PathVariable String code, Model model) {
		var detail = studentService.get(code);
		return showForm(model, StudentForm.from(detail.student(), detail.deptCode()), true);
	}

	@PostMapping("/{code}/edit")
	public String edit(@PathVariable String code, @Valid @ModelAttribute("form") StudentForm form,
					   BindingResult result, Model model, RedirectAttributes ra) {
		form.setCode(code);
		if (result.hasErrors()) {
			return showForm(model, form, true);
		}
		try {
			studentService.update(code, form);
		} catch (IllegalArgumentException e) {
			model.addAttribute("error", e.getMessage());
			return showForm(model, form, true);
		}
		ra.addFlashAttribute("message", "수정되었습니다.");
		return "redirect:/students/" + code;
	}

	@PostMapping("/{code}/delete")
	public String delete(@PathVariable String code, RedirectAttributes ra) {
		try {
			studentService.delete(code);
		} catch (IllegalStateException e) {
			ra.addFlashAttribute("error", e.getMessage());
			return "redirect:/students/" + code;
		} catch (DataAccessException e) {
			ra.addFlashAttribute("error", "다른 데이터(시간표 등)에서 사용 중이라 삭제할 수 없습니다.");
			return "redirect:/students/" + code;
		}
		ra.addFlashAttribute("message", "삭제되었습니다.");
		return "redirect:/students";
	}

	private String showForm(Model model, StudentForm form, boolean edit) {
		model.addAttribute("form", form);
		model.addAttribute("edit", edit);
		model.addAttribute("departments", departmentRepository.findAll());
		return "students/form";
	}
}
