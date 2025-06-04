package com.example.onlinetestingbackend.dto;

import com.example.onlinetestingbackend.entity.DetailedResult;

import java.util.List;

public class RecordsDto {
    private Integer courseId;
    private Integer paperId;
    private List<AnalyseDto> analyses;
    public RecordsDto(Integer courseId, Integer paperId, List<AnalyseDto> analyses) {}
    public RecordsDto() {  }
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

    public List<AnalyseDto> getAnalyses() {
        return analyses;
    }

    public void setAnalyses(List<AnalyseDto> analyses) {
        this.analyses = analyses;
    }
}
