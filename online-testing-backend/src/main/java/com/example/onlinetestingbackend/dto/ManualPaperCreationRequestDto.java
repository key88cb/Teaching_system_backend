package com.example.onlinetestingbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ManualPaperCreationRequestDto {
    private Integer courseId;
    private String creator;
    private Integer singleChoiceNum;
    private Integer multipleChoiceNum;
    private Integer trueFalseNum;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private Integer totalScores;
    private Integer highestScoresForSingleChoice;
    private Integer highestScoresForMultipleChoice;
    private Integer highestScoresForTrueFalse;
    private List<ManualPaperQuestionDto> questions;
    private String paperName;

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
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

    public Integer getTotalScores() {
        return totalScores;
    }

    public void setTotalScores(Integer totalScores) {
        this.totalScores = totalScores;
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

    public List<ManualPaperQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ManualPaperQuestionDto> questions) {
        this.questions = questions;
    }
}
