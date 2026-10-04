package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 학생 소속 학과 */
@Entity
@Table(name = "CS_Student_Dept")
@Getter
@Setter
@NoArgsConstructor
public class StudentDept {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CS_SD_Seq")
	private Integer seq;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_SI_Code")
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_DI_Code")
	private Department department;

	@Column(name = "CS_SD_Rdate", nullable = false)
	private LocalDate regDate;
}
