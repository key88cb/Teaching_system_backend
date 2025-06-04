package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.PaperQuestionId;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Paper_questions")
@IdClass(PaperQuestionId.class)
public class PaperQuestion {

    @Id @Column(name = "paper_id") private Integer paperId;
    @Id @Column(name = "course_id") private Integer courseId;
    @Id @Column(name = "question_id") private Integer questionId;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "knowledge_points", length = 256)
    private String knowledgePoints;
    @Column(name = "question_type", nullable = false, length = 20)
    private String questionType;
    @Column(name = "A", nullable = false, length = 256)
    private String optionA;

    @Column(name = "B", nullable = false, length = 256)
    private String optionB;

    @Column(name = "C", length = 256)
    private String optionC;

    @Column(name = "D", length = 256)
    private String optionD;

    @Column(name = "question_text", nullable = false, length = 256)
    private String questionText;

    @Column(name = "correct_answer", nullable = false, length = 4)
    private String correctAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(value = {
            @JoinColumn(name = "paper_id", referencedColumnName = "paper_id", insertable = false, updatable = false),
            @JoinColumn(name = "course_id", referencedColumnName = "course_id", insertable = false, updatable = false)
    }, foreignKey = @ForeignKey(name = "fk_paperquestions_paperinfo"))

    private PaperInfo paperInfo;

    // Constructors, Getters, Setters, equals, hashCode (similar to previous version)
    public PaperQuestion() {}
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(String k) { this.knowledgePoints = k; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public PaperInfo getPaperInfo() { return paperInfo; }
    public void setPaperInfo(PaperInfo pi) { this.paperInfo = pi; if (pi != null) { this.paperId = pi.getPaperId(); this.courseId = pi.getCourseId(); }}
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PaperQuestion pq = (PaperQuestion) o; return Objects.equals(paperId, pq.paperId) && Objects.equals(courseId, pq.courseId) && Objects.equals(questionId, pq.questionId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId, questionId); }
    public String getQuestionType() {
        return questionType;
    }
    public void setQuestionType(String questiontype) {
        this.questionType = questiontype;
    }
}