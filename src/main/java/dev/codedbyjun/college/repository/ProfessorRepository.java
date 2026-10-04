package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, String> {
}
