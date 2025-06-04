package com.example.onlinetestingbackend.dto;

public class QueryPaperRequestDto {
    private Integer courseId;
    private String creator;

    // Getter 和 Setter 方法
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
}