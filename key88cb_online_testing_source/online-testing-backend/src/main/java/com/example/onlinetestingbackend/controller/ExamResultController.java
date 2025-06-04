package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.*;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "http://localhost:5173")
public class ExamResultController {
    @Autowired
    private ExamQuestionService examQuestionService;
    @PostMapping("/submit-exam")
    public ResponseEntity<String> submitExam(@RequestBody ExamPlainRecordDto examResult) {
        examQuestionService.judgeResult(examResult);
        return ResponseEntity.ok("Exam Submitted");
    }
    @GetMapping("/search-exam-for-all")
    public ResponseEntity<RecordsDto> getExamResultsForClass(@RequestParam Integer courseId, @RequestParam Integer paperId) {
        RecordsDto recordDto=examQuestionService.searchbycourseIdandpaperId(courseId,paperId);
        return ResponseEntity.ok(recordDto);
    }
    @GetMapping("search-exam-for-one")
    public ResponseEntity<RecordDto> getExamResults(@RequestParam Integer courseId, @RequestParam Integer paperId,@RequestParam Integer studentId) {
        RecordDto recordDto=examQuestionService.searchbycourseIdandpaperIdandstudentId(courseId,paperId,studentId);
        return ResponseEntity.ok(recordDto);
    }
    @PostMapping("/edit-score")
    public ResponseEntity<String> editScore(@RequestBody ScoreEditDto dto) {
        try {
            examQuestionService.editResultforonestudent(dto);
            return ResponseEntity.ok("成绩已更新");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("修改失败：" + e.getMessage());
        }
    }
    @GetMapping("search-examResult-for-one-student")
    public  ResponseEntity<List<ExamwithNameDto>> getExamResultForOne(@RequestParam Integer studentId) {
        List<ExamwithNameDto> body=examQuestionService.searchexamresultbystudentId(studentId);
        return ResponseEntity.ok(body);
    }
    @GetMapping("search-examResult-for-all")
    public  ResponseEntity<List<ExamresultDto>> getAbstractExamResult(@RequestParam Integer paperId, @RequestParam Integer courseId){
        List<ExamresultDto> body=examQuestionService.searchexamresultbybycourseIdandpaperId(courseId,paperId);
        return ResponseEntity.ok(body);
    }
    @GetMapping("search-examResult-for-one")
    public  ResponseEntity<ExamresultDto> getAbstractExamResultForOne(@RequestParam Integer paperId, @RequestParam Integer courseId,@RequestParam Integer studentId){
        ExamresultDto body=examQuestionService.searchexamresultbybycourseIdandpaperIdandstudentId(courseId,paperId,studentId);
        return ResponseEntity.ok(body);
    }
}
