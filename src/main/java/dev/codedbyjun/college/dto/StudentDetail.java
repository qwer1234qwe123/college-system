package dev.codedbyjun.college.dto;

import dev.codedbyjun.college.entity.Student;

/** 학생 정보 + 현재 소속 학과 */
public record StudentDetail(Student student, String deptCode, String deptName) {
}
