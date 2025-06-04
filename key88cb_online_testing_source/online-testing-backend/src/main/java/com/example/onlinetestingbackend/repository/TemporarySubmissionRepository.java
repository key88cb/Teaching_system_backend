package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.TemporarySubmission;
import com.example.onlinetestingbackend.entity.id.ExamResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemporarySubmissionRepository extends JpaRepository<TemporarySubmission, ExamResultId> {
    List<TemporarySubmission> findByPaperIdAndCourseId(int paperId, int courseId);
    List<TemporarySubmission> findByPaperIdAndCourseIdAndStudentId(int paperId, int courseId, int studentId);
}
