// src/main/java/com/example/onlinetestingbackend/dto/UpdatePaperTimeRequestDto.java
package com.example.onlinetestingbackend.dto;

import java.time.LocalDateTime;

public class UpdatePaperTimeRequestDto {
    private Integer paperId;
    private Integer courseId;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private String paperName;

    // getter 和 setter
    public Integer getPaperId() { return paperId; }
    public void setPaperId(Integer paperId) { this.paperId = paperId; }

    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }

    public LocalDateTime getOpenTime() { return openTime; }
    public void setOpenTime(LocalDateTime openTime) { this.openTime = openTime; }

    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }
}
