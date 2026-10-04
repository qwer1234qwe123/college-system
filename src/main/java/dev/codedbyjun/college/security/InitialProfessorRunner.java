package dev.codedbyjun.college.security;

import dev.codedbyjun.college.entity.Professor;
import dev.codedbyjun.college.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 첫 로그인용 교수 계정 생성.
 * 환경변수 INIT_PROF_CODE, INIT_PROF_PASSWORD 가 둘 다 있고 해당 계정이 없을 때만 동작한다.
 * 계정이 만들어진 뒤에는 환경변수를 지워도 된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitialProfessorRunner implements ApplicationRunner {

	private final ProfessorRepository professorRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.init-prof.code:}")
	private String code;

	@Value("${app.init-prof.password:}")
	private String password;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (code.isBlank() || password.isBlank() || professorRepository.existsById(code)) {
			return;
		}
		Professor prof = new Professor();
		prof.setCode(code);
		prof.setName("관리자");
		prof.setEmail("admin@codedbyjun.dev");
		prof.setPhone("000-0000-0000");
		prof.setRegDate(LocalDate.now());
		prof.setPassword(passwordEncoder.encode(password));
		professorRepository.save(prof);
		log.info("초기 교수 계정 생성: {}", code);
	}
}
