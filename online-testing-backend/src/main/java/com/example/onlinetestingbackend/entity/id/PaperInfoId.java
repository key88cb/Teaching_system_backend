package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PaperInfoId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "paper_id")
    // Will be INT NOT NULL in generated DDL
    private Integer paperId;

    @Column(name = "course_id")
    private Integer courseId;

    public PaperInfoId() {}
    public PaperInfoId(Integer paperId, Integer courseId) { this.paperId = paperId; this.courseId = courseId; }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PaperInfoId that = (PaperInfoId) o; return Objects.equals(paperId, that.paperId) && Objects.equals(courseId, that.courseId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId); }
}