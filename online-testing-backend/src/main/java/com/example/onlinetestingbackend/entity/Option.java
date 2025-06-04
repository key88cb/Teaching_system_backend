package com.example.onlinetestingbackend.entity;

import com.example.onlinetestingbackend.entity.id.OptionId;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Options_table")
public class Option {

    @EmbeddedId
    private OptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId") // Maps to OptionId.questionId
    @JoinColumn(name = "question_id", referencedColumnName = "question_id", insertable = false, updatable = false)
    private Question question;

    @Column(name = "option_text", nullable = false, length = 256)
    private String optionText;

    // Constructors
    public Option() {
    }

    // Getters and Setters
    public OptionId getId() {
        return id;
    }

    public void setId(OptionId id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    // Convenience getter for option_id value from the composite ID
    public Integer getOptionIdValue() {
        return this.id != null ? this.id.getOptionIdValue() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return Objects.equals(id, option.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}