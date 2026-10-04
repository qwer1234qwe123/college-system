package dev.codedbyjun.college.dto;

import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.entity.TimeTable;
import dev.codedbyjun.college.support.Terms;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 과목 개설/수정 화면 입력값 */
@Getter
@Setter
public class SubjectForm {

	public static final int SLOT_COUNT = 3;

	@NotBlank(message = "과목 코드를 입력하세요.")
	@Size(max = 20)
	private String code;

	@NotBlank(message = "과목명을 입력하세요.")
	@Size(max = 20, message = "과목명은 20자 이내입니다.")
	private String name;

	@NotBlank(message = "개설 학과를 선택하세요.")
	private String deptCode;

	@NotNull(message = "학점을 선택하세요.")
	@Min(1) @Max(4)
	private Integer credit;

	@Pattern(regexp = "[ABCD]", message = "이수구분을 선택하세요.")
	private String majorType;

	@NotBlank(message = "강의실을 입력하세요.")
	@Size(max = 10, message = "강의실은 10자 이내입니다.")
	private String room;

	@NotNull(message = "정원을 입력하세요.")
	@Min(value = 1, message = "정원은 1명 이상입니다.")
	@Max(value = 500, message = "정원은 500명 이하입니다.")
	private Integer capacity;

	@Pattern(regexp = Terms.PATTERN, message = "학기 형식은 2026-2 처럼 입력하세요.")
	private String term;

	@Valid
	private List<Slot> slots = emptySlots();

	@Getter
	@Setter
	public static class Slot {
		/** 1=월 ~ 5=금, 빈 값이면 사용 안 함 */
		private String day;
		private Integer start;
		private Integer end;

		public boolean isEmpty() {
			return day == null || day.isBlank();
		}
	}

	public static List<Slot> emptySlots() {
		List<Slot> list = new ArrayList<>();
		for (int i = 0; i < SLOT_COUNT; i++) {
			list.add(new Slot());
		}
		return list;
	}

	public static SubjectForm from(Subject s) {
		SubjectForm f = new SubjectForm();
		f.setCode(s.getCode());
		f.setName(s.getName());
		f.setDeptCode(s.getDepartment().getCode());
		f.setCredit(s.getCredit());
		f.setMajorType(s.getMajorType());
		f.setRoom(s.getRoom());
		f.setCapacity(s.getCapacity());
		f.setTerm(s.getTerm());
		List<TimeTable> times = s.getTimes();
		for (int i = 0; i < Math.min(times.size(), SLOT_COUNT); i++) {
			Slot slot = f.getSlots().get(i);
			slot.setDay(times.get(i).getDay());
			slot.setStart(times.get(i).getStart());
			slot.setEnd(times.get(i).getEnd());
		}
		return f;
	}
}
