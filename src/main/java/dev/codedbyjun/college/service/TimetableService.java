package dev.codedbyjun.college.service;

import dev.codedbyjun.college.entity.AttendClass;
import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.entity.TimeTable;
import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/** 주간 시간표 (월~금, 1~9교시) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

	public static final int PERIODS = 9;
	public static final int COLORS = 8;

	private final AttendClassRepository attendClassRepository;
	private final SubjectRepository subjectRepository;

	/** 시간표 칸 하나: grid 위치(day 1~5, start~end 교시)와 표시 정보 */
	public record Block(String code, String name, String room, String profName,
						int day, int start, int end, int color) {
	}

	public record Timetable(List<Block> blocks, List<Subject> noTime, int credits, int subjectCount) {
	}

	public Timetable forStudent(String studentCode, String term) {
		List<Subject> subjects = attendClassRepository.findByStudentCodeAndSubjectTerm(studentCode, term).stream()
			.map(AttendClass::getSubject)
			.toList();
		return build(subjects);
	}

	public Timetable forProfessor(String profCode, String term) {
		return build(subjectRepository.findByTermAndProfessorCodeOrderByCodeAsc(term, profCode));
	}

	private Timetable build(List<Subject> subjects) {
		List<Block> blocks = new ArrayList<>();
		List<Subject> noTime = new ArrayList<>();
		int credits = 0;
		for (int i = 0; i < subjects.size(); i++) {
			Subject s = subjects.get(i);
			credits += s.getCredit();
			if (s.getTimes().isEmpty()) {
				noTime.add(s);
			}
			for (TimeTable t : s.getTimes()) {
				blocks.add(new Block(s.getCode(), s.getName(), s.getRoom(), s.getProfessor().getName(),
					t.getDayNum(), t.getStart(), t.getEnd(), i % COLORS));
			}
		}
		return new Timetable(blocks, noTime, credits, subjects.size());
	}
}
