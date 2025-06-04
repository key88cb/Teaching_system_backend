package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.id.DetailedResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailedResultRepository extends JpaRepository<DetailedResult, DetailedResultId> {

    // 根据试卷ID和课程ID查询所有详细答题记录
    List<DetailedResult> findByPaperIdAndCourseId(Integer paperId, Integer courseId);

    // 根据试卷ID、课程ID和学生ID查询该学生在该试卷上的所有详细答题记录
    List<DetailedResult> findByPaperIdAndCourseIdAndStudentId(Integer paperId, Integer courseId, Integer studentId);

    // 根据试卷ID、课程ID、学生ID和题目ID查询特定题目的答题记录 (虽然 JpaRepository 提供了 findById)
     DetailedResult findByPaperIdAndCourseIdAndStudentIdAndQuestionId(Integer paperId, Integer courseId, Integer studentId, Integer questionId);

    // 查询特定学生在特定课程中的所有详细答题记录
    List<DetailedResult> findByCourseIdAndStudentId(Integer courseId, Integer studentId);
}
