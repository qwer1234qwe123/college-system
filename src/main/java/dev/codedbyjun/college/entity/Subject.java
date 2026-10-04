package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 과목 정보 */
@Entity
@Table(name = "CS_Subject_Info")
@Getter
@Setter
@NoArgsConstructor
public class Subject {

	@Id
	@Column(name = "CS_SBI_Code", length = 20)
	private String code;

	@Column(name = "CS_SBI_Name", length = 20, nullable = false)
	private String name;

	/** 담당 교수 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_PI_Code")
	private Professor professor;

	/** 개설 학과 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_DI_Code")
	private Department department;

	@Column(name = "CS_SBI_Date", nullable = false)
	private LocalDate openDate;

	@Column(name = "CS_SBI_Credit", nullable = false)
	private Integer credit;

	/** A=전공필수, B=전공선택, C=교양필수, D=교양선택 */
	@Column(name = "CS_SBI_Major", columnDefinition = "char(1)", nullable = false)
	private String majorType;

	/** 강의실 정보 */
	@Column(name = "CS_SBI_Info", length = 10, nullable = false)
	private String room;

	/** 수강 최대 인원 */
	@Column(name = "CS_SBI_Num", nullable = false)
	private Integer capacity;

	/** 개설 학기 (예: 2026-2) */
	@Column(name = "CS_SBI_Term", length = 10, nullable = false)
	private String term;

	/** 강의시간 */
	@OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("day asc, time asc")
	private List<TimeTable> times = new ArrayList<>();

	public static String majorTypeLabel(String type) {
		return switch (type) {
			case "A" -> "전공필수";
			case "B" -> "전공선택";
			case "C" -> "교양필수";
			case "D" -> "교양선택";
			default -> type;
		};
	}

	/** 예: 월 1-3교시, 수 2교시 */
	public String getTimeText() {
		if (times.isEmpty()) {
			return "-";
		}
		return times.stream().map(TimeTable::getLabel).collect(Collectors.joining(", "));
	}
}
