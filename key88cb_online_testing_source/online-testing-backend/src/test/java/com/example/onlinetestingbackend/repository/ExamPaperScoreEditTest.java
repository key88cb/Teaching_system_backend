package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.dto.ScoreEditDto;
import com.example.onlinetestingbackend.entity.DetailedResult;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@SpringBootTest
public class ExamPaperScoreEditTest {
    @Autowired
    private DetailedResultRepository detailedResultRepository;
    @Autowired
    private ExamQuestionService examQuestionService;
    @Test
    @Transactional
    @Commit
    void EditScore(){
        List<DetailedResult> detailedResults = detailedResultRepository.findAll();
        int tempstudent=-1;
        List<ScoreEditDto> scoreEditDtos = new ArrayList<>();
        for (DetailedResult detailedResult : detailedResults) {
            String studentanswer=detailedResult.getStudentAnswer();
            String answer=detailedResult.getCorrectAnswer();
            if(!studentanswer.equals(answer)) {
                tempstudent = detailedResult.getStudentId();
                ScoreEditDto scoreEditDto = new ScoreEditDto();
                scoreEditDto.setStudentId(tempstudent);
                scoreEditDto.setPaperId(detailedResult.getPaperId());
                scoreEditDto.setQuestionId(detailedResult.getQuestionId());
                scoreEditDto.setCourseId(detailedResult.getCourseId());
                scoreEditDto.setScore(detailedResult.getPoints() + 1);
                scoreEditDtos.add(scoreEditDto);
            }
        }
        examQuestionService.editResultformanystudent(scoreEditDtos);
    }
}
