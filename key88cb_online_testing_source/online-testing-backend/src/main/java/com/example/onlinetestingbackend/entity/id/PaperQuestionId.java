package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PaperQuestionId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "paper_id")
    private Integer paperId;

    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "question_id")
    private Integer questionId;
    // getter setter 自动生成的
    public PaperQuestionId() {}
    public PaperQuestionId(Integer paperId, Integer courseId, Integer questionId) { this.paperId = paperId; this.courseId = courseId; this.questionId = questionId; }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PaperQuestionId that = (PaperQuestionId) o; return Objects.equals(paperId, that.paperId) && Objects.equals(courseId, that.courseId) && Objects.equals(questionId, that.questionId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, questionId); }
}