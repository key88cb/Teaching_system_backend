package com.example.onlinetestingbackend.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "question_id", length = 8)
    private Integer questionId; // This will be mapped from Question entity

    @Column(name = "option_id", length = 8)
    private Integer optionIdValue; // Renamed to avoid conflict with a potential 'id' field in Option entity

    // Constructors
    public OptionId() {
    }

    public OptionId(Integer questionId, Integer optionIdValue) {
//        this.questionId = questionId;
        this.optionIdValue = optionIdValue;
    }
    // Getters and Setters
    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getOptionIdValue() {
        return optionIdValue;
    }

    public void setOptionIdValue(Integer optionIdValue) {
        this.optionIdValue = optionIdValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionId optionId = (OptionId) o;
        return Objects.equals(questionId, optionId.questionId) &&
                Objects.equals(optionIdValue, optionId.optionIdValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, optionIdValue);
    }
}