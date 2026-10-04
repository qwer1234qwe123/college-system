package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.Subject;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, String> {

	List<Subject> findByTermOrderByCodeAsc(String term);

	List<Subject> findByTermAndProfessorCodeOrderByCodeAsc(String term, String professorCode);

	@Query("select distinct s.term from Subject s order by s.term desc")
	List<String> findTerms();

	/** 수강신청 정원 체크 동시성 처리용: 과목 행을 잠근다 (SELECT ... FOR UPDATE) */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from Subject s where s.code = :code")
	Optional<Subject> findByIdForUpdate(String code);
}
