package dev.codedbyjun.college.support;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/** 학기 표기: "2026-1"(1~6월), "2026-2"(7~12월) */
public final class Terms {

	public static final String PATTERN = "\\d{4}-[12]";

	private Terms() {
	}

	public static String current() {
		LocalDate now = LocalDate.now();
		return now.getYear() + "-" + (now.getMonthValue() <= 6 ? 1 : 2);
	}

	/** 요청 파라미터의 학기 값 검증: 형식이 맞지 않으면 현재 학기 */
	public static String orCurrent(String term) {
		return term != null && term.matches(PATTERN) ? term : current();
	}

	public static String label(String term) {
		if (term == null || !term.matches(PATTERN)) {
			return term;
		}
		return term.substring(0, 4) + "년 " + term.charAt(5) + "학기";
	}

	/** 학기 선택 목록: DB에 있는 학기 + 현재 학기, 최신순 */
	public static List<Option> options(Collection<String> dbTerms) {
		TreeSet<String> set = new TreeSet<>(dbTerms);
		set.add(current());
		List<Option> list = new ArrayList<>();
		for (String t : set.descendingSet()) {
			list.add(new Option(t, label(t)));
		}
		return list;
	}

	public record Option(String value, String label) {
	}
}
