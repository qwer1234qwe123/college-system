package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 학과 정보 */
@Entity
@Table(name = "CS_Department_Info")
@Getter
@Setter
@NoArgsConstructor
public class Department {

	@Id
	@Column(name = "CS_DI_Code", length = 20)
	private String code;

	@Column(name = "CS_DI_Name", length = 20, nullable = false)
	private String name;

	@Column(name = "CS_DI_Rdate", nullable = false)
	private LocalDate regDate;
}
