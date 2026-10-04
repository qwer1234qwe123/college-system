package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}
