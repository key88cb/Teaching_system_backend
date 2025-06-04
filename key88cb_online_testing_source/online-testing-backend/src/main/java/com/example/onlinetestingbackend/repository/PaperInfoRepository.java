package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface PaperInfoRepository extends JpaRepository<PaperInfo, PaperInfoId> {

    // 根据课程ID查询试卷信息
    List<PaperInfo> findByCourseId(Integer courseId);

    // 根据创建者查询试卷信息
    List<PaperInfo> findByCreator(String creator);

    // 根据开放时间范围查询试卷
    List<PaperInfo> findByOpenTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据课程ID和创建者查询
    List<PaperInfo> findByCourseIdAndCreator(Integer courseId, String creator);

    PaperInfo findByCourseIdAndPaperId(Integer courseId, Integer paperId);

    void deleteById(PaperInfoId id);

    // 查询未开始的考试（openTime 在当前时间之后）
    List<PaperInfo> findByOpenTimeAfter(java.time.LocalDateTime now);

    // 查询正在进行的考试（openTime 早于当前时间，closeTime 晚于当前时间）
    List<PaperInfo> findByOpenTimeBeforeAndCloseTimeAfter(java.time.LocalDateTime now1, java.time.LocalDateTime now2);

    // 查询已结束的考试（closeTime 早于当前时间）
    List<PaperInfo> findByCloseTimeBefore(java.time.LocalDateTime now);
}