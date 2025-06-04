package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExamResultId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "paper_id") private Integer paperId;
    @Column(name = "course_id") private Integer courseId;
    @Column(name = "student_id") private Integer studentId; // DDL: int(10)

    public ExamResultId() {}
    public ExamResultId(Integer pId, Integer cId, Integer sId) { this.paperId = pId; this.courseId = cId; this.studentId = sId; }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; ExamResultId that = (ExamResultId) o; return Objects.equals(paperId, that.paperId) && Objects.equals(courseId, that.courseId) && Objects.equals(studentId, that.studentId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, studentId); }
}