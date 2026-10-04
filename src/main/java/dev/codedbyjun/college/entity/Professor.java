package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 교수 기본 정보 */
@Entity
@Table(name = "CS_Prof_Info")
@Getter
@Setter
@NoArgsConstructor
public class Professor {

	@Id
	@Column(name = "CS_PI_Code", length = 20)
	private String code;

	@Column(name = "CS_PI_Name", length = 20, nullable = false)
	private String name;

	@Column(name = "CS_PI_Email", length = 40, nullable = false)
	private String email;

	@Column(name = "CS_PI_Phone", length = 20, nullable = false)
	private String phone;

	/** 임용일자 */
	@Column(name = "CS_PI_Rdate", nullable = false)
	private LocalDate regDate;

	/** BCrypt 해시 */
	@Column(name = "CS_PI_Password", length = 100, nullable = false)
	private String password;
}
