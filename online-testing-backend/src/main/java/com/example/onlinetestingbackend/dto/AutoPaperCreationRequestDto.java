package com.example.onlinetestingbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AutoPaperCreationRequestDto {
    private Integer courseId;
    private String creator;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private List<QuestionTypeConfig> questionTypeConfigs;
    private String paperName;
    private List<ManualPaperQuestionDto> questions;

    public List<ManualPaperQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ManualPaperQuestionDto> questions) {
        this.questions = questions;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    // Getters and Setters
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

    public List<QuestionTypeConfig> getQuestionTypeConfigs() {
        return questionTypeConfigs;
    }

    public void setQuestionTypeConfigs(List<QuestionTypeConfig> questionTypeConfigs) {
        this.questionTypeConfigs = questionTypeConfigs;
    }

    public static class QuestionTypeConfig {
        private String type; // 题型: Single Choice, Multiple Choice, True/False
        private Integer numberOfQuestions; // 题目数量
        private Integer pointsPerQuestion; // 每题分数
        private List<String> tags; // 出题范围标签

        // Getters and Setters

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getNumberOfQuestions() {
            return numberOfQuestions;
        }

        public void setNumberOfQuestions(Integer numberOfQuestions) {
            this.numberOfQuestions = numberOfQuestions;
        }

        public Integer getPointsPerQuestion() {
            return pointsPerQuestion;
        }

        public void setPointsPerQuestion(Integer pointsPerQuestion) {
            this.pointsPerQuestion = pointsPerQuestion;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    // Getter and Setter methods for all fields
}
