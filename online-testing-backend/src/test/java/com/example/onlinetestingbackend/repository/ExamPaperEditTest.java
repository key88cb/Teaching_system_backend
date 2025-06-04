package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.dto.ExamPlainRecordDto;
import com.example.onlinetestingbackend.dto.PlainAnswerDto;
import com.example.onlinetestingbackend.entity.ExamResult;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.entity.PaperQuestion;
import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.service.ExamQuestionService;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ExamPaperEditTest {
    @Autowired
    private PaperQuestionService paperQuestionService;
    @Autowired
    private PaperInfoRepository paperInfoRepository;
    @Autowired
    private ExamQuestionService examQuestionService;
    @Test
    @Transactional
    @Commit
    void testForExam(){
        List<PaperInfo> paperInfoList = paperInfoRepository.findAll();
        for (PaperInfo paperInfo : paperInfoList) {
            List<PaperQuestion> paperQuestions=paperInfo.getPaperQuestions();
            int paperId=paperInfo.getPaperId();
            int courseId=paperInfo.getCourseId();
            for(int j=0;j<10;j++) {
                int studentId=j+1;
                ExamPlainRecordDto examPlainRecordDto = new ExamPlainRecordDto();
                examPlainRecordDto.setPaperId(paperId);
                examPlainRecordDto.setCourseId(courseId);
                List<PlainAnswerDto> plainAnswerDtoList = new ArrayList<>();
                for (PaperQuestion paperQuestion : paperQuestions) {

                    String type = paperQuestion.getQuestionType();
                    String answer = "";
                    if (type.equals("Single Choice")) {
                        int randomIndex = (int) (Math.random() * 4);
                        if (randomIndex < 1) {
                            answer = "A";
                        } else if (randomIndex < 2) {
                            answer = "B";
                        } else if (randomIndex < 3) {
                            answer = "C";
                        } else {
                            answer = "D";
                        }
                    } else if (type.equals("Multiple Choice")) {
                        for (int i = 0; i < 4; i++) {
                            char c = (char) ('A' + i); // 正确写法：强制转换为 char 类型
                            Boolean randomIndex = (Math.random() <= 0.5);
                            if (randomIndex) {
                                answer += c;
                            }
                        }
                    } else {
                        Boolean randomIndex = (Math.random() <= 0.5);
                        if (randomIndex) {
                            answer = "A";
                        } else {
                            answer = "B";
                        }
                    }
                    PlainAnswerDto plainAnswerDto = new PlainAnswerDto();
                    plainAnswerDto.setAnswer(answer);
                    plainAnswerDto.setQuestionId(paperQuestion.getQuestionId());
                    plainAnswerDtoList.add(plainAnswerDto);
                }
                Random random = new Random();
                examPlainRecordDto.setStartTime(paperInfo.getOpenTime().plusMinutes(random.nextInt(6)));
                examPlainRecordDto.setFinishTime(paperInfo.getCloseTime().minusMinutes(random.nextInt(6)));
                examPlainRecordDto.setStudentId(studentId);
                examPlainRecordDto.setAnswers(plainAnswerDtoList);
                try {
                    examQuestionService.judgeResult(examPlainRecordDto);
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }

        }
    }
}
