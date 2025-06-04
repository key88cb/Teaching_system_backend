package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DetailedResultId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "paper_id") private Integer paperId;
    @Column(name = "course_id") private Integer courseId;
    @Column(name = "student_id") private Integer studentId; // DDL: int(10)
    @Column(name = "question_id") private Integer questionId;

    public DetailedResultId() {}
    public DetailedResultId(Integer pId, Integer cId, Integer sId, Integer qId) { this.paperId = pId; this.courseId = cId; this.studentId = sId; this.questionId = qId; }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; DetailedResultId that = (DetailedResultId) o; return Objects.equals(paperId, that.paperId) && Objects.equals(courseId, that.courseId) && Objects.equals(studentId, that.studentId) && Objects.equals(questionId, that.questionId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, studentId, questionId); }
}