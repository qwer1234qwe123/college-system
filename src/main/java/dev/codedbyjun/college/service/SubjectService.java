package dev.codedbyjun.college.service;

import dev.codedbyjun.college.dto.SubjectForm;
import dev.codedbyjun.college.dto.SubjectRow;
import dev.codedbyjun.college.entity.Department;
import dev.codedbyjun.college.entity.Professor;
import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.entity.TimeTable;
import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.DepartmentRepository;
import dev.codedbyjun.college.repository.ProfessorRepository;
import dev.codedbyjun.college.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectService {

	private final SubjectRepository subjectRepository;
	private final DepartmentRepository departmentRepository;
	private final ProfessorRepository professorRepository;
	private final AttendClassRepository attendClassRepository;

	/** 학기별 과목 목록 (profCode가 있으면 그 교수 과목만) */
	public List<SubjectRow> list(String term, String profCode) {
		List<Subject> subjects = profCode == null
			? subjectRepository.findByTermOrderByCodeAsc(term)
			: subjectRepository.findByTermAndProfessorCodeOrderByCodeAsc(term, profCode);
		Map<String, Long> counts = enrolledCounts(term);
		return subjects.stream()
			.map(s -> SubjectRow.of(s, counts.getOrDefault(s.getCode(), 0L)))
			.toList();
	}

	public List<String> terms() {
		return subjectRepository.findTerms();
	}

	public Map<String, Long> enrolledCounts(String term) {
		Map<String, Long> counts = new HashMap<>();
		for (Object[] row : attendClassRepository.countBySubjectInTerm(term)) {
			counts.put((String) row[0], (Long) row[1]);
		}
		return counts;
	}

	public SubjectForm getForm(String code, String profCode) {
		return SubjectForm.from(getOwned(code, profCode));
	}

	@Transactional
	public void create(SubjectForm form, String profCode) {
		if (subjectRepository.existsById(form.getCode())) {
			throw new IllegalArgumentException("이미 있는 과목 코드입니다.");
		}
		Professor prof = professorRepository.findById(profCode)
			.orElseThrow(() -> new IllegalArgumentException("교수 정보를 찾을 수 없습니다."));
		Subject s = new Subject();
		s.setCode(form.getCode().trim());
		s.setProfessor(prof);
		s.setOpenDate(LocalDate.now());
		apply(s, form);
		subjectRepository.save(s);
	}

	@Transactional
	public void update(String code, SubjectForm form, String profCode) {
		Subject s = getOwned(code, profCode);
		long enrolled = attendClassRepository.countBySubjectCode(code);
		if (form.getCapacity() < enrolled) {
			throw new IllegalArgumentException("정원을 현재 신청 인원(" + enrolled + "명)보다 적게 줄일 수 없습니다.");
		}
		if (enrolled > 0 && !s.getTerm().equals(form.getTerm())) {
			throw new IllegalArgumentException("수강생이 있는 과목은 학기를 바꿀 수 없습니다.");
		}
		apply(s, form);
	}

	@Transactional
	public void delete(String code, String profCode) {
		Subject s = getOwned(code, profCode);
		if (attendClassRepository.existsBySubjectCode(code)) {
			throw new IllegalStateException("수강 신청한 학생이 있는 과목은 삭제할 수 없습니다.");
		}
		subjectRepository.delete(s);
	}

	private Subject getOwned(String code, String profCode) {
		Subject s = subjectRepository.findById(code)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다: " + code));
		if (!s.getProfessor().getCode().equals(profCode)) {
			throw new IllegalStateException("본인이 담당하는 과목만 수정할 수 있습니다.");
		}
		return s;
	}

	private void apply(Subject s, SubjectForm f) {
		Department dept = departmentRepository.findById(f.getDeptCode())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학과입니다."));
		s.setName(f.getName().trim());
		s.setDepartment(dept);
		s.setCredit(f.getCredit());
		s.setMajorType(f.getMajorType());
		s.setRoom(f.getRoom().trim());
		s.setCapacity(f.getCapacity());
		s.setTerm(f.getTerm());

		List<TimeTable> times = toTimes(s, f.getSlots());
		s.getTimes().clear();
		s.getTimes().addAll(times);
	}

	/** 입력한 강의시간 검사 후 변환 */
	private List<TimeTable> toTimes(Subject s, List<SubjectForm.Slot> slots) {
		List<TimeTable> result = new ArrayList<>();
		for (SubjectForm.Slot slot : slots) {
			if (slot.isEmpty()) {
				continue;
			}
			int day = Integer.parseInt(slot.getDay());
			if (day < 1 || day > 5) {
				throw new IllegalArgumentException("요일이 올바르지 않습니다.");
			}
			if (slot.getStart() == null || slot.getEnd() == null) {
				throw new IllegalArgumentException("강의시간의 시작/끝 교시를 모두 선택하세요.");
			}
			if (slot.getStart() < 1 || slot.getEnd() > 9 || slot.getStart() > slot.getEnd()) {
				throw new IllegalArgumentException("교시는 1~9 사이이고, 시작이 끝보다 늦을 수 없습니다.");
			}
			TimeTable t = new TimeTable(s, day, slot.getStart(), slot.getEnd());
			for (TimeTable other : result) {
				if (t.overlaps(other)) {
					throw new IllegalArgumentException("강의시간끼리 겹칩니다: " + t.getLabel() + " / " + other.getLabel());
				}
			}
			result.add(t);
		}
		if (result.isEmpty()) {
			throw new IllegalArgumentException("강의시간을 1개 이상 입력하세요.");
		}
		return result;
	}
}
