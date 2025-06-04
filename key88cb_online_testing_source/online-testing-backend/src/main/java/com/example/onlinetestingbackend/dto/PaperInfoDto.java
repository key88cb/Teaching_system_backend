package com.example.onlinetestingbackend.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PaperInfoDto {

    private Integer paperId;
    private Integer courseId;
    private String creator;
    private Integer singleChoiceNum;
    private Integer multipleChoiceNum;
    private Integer trueFalseNum;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private Integer highestScoresForSingleChoice;
    private Integer highestScoresForMultipleChoice;
    private Integer highestScoresForTrueFalse;
    private Integer totalScores;
    private String paperName;

    private List<PaperQuestionDto> paperQuestions = new ArrayList<>();

    // Constructors
    public PaperInfoDto() {}

    // Getters and Setters
    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getSingleChoiceNum() {
        return singleChoiceNum;
    }

    public void setSingleChoiceNum(Integer singleChoiceNum) {
        this.singleChoiceNum = singleChoiceNum;
    }

    public Integer getMultipleChoiceNum() {
        return multipleChoiceNum;
    }

    public void setMultipleChoiceNum(Integer multipleChoiceNum) {
        this.multipleChoiceNum = multipleChoiceNum;
    }

    public Integer getTrueFalseNum() {
        return trueFalseNum;
    }

    public void setTrueFalseNum(Integer trueFalseNum) {
        this.trueFalseNum = trueFalseNum;
    }

    public LocalDateTime getOpenTime() {
        return openTime;
    }

    public void setOpenTime(LocalDateTime openTime) {
        this.openTime = openTime;
    }

    public LocalDateTime getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(LocalDateTime closeTime) {
        this.closeTime = closeTime;
    }

    public Integer getHighestScoresForSingleChoice() {
        return highestScoresForSingleChoice;
    }

    public void setHighestScoresForSingleChoice(Integer highestScoresForSingleChoice) {
        this.highestScoresForSingleChoice = highestScoresForSingleChoice;
    }

    public Integer getHighestScoresForMultipleChoice() {
        return highestScoresForMultipleChoice;
    }

    public void setHighestScoresForMultipleChoice(Integer highestScoresForMultipleChoice) {
        this.highestScoresForMultipleChoice = highestScoresForMultipleChoice;
    }

    public Integer getHighestScoresForTrueFalse() {
        return highestScoresForTrueFalse;
    }

    public void setHighestScoresForTrueFalse(Integer highestScoresForTrueFalse) {
        this.highestScoresForTrueFalse = highestScoresForTrueFalse;
    }

    public Integer getTotalScores() {
        return totalScores;
    }

    public void setTotalScores(Integer totalScores) {
        this.totalScores = totalScores;
    }

    public List<PaperQuestionDto> getPaperQuestions() {
        return paperQuestions;
    }

    public void setPaperQuestions(List<PaperQuestionDto> paperQuestions) {
        this.paperQuestions = paperQuestions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperInfoDto that = (PaperInfoDto) o;
        return Objects.equals(paperId, that.paperId) &&
                Objects.equals(courseId, that.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paperId, courseId);
    }
}
