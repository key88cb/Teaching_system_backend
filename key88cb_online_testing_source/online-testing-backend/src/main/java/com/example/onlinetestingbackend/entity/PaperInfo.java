package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Paper_info", uniqueConstraints = {@UniqueConstraint(columnNames = {"paper_id", "course_id"})})
@IdClass(PaperInfoId.class)
public class PaperInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paper_id")
    // 这里可能会出现一些问题 因为 卷子信息的主键是复合的 自动生成的sql语句 无法对自动生成的主键成员加一 也就是paper_id 可能无法自增
    private Integer paperId;

    @Id
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "creator", nullable = false, length = 20)
    private String creator;

    @Column(name = "single_choice_num") // int(3) display width not specified
    private Integer singleChoiceNum;

    @Column(name = "multiple_choice_num")
    private Integer multipleChoiceNum;

    @Column(name = "true_false_num")
    private Integer trueFalseNum;

    @Column(name = "open_time", columnDefinition = "datetime(0)")
    private LocalDateTime openTime;

    @Column(name = "close_time", columnDefinition = "datetime(0)")
    private LocalDateTime closeTime;

    @Column(name = "highest_scores_for_single_choice")
    private Integer highestScoresForSingleChoice;

    @Column(name = "highest_scores_for_multiple_choice")
    private Integer highestScoresForMultipleChoice;

    @Column(name = "highest_scores_for_true_false")
    private Integer highestScoresForTrueFalse;

    @Column(name = "total_scores")
    private Integer totalScores;

    @Column(name = "paper_name", nullable = false, length = 256)
    private String paperName;

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PaperQuestion> paperQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExamResult> examResults = new ArrayList<>();

    @OneToMany(mappedBy = "paperInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetailedResult> detailedResults = new ArrayList<>();

    // Constructors, Getters, Setters, equals, hashCode
    public PaperInfo() {}
    // Getter and Setter
    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public Integer getSingleChoiceNum() { return singleChoiceNum; }
    public void setSingleChoiceNum(Integer singleChoiceNum) { this.singleChoiceNum = singleChoiceNum; }
    public Integer getMultipleChoiceNum() { return multipleChoiceNum; }
    public void setMultipleChoiceNum(Integer multipleChoiceNum) { this.multipleChoiceNum = multipleChoiceNum; }
    public Integer getTrueFalseNum() { return trueFalseNum; }
    public void setTrueFalseNum(Integer trueFalseNum) { this.trueFalseNum = trueFalseNum; }
    public LocalDateTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalDateTime openTime) { this.openTime = openTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public Integer getHighestScoresForSingleChoice() { return highestScoresForSingleChoice; }
    public void setHighestScoresForSingleChoice(Integer h) { this.highestScoresForSingleChoice = h; }
    public Integer getHighestScoresForMultipleChoice() { return highestScoresForMultipleChoice; }
    public void setHighestScoresForMultipleChoice(Integer h) { this.highestScoresForMultipleChoice = h; }
    public Integer getHighestScoresForTrueFalse() { return highestScoresForTrueFalse; }
    public void setHighestScoresForTrueFalse(Integer h) { this.highestScoresForTrueFalse = h; }
    public Integer getTotalScores() { return totalScores; }
    public void setTotalScores(Integer totalScores) { this.totalScores = totalScores; }
    public List<PaperQuestion> getPaperQuestions() { return paperQuestions; }
    public void setPaperQuestions(List<PaperQuestion> paperQuestions) { this.paperQuestions = paperQuestions; }
    public List<ExamResult> getExamResults() { return examResults; }
    public void setExamResults(List<ExamResult> examResults) { this.examResults = examResults; }
    public List<DetailedResult> getDetailedResults() { return detailedResults; }
    public void setDetailedResults(List<DetailedResult> detailedResults) { this.detailedResults = detailedResults; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; PaperInfo pi = (PaperInfo) o; return Objects.equals(paperId, pi.paperId) && Objects.equals(courseId, pi.courseId); }
    @Override public int hashCode() { return Objects.hash(paperId, courseId); }
}