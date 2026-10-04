package dev.codedbyjun.college.repository;

import dev.codedbyjun.college.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeTableRepository extends JpaRepository<TimeTable, Integer> {
}
