package dev.codedbyjun.college.service;

import dev.codedbyjun.college.dto.SubjectRow;
import dev.codedbyjun.college.entity.AttendClass;
import dev.codedbyjun.college.entity.Student;
import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.entity.TimeTable;
import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.GradeRepository;
import dev.codedbyjun.college.repository.StudentRepository;
import dev.codedbyjun.college.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 수강신청 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollService {

	/** 학기당 최대 신청 학점 */
	public static final int MAX_CREDITS = 21;

	private final SubjectRepository subjectRepository;
	private final StudentRepository studentRepository;
	private final AttendClassRepository attendClassRepository;
	private final GradeRepository gradeRepository;
	private final SubjectService subjectService;

	public record EnrollRow(SubjectRow subject, boolean applied) {
	}

	/** 학기 개설 과목 + 내가 신청했는지 여부 */
	public List<EnrollRow> rows(String studentCode, String term) {
		Set<String> applied = attendClassRepository.findByStudentCodeAndSubjectTerm(studentCode, term).stream()
			.map(a -> a.getSubject().getCode())
			.collect(Collectors.toSet());
		Map<String, Long> counts = subjectService.enrolledCounts(term);
		return subjectRepository.findByTermOrderByCodeAsc(term).stream()
			.map(s -> new EnrollRow(SubjectRow.of(s, counts.getOrDefault(s.getCode(), 0L)), applied.contains(s.getCode())))
			.toList();
	}

	public int myCredits(String studentCode, String term) {
		return attendClassRepository.sumCredits(studentCode, term);
	}

	@Transactional
	public String apply(String studentCode, String subjectCode) {
		Student student = studentRepository.findById(studentCode)
			.orElseThrow(() -> new IllegalStateException("학생 정보를 찾을 수 없습니다."));
		if (!"재학".equals(student.getStatus())) {
			throw new IllegalStateException("재학 중인 학생만 수강신청할 수 있습니다. (현재: " + student.getStatus() + ")");
		}

		// 동시에 여러 명이 신청해도 정원을 넘지 않도록 과목 행을 잠근 뒤 검사
		Subject subject = subjectRepository.findByIdForUpdate(subjectCode)
			.orElseThrow(() -> new IllegalStateException("존재하지 않는 과목입니다."));

		if (attendClassRepository.existsBySubjectCodeAndStudentCode(subjectCode, studentCode)) {
			throw new IllegalStateException("이미 신청한 과목입니다.");
		}
		if (attendClassRepository.countBySubjectCode(subjectCode) >= subject.getCapacity()) {
			throw new IllegalStateException("'" + subject.getName() + "' 과목은 정원(" + subject.getCapacity() + "명)이 찼습니다.");
		}
		int credits = attendClassRepository.sumCredits(studentCode, subject.getTerm());
		if (credits + subject.getCredit() > MAX_CREDITS) {
			throw new IllegalStateException("학기당 최대 " + MAX_CREDITS + "학점까지 신청할 수 있습니다. (현재 " + credits + "학점)");
		}
		for (AttendClass mine : attendClassRepository.findByStudentCodeAndSubjectTerm(studentCode, subject.getTerm())) {
			for (TimeTable a : mine.getSubject().getTimes()) {
				for (TimeTable b : subject.getTimes()) {
					if (a.overlaps(b)) {
						throw new IllegalStateException("'" + mine.getSubject().getName() + "' 과목과 시간이 겹칩니다. (" + a.getLabel() + ")");
					}
				}
			}
		}

		AttendClass attend = new AttendClass();
		attend.setStudent(student);
		attend.setSubject(subject);
		attend.setApplyDate(LocalDate.now());
		attendClassRepository.save(attend);
		return subject.getName();
	}

	@Transactional
	public String cancel(String studentCode, String subjectCode) {
		AttendClass attend = attendClassRepository.findBySubjectCodeAndStudentCode(subjectCode, studentCode)
			.orElseThrow(() -> new IllegalStateException("신청 내역이 없습니다."));
		if (gradeRepository.existsByAttendClassSeq(attend.getSeq())) {
			throw new IllegalStateException("성적이 입력된 과목은 취소할 수 없습니다.");
		}
		String name = attend.getSubject().getName();
		attendClassRepository.delete(attend);
		return name;
	}
}
