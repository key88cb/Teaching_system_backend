package com.example.onlinetestingbackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ExamresultDto {
    private Integer studentId;
    private Integer totalScore;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
