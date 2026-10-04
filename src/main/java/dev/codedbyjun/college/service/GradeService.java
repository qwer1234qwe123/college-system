package dev.codedbyjun.college.service;

import dev.codedbyjun.college.dto.SubjectRow;
import dev.codedbyjun.college.entity.AttendClass;
import dev.codedbyjun.college.entity.Grade;
import dev.codedbyjun.college.entity.Subject;
import dev.codedbyjun.college.repository.AttendClassRepository;
import dev.codedbyjun.college.repository.GradeRepository;
import dev.codedbyjun.college.repository.SubjectRepository;
import dev.codedbyjun.college.support.GradeScale;
import dev.codedbyjun.college.support.Terms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 성적 입력(교수) / 성적 조회(학생) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

	private final SubjectRepository subjectRepository;
	private final AttendClassRepository attendClassRepository;
	private final GradeRepository gradeRepository;
	private final SubjectService subjectService;

	// ---------- 교수 ----------

	public record LectureRow(SubjectRow subject, long graded) {
		public boolean done() {
			return subject.enrolled() > 0 && graded >= subject.enrolled();
		}
	}

	public record RosterRow(int attendSeq, String studentCode, String studentName, String grade, String status,
							Integer score, String letter) {
	}

	public record Roster(SubjectRow subject, List<RosterRow> rows) {
	}

	/** 내 강의 목록 + 성적 입력 인원 */
	public List<LectureRow> lectures(String profCode, String term) {
		Map<String, Long> graded = new HashMap<>();
		for (Object[] row : gradeRepository.countBySubjectInTerm(term)) {
			graded.put((String) row[0], (Long) row[1]);
		}
		return subjectService.list(term, profCode).stream()
			.map(s -> new LectureRow(s, graded.getOrDefault(s.code(), 0L)))
			.toList();
	}

	public Roster roster(String subjectCode, String profCode) {
		Subject subject = getOwned(subjectCode, profCode);
		List<AttendClass> attends = attendClassRepository.findBySubjectCodeOrderByStudentCodeAsc(subjectCode);
		Map<Integer, Grade> grades = gradesOf(attends);
		List<RosterRow> rows = attends.stream().map(a -> {
			Grade g = grades.get(a.getSeq());
			Integer score = g == null ? null : g.getScore();
			return new RosterRow(a.getSeq(), a.getStudent().getCode(), a.getStudent().getName(),
				a.getStudent().getGrade(), a.getStudent().getStatus(), score, GradeScale.letter(score));
		}).toList();
		return new Roster(SubjectRow.of(subject, attends.size()), rows);
	}

	/** 점수 저장: 빈 칸은 성적 삭제(미입력), 0~100만 허용 */
	@Transactional
	public int save(String subjectCode, String profCode, Map<Integer, Integer> scores) {
		getOwned(subjectCode, profCode);
		List<AttendClass> attends = attendClassRepository.findBySubjectCodeOrderByStudentCodeAsc(subjectCode);
		Map<Integer, Grade> grades = gradesOf(attends);
		int saved = 0;
		for (AttendClass a : attends) {
			Integer score = scores.get(a.getSeq());
			Grade g = grades.get(a.getSeq());
			if (score == null) {
				if (g != null) {
					gradeRepository.delete(g);
				}
				continue;
			}
			if (score < 0 || score > 100) {
				throw new IllegalArgumentException(a.getStudent().getName() + " 학생의 점수가 0~100 범위를 벗어났습니다.");
			}
			if (g == null) {
				g = new Grade();
				g.setAttendClass(a);
			} else if (g.getScore().equals(score)) {
				saved++;
				continue;
			}
			g.setScore(score);
			g.setInputDate(LocalDate.now());
			gradeRepository.save(g);
			saved++;
		}
		return saved;
	}

	private Subject getOwned(String subjectCode, String profCode) {
		Subject s = subjectRepository.findById(subjectCode)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));
		if (!s.getProfessor().getCode().equals(profCode)) {
			throw new IllegalStateException("본인이 담당하는 과목의 성적만 입력할 수 있습니다.");
		}
		return s;
	}

	private Map<Integer, Grade> gradesOf(List<AttendClass> attends) {
		List<Integer> seqs = attends.stream().map(AttendClass::getSeq).toList();
		if (seqs.isEmpty()) {
			return Map.of();
		}
		return gradeRepository.findByAttendClassSeqIn(seqs).stream()
			.collect(Collectors.toMap(g -> g.getAttendClass().getSeq(), Function.identity()));
	}

	// ---------- 학생 ----------

	public record MyGradeRow(String code, String name, String majorLabel, int credit, String profName,
							 Integer score, String letter) {
	}

	public record TermGrades(String term, String termLabel, List<MyGradeRow> rows,
							 int credits, int gradedCredits, Double gpa) {
	}

	public record Report(List<TermGrades> terms, int totalCredits, int earnedCredits, Double gpa) {
	}

	/** 학기별 성적 + 평점(4.5 만점, 학점 가중 평균) */
	public Report report(String studentCode) {
		List<AttendClass> attends = attendClassRepository.findByStudentCodeOrderBySubjectTermDescSubjectCodeAsc(studentCode);
		Map<Integer, Grade> grades = gradesOf(attends);

		Map<String, List<AttendClass>> byTerm = new LinkedHashMap<>();
		for (AttendClass a : attends) {
			byTerm.computeIfAbsent(a.getSubject().getTerm(), k -> new ArrayList<>()).add(a);
		}

		List<TermGrades> terms = new ArrayList<>();
		double allPoints = 0;
		int allGraded = 0, allCredits = 0, earned = 0;
		for (var e : byTerm.entrySet()) {
			List<MyGradeRow> rows = new ArrayList<>();
			double points = 0;
			int credits = 0, gradedCredits = 0;
			for (AttendClass a : e.getValue()) {
				Subject s = a.getSubject();
				Grade g = grades.get(a.getSeq());
				Integer score = g == null ? null : g.getScore();
				rows.add(new MyGradeRow(s.getCode(), s.getName(), Subject.majorTypeLabel(s.getMajorType()),
					s.getCredit(), s.getProfessor().getName(), score, GradeScale.letter(score)));
				credits += s.getCredit();
				if (score != null) {
					gradedCredits += s.getCredit();
					points += GradeScale.point(score) * s.getCredit();
					if (score >= 60) {
						earned += s.getCredit();
					}
				}
			}
			terms.add(new TermGrades(e.getKey(), Terms.label(e.getKey()), rows, credits, gradedCredits,
				gradedCredits == 0 ? null : points / gradedCredits));
			allPoints += points;
			allGraded += gradedCredits;
			allCredits += credits;
		}
		return new Report(terms, allCredits, earned, allGraded == 0 ? null : allPoints / allGraded);
	}
}
