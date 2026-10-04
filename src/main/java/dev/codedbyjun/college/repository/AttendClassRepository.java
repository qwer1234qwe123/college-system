package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.AttendClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AttendClassRepository extends JpaRepository<AttendClass, Integer> {

	boolean existsByStudentCode(String studentCode);

	boolean existsBySubjectCode(String subjectCode);

	boolean existsBySubjectCodeAndStudentCode(String subjectCode, String studentCode);

	long countBySubjectCode(String subjectCode);

	Optional<AttendClass> findBySubjectCodeAndStudentCode(String subjectCode, String studentCode);

	List<AttendClass> findByStudentCodeAndSubjectTerm(String studentCode, String term);

	List<AttendClass> findByStudentCodeOrderBySubjectTermDescSubjectCodeAsc(String studentCode);

	List<AttendClass> findBySubjectCodeOrderByStudentCodeAsc(String subjectCode);

	@Query("select coalesce(sum(a.subject.credit), 0) from AttendClass a where a.student.code = :studentCode and a.subject.term = :term")
	int sumCredits(String studentCode, String term);

	/** 과목코드별 신청 인원 [과목코드, 인원] */
	@Query("select a.subject.code, count(a) from AttendClass a where a.subject.term = :term group by a.subject.code")
	List<Object[]> countBySubjectInTerm(String term);
}
