package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 수강 신청 */
@Entity
@Table(name = "CS_AttendClass_Info")
@Getter
@Setter
@NoArgsConstructor
public class AttendClass {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CS_AI_Seq")
	private Integer seq;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_SBI_Code")
	private Subject subject;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_SI_Code")
	private Student student;

	/** 신청 일자 */
	@Column(name = "CS_CI_Date", nullable = false)
	private LocalDate applyDate;
}
