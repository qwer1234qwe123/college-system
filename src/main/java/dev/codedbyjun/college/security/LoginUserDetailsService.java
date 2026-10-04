package dev.codedbyjun.college.security;

import dev.codedbyjun.college.repository.ProfessorRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 학번 또는 교수코드로 로그인.
 * 학생 테이블을 먼저 찾고, 없으면 교수 테이블을 찾는다.
 * (학번과 교수코드가 겹치지 않도록 교수코드는 'P'로 시작하게 운영하는 것을 권장)
 */
@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {

	private final StudentRepository studentRepository;
	private final ProfessorRepository professorRepository;

	@Override
	public UserDetails loadUserByUsername(String code) throws UsernameNotFoundException {
		var student = studentRepository.findById(code);
		if (student.isPresent()) {
			return User.withUsername(student.get().getCode())
				.password(student.get().getPassword())
				.roles("STUDENT")
				.build();
		}
		return professorRepository.findById(code)
			.map(p -> User.withUsername(p.getCode())
				.password(p.getPassword())
				.roles("PROF")
				.build())
			.orElseThrow(() -> new UsernameNotFoundException(code));
	}
}
