package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.StudentDept;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentDeptRepository extends JpaRepository<StudentDept, Integer> {

	Optional<StudentDept> findFirstByStudentCodeOrderBySeqDesc(String studentCode);

	void deleteByStudentCode(String studentCode);
}
