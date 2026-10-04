package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 학생 기본 정보 */
@Entity
@Table(name = "CS_Student_Info")
@Getter
@Setter
@NoArgsConstructor
public class Student {

	@Id
	@Column(name = "CS_SI_Code", length = 20)
	private String code;

	@Column(name = "CS_SI_Name", length = 20, nullable = false)
	private String name;

	@Column(name = "CS_SI_Birth", nullable = false)
	private LocalDate birth;

	@Column(name = "CS_SI_Addr", length = 100, nullable = false)
	private String addr;

	/** 학년 (1~4) */
	@Column(name = "CS_SI_Class", columnDefinition = "char(1)", nullable = false)
	private String grade;

	/** 성별 (M/F) */
	@Column(name = "CS_SI_Gender", columnDefinition = "char(1)", nullable = false)
	private String gender;

	@Column(name = "CS_SI_Phone", length = 20, nullable = false)
	private String phone;

	@Column(name = "CS_SI_Email", length = 40, nullable = false)
	private String email;

	/** 입학년도 */
	@Column(name = "CS_SI_Rdate", nullable = false)
	private LocalDate entranceDate;

	/** 학적상태 (재학/휴학/졸업 등) */
	@Column(name = "CS_SI_Condition", length = 10, nullable = false)
	private String status;

	/** BCrypt 해시 */
	@Column(name = "CS_SI_Password", length = 100, nullable = false)
	private String password;
}
