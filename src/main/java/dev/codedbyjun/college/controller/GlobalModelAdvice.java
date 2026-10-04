package dev.codedbyjun.college.controller;

import dev.codedbyjun.college.entity.Professor;
import dev.codedbyjun.college.entity.Student;
import dev.codedbyjun.college.repository.ProfessorRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** 모든 화면 공통: 현재 경로(메뉴 활성화), 로그인 사용자 이름 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

	private final StudentRepository studentRepository;
	private final ProfessorRepository professorRepository;

	@ModelAttribute("currentPath")
	public String currentPath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getContextPath().length());
	}

	@ModelAttribute("currentUserName")
	public String currentUserName(Authentication auth) {
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return null;
		}
		return studentRepository.findById(auth.getName()).map(Student::getName)
			.or(() -> professorRepository.findById(auth.getName()).map(Professor::getName))
			.orElse(auth.getName());
	}
}
