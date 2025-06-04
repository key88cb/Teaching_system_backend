package com.example.onlinetestingbackend.dto;

public class ManualPaperQuestionDto {
    private Integer questionId;
    private Integer points;

    public ManualPaperQuestionDto() {}

    public ManualPaperQuestionDto(Integer questionId, Integer points) {
        this.questionId = questionId;
        this.points = points;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
