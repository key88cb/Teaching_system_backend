// OptionDto.java
package com.example.onlinetestingbackend.dto;

public class OptionDto {
    private Integer optionIdValue; // 对应 OptionId 中的 optionIdValue
    private String optionText;

    // 构造函数 (空构造函数是必须的，使用 Jackson 进行反序列化)
    public OptionDto() {}

    public OptionDto(Integer optionIdValue, String optionText) {
        this.optionIdValue = optionIdValue;
        this.optionText = optionText;
    }

    // Getters and Setters
    public Integer getOptionIdValue() {
        return optionIdValue;
    }

    public void setOptionIdValue(Integer optionIdValue) {
        this.optionIdValue = optionIdValue;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}