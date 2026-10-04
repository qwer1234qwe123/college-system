package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {

	List<Student> findByNameContainingOrCodeContainingOrderByCodeAsc(String name, String code);

	List<Student> findAllByOrderByCodeAsc();
}
