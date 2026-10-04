package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Integer> {

	Optional<Grade> findByAttendClassSeq(Integer attendSeq);

	boolean existsByAttendClassSeq(Integer attendSeq);

	List<Grade> findByAttendClassSeqIn(Collection<Integer> attendSeqs);

	/** 과목코드별 성적 입력 인원 [과목코드, 인원] */
	@Query("select g.attendClass.subject.code, count(g) from Grade g where g.attendClass.subject.term = :term group by g.attendClass.subject.code")
	List<Object[]> countBySubjectInTerm(String term);
}
