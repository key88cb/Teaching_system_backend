package com.example.onlinetestingbackend.dto;

import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.ExamResult;

import java.util.List;

public class RecordDto {
    private Integer courseId;
    private Integer studentId;
    private Integer paperId;
    private List<DetailedResultDto> DetailedResults;
    public RecordDto() {}
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

    public List<DetailedResultDto> getDetailedResults() {
        return DetailedResults;
    }

    public void setDetailedResults(List<DetailedResultDto> detailedResults) {
        DetailedResults = detailedResults;
    }
}
