package dev.codedbyjun.college.service;

import dev.codedbyjun.college.dto.StudentDetail;
import dev.codedbyjun.college.dto.StudentForm;
import dev.codedbyjun.college.entity.Department;
import dev.codedbyjun.college.entity.Student;
import dev.codedbyjun.college.entity.StudentDept;
import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.repository.StudentDeptRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

	private final StudentRepository studentRepository;
	private final StudentDeptRepository studentDeptRepository;
	private final DepartmentRepository departmentRepository;
	private final AttendClassRepository attendClassRepository;
	private final PasswordEncoder passwordEncoder;

	public List<Student> search(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return studentRepository.findAllByOrderByCodeAsc();
		}
		return studentRepository.findByNameContainingOrCodeContainingOrderByCodeAsc(keyword, keyword);
	}

	public StudentDetail get(String code) {
		Student student = studentRepository.findById(code)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다: " + code));
		return studentDeptRepository.findFirstByStudentCodeOrderBySeqDesc(code)
			.map(sd -> new StudentDetail(student, sd.getDepartment().getCode(), sd.getDepartment().getName()))
			.orElseGet(() -> new StudentDetail(student, null, "-"));
	}

	@Transactional
	public void create(StudentForm form) {
		if (studentRepository.existsById(form.getCode())) {
			throw new IllegalArgumentException("이미 등록된 학번입니다.");
		}
		if (form.getPassword() == null || form.getPassword().isBlank()) {
			throw new IllegalArgumentException("비밀번호를 입력하세요.");
		}
		Student student = new Student();
		student.setCode(form.getCode());
		apply(student, form);
		studentRepository.save(student);
		addDept(student, form.getDeptCode());
	}

	@Transactional
	public void update(String code, StudentForm form) {
		Student student = studentRepository.findById(code)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학번입니다: " + code));
		apply(student, form);

		// 학과가 바뀌면 이력으로 새 행을 추가 (가장 최근 행이 현재 소속)
		String currentDept = studentDeptRepository.findFirstByStudentCodeOrderBySeqDesc(code)
			.map(sd -> sd.getDepartment().getCode())
			.orElse(null);
		if (!form.getDeptCode().equals(currentDept)) {
			addDept(student, form.getDeptCode());
		}
	}

	@Transactional
	public void delete(String code) {
		if (attendClassRepository.existsByStudentCode(code)) {
			throw new IllegalStateException("수강 신청 기록이 있는 학생은 삭제할 수 없습니다. 학적상태를 변경하세요.");
		}
		studentDeptRepository.deleteByStudentCode(code);
		studentRepository.deleteById(code);
		studentRepository.flush();
	}

	private void apply(Student s, StudentForm f) {
		s.setName(f.getName());
		s.setBirth(f.getBirth());
		s.setAddr(f.getAddr());
		s.setGrade(f.getGrade());
		s.setGender(f.getGender());
		s.setPhone(f.getPhone());
		s.setEmail(f.getEmail());
		s.setEntranceDate(f.getEntranceDate());
		s.setStatus(f.getStatus());
		if (f.getPassword() != null && !f.getPassword().isBlank()) {
			s.setPassword(passwordEncoder.encode(f.getPassword()));
		}
	}

	private void addDept(Student student, String deptCode) {
		Department dept = departmentRepository.findById(deptCode)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과입니다: " + deptCode));
		StudentDept sd = new StudentDept();
		sd.setStudent(student);
		sd.setDepartment(dept);
		sd.setRegDate(LocalDate.now());
		studentDeptRepository.save(sd);
	}
}
