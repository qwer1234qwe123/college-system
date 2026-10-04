package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** 성적 정보 (수강 신청 1건당 1개) */
@Entity
@Table(name = "CS_Grade_Info")
@Getter
@Setter
@NoArgsConstructor
public class Grade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CS_GI_Seq")
	private Integer seq;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_AI_Seq", unique = true)
	private AttendClass attendClass;

	@Column(name = "CS_GI_Grade", nullable = false)
	private Integer score;

	/** 성적 입력 일자 */
	@Column(name = "CS_GI_Date", nullable = false)
	private LocalDate inputDate;
}
