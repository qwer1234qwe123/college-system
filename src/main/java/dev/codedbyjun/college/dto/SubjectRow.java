package dev.codedbyjun.college.dto;

import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.support.Terms;

/** 과목 목록 한 줄 */
public record SubjectRow(
	String code,
	String name,
	String profCode,
	String profName,
	String deptName,
	int credit,
	String majorType,
	String majorLabel,
	String room,
	int capacity,
	long enrolled,
	String term,
	String termLabel,
	String timeText
) {
	public static SubjectRow of(Subject s, long enrolled) {
		return new SubjectRow(
			s.getCode(), s.getName(),
			s.getProfessor().getCode(), s.getProfessor().getName(),
			s.getDepartment().getName(),
			s.getCredit(), s.getMajorType(), Subject.majorTypeLabel(s.getMajorType()),
			s.getRoom(), s.getCapacity(), enrolled,
			s.getTerm(), Terms.label(s.getTerm()), s.getTimeText());
	}

	public boolean full() {
		return enrolled >= capacity;
	}

	/** 정원 대비 신청률 (0~100) */
	public int fillPercent() {
		return capacity == 0 ? 100 : (int) Math.min(100, enrolled * 100 / capacity);
	}
}
