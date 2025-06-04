package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.entity.id.ExamResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, ExamResultId> {

    // 根据试卷ID和课程ID查询所有学生的考试结果
    List<ExamResult> findByPaperIdAndCourseId(Integer paperId, Integer courseId);

    // 根据学生ID查询该学生的所有考试结果
    List<ExamResult> findByStudentId(Integer studentId);

    // 根据试卷ID、课程ID和学生ID查询特定考试结果 (虽然 JpaRepository 提供了 findById)
    Optional<ExamResult> findByPaperIdAndCourseIdAndStudentId(Integer paperId, Integer courseId, Integer studentId);

    // 查询特定课程中特定学生的所有考试结果
    List<ExamResult> findByCourseIdAndStudentId(Integer courseId, Integer studentId);
}