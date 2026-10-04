package dev.codedbyjun.college.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 과목 강의시간 (학생 시간표는 수강신청 + 강의시간으로 만든다) */
@Entity
@Table(name = "CS_Time_Info")
@Getter
@Setter
@NoArgsConstructor
public class TimeTable {

	public static final String[] DAY_NAMES = {"", "월", "화", "수", "목", "금"};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CS_TI_Num")
	private Integer num;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "CS_SBI_Code")
	private Subject subject;

	/** 강의 요일 (1=월 ~ 5=금) */
	@Column(name = "CS_TI_Day", columnDefinition = "char(1)", nullable = false)
	private String day;

	/** 교시 범위 "시작-끝" (예: 1-3) */
	@Column(name = "CS_TI_Time", length = 10, nullable = false)
	private String time;

	public TimeTable(Subject subject, int day, int start, int end) {
		this.subject = subject;
		this.day = String.valueOf(day);
		this.time = start + "-" + end;
	}

	public int getDayNum() {
		return Integer.parseInt(day);
	}

	public int getStart() {
		return Integer.parseInt(time.split("-")[0]);
	}

	public int getEnd() {
		String[] p = time.split("-");
		return Integer.parseInt(p[p.length - 1]);
	}

	public boolean overlaps(TimeTable o) {
		return getDayNum() == o.getDayNum() && getStart() <= o.getEnd() && o.getStart() <= getEnd();
	}

	/** 예: 월 1-3교시 */
	public String getLabel() {
		return DAY_NAMES[getDayNum()] + " " + (getStart() == getEnd() ? getStart() : time) + "교시";
	}
}
