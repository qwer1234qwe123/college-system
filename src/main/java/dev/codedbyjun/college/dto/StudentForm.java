package dev.codedbyjun.college.dto;

import dev.codedbyjun.college.entity.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** 학생 등록/수정 화면 입력값 */
@Getter
@Setter
public class StudentForm {

	@NotBlank(message = "학번을 입력하세요.")
	@Size(max = 20)
	private String code;

	@NotBlank(message = "이름을 입력하세요.")
	@Size(max = 20)
	private String name;

	@NotNull(message = "생년월일을 입력하세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birth;

	@NotBlank(message = "주소를 입력하세요.")
	@Size(max = 100)
	private String addr;

	@Pattern(regexp = "[1-4]", message = "학년은 1~4 중 하나입니다.")
	private String grade;

	@Pattern(regexp = "[MF]", message = "성별을 선택하세요.")
	private String gender;

	@NotBlank(message = "전화번호를 입력하세요.")
	@Size(max = 20)
	private String phone;

	@NotBlank(message = "이메일을 입력하세요.")
	@Email(message = "이메일 형식이 아닙니다.")
	@Size(max = 40)
	private String email;

	@NotNull(message = "입학일을 입력하세요.")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate entranceDate;

	@NotBlank(message = "학적상태를 선택하세요.")
	@Size(max = 10)
	private String status;

	/** 등록 시 필수, 수정 시 비워두면 기존 비밀번호 유지 */
	@Size(max = 50)
	private String password;

	@NotBlank(message = "학과를 선택하세요.")
	private String deptCode;

	public static StudentForm from(Student s, String deptCode) {
		StudentForm f = new StudentForm();
		f.setCode(s.getCode());
		f.setName(s.getName());
		f.setBirth(s.getBirth());
		f.setAddr(s.getAddr());
		f.setGrade(s.getGrade());
		f.setGender(s.getGender());
		f.setPhone(s.getPhone());
		f.setEmail(s.getEmail());
		f.setEntranceDate(s.getEntranceDate());
		f.setStatus(s.getStatus());
		f.setDeptCode(deptCode);
		return f;
	}
}
