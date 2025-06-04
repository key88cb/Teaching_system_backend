package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.id.PaperQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaperQuestionRepository extends JpaRepository<PaperQuestion, PaperQuestionId> {

    // 根据试卷ID (PaperInfoId 的一部分) 查询所有试卷题目
    // 注意：PaperQuestionId 包含 paperId 和 courseId，所以查询通常基于这两个
    List<PaperQuestion> findByPaperIdAndCourseId(Integer paperId, Integer courseId);

    // 根据试卷ID、课程ID和题目ID查询特定题目 (虽然 JpaRepository 提供了 findById)
     PaperQuestion findByPaperIdAndCourseIdAndQuestionId(Integer paperId, Integer courseId, Integer questionId);

    boolean existsByPaperIdAndCourseIdAndQuestionId(Integer paperId, Integer courseId, Integer questionId);

    // 根据知识点模糊查询
    List<PaperQuestion> findByKnowledgePointsContainingAndPaperIdAndCourseId(String keyword, Integer paperId, Integer courseId);

    void deleteByPaperIdAndCourseId(Integer paperId, Integer courseId);
}