package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.entity.Department;
import dev.codedbyjun.college.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** 학과 관리 (교수 전용) - 학생 등록 전에 학과가 있어야 한다 */
@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

	private final DepartmentRepository departmentRepository;

	@GetMapping
	public String list(Model model) {
		model.addAttribute("departments", departmentRepository.findAll());
		return "departments/list";
	}

	@PostMapping
	public String create(@RequestParam String code, @RequestParam String name, RedirectAttributes ra) {
		code = code.trim();
		name = name.trim();
		if (code.isEmpty() || name.isEmpty() || code.length() > 20 || name.length() > 20) {
			ra.addFlashAttribute("error", "학과 코드와 이름을 20자 이내로 입력하세요.");
		} else if (departmentRepository.existsById(code)) {
			ra.addFlashAttribute("error", "이미 있는 학과 코드입니다.");
		} else {
			Department d = new Department();
			d.setCode(code);
			d.setName(name);
			d.setRegDate(LocalDate.now());
			departmentRepository.save(d);
			ra.addFlashAttribute("message", "학과가 추가되었습니다.");
		}
		return "redirect:/departments";
	}
}
