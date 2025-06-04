package com.example.onlinetestingbackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExamPlainRecordDto {
    private Integer paperId;
    private Integer courseId;
    private Integer studentId;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private List<PlainAnswerDto> answers=new ArrayList<PlainAnswerDto>();
    public ExamPlainRecordDto() {}
    public ExamPlainRecordDto(Integer paperId, Integer courseId, List<PlainAnswerDto> answers) {
        this.paperId = paperId;
        this.courseId = courseId;
        this.answers = answers;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
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

    public List<PlainAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<PlainAnswerDto> answers) {
        this.answers = answers;
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
