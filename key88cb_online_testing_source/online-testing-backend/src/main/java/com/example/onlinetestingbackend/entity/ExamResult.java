package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.ExamResultId;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Exam_result")
@IdClass(ExamResultId.class)
public class ExamResult {

    @Id @Column(name = "paper_id") private Integer paperId;
    @Id @Column(name = "course_id") private Integer courseId;
    @Id @Column(name = "student_id") private Integer studentId;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;
    @Column(name="start_time")
    private LocalDateTime startTime;
    @Column(name="finish_time")
    private LocalDateTime finishTime;

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "paper_id", referencedColumnName = "paper_id", insertable = false, updatable = false),
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_examresult_paperinfo"))
    private PaperInfo paperInfo;

    // Constructors, Getters, Setters, equals, hashCode (similar to previous version)
    public ExamResult() {}
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }
    public PaperInfo getPaperInfo() { return paperInfo; }
    public void setPaperInfo(PaperInfo pi) { this.paperInfo = pi; if (pi != null) { this.paperId = pi.getPaperId(); this.courseId = pi.getCourseId(); }}
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; ExamResult er = (ExamResult) o; return Objects.equals(paperId, er.paperId) && Objects.equals(courseId, er.courseId) && Objects.equals(studentId, er.studentId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, studentId); }
}