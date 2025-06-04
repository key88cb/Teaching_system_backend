package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.ExamResultId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "temporary_submission", uniqueConstraints = {@UniqueConstraint(columnNames = {"paper_id", "course_id","student_id"})})
@IdClass(ExamResultId.class)
public class TemporarySubmission {
    // 格式: paperId:courseId:studentId
    @Id
    @Column(name = "paper_id")
    private Integer paperId;

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Id
    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "submission_time")
    private LocalDateTime submissionTime;

    // JSON or BLOB 字段存储答案列表
    @Column(name = "answers", columnDefinition = "TEXT")
    private String answersJson; // 可以使用 Jackson 序列化 List<PlainAnswerDto>

    public int getPaperId() { return paperId; }
    public void setPaperId(int paperId) { this.paperId = paperId; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public void setSubmissionTime(LocalDateTime submissionTime) { this.submissionTime = submissionTime; }

    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public ExamResultId getId(){
        return new ExamResultId(paperId,courseId,studentId);
    }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, studentId); }
}