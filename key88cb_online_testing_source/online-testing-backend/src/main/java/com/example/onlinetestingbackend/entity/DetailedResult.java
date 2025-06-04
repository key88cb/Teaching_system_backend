package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.DetailedResultId;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Detailed_result")
@IdClass(DetailedResultId.class)
public class DetailedResult {

    @Id @Column(name = "paper_id") private Integer paperId;
    @Id @Column(name = "course_id") private Integer courseId;
    @Id @Column(name = "student_id") private Integer studentId;
    @Id @Column(name = "question_id") private Integer questionId;

    @Column(name = "correct_answer", nullable = false, length = 4)
    private String correctAnswer;

    @Column(name = "student_answer", nullable = false, length = 4)
    private String studentAnswer;

    @Column(name = "points", nullable = false) // DDL: int(3)
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "paper_id", referencedColumnName = "paper_id", insertable = false, updatable = false),
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_detailedresult_paperinfo"))
    private PaperInfo paperInfo;

    // Constructors, Getters, Setters, equals, hashCode
    public DetailedResult() {}
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String ca) { this.correctAnswer = ca; }
    public String getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(String sa) { this.studentAnswer = sa; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public PaperInfo getPaperInfo() { return paperInfo; }
    public void setPaperInfo(PaperInfo pi) { this.paperInfo = pi; if (pi != null) { this.paperId = pi.getPaperId(); this.courseId = pi.getCourseId(); }}
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; DetailedResult dr = (DetailedResult) o; return Objects.equals(paperId, dr.paperId) && Objects.equals(courseId, dr.courseId) && Objects.equals(studentId, dr.studentId) && Objects.equals(questionId, dr.questionId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, studentId, questionId); }
}
