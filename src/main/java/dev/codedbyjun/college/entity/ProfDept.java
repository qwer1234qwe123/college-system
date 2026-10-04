package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 교수 소속 학과 */
@Entity
@Table(name = "CS_Prof_Dept")
@Getter
@Setter
@NoArgsConstructor
public class ProfDept {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CS_PD_Number")
	private Integer number;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_DI_Code")
	private Department department;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_PI_Code")
	private Professor professor;

	@Column(name = "CS_PD_Rdate", nullable = false)
	private LocalDate regDate;

	/** 학과장 여부 (Y/N) */
	@Column(name = "CS_PD_Task", columnDefinition = "char(1)", nullable = false)
	private String headYn = "N";
}
