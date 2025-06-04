package com.example.onlinetestingbackend.dto;

import java.util.List;

public class AnalyseDto {
    private Integer questionId;
    private Float avgscore;
    private List<Integer> counts;

    public Float getAvgscore() {
        return avgscore;
    }

    public void setAvgscore(Float avgscore) {
        this.avgscore = avgscore;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public void setCounts(List<Integer> counts) {
        this.counts = counts;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
}
