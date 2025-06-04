package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.service.QuestionService;
import com.example.onlinetestingbackend.dto.QuestionDto;
import com.example.onlinetestingbackend.dto.OptionDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class QuestionInsert {
    @Autowired
    private QuestionService questionService;

    @Test
    void testQuestions() throws Exception {

        // 读入CSV文件并解析为 QuestionDto 对象
        List<QuestionDto> questionDtos = readQuestionDtosFromCSV();
        System.out.println("✅ Read " + questionDtos.size() + " questions from CSV.");

        // 插入每个 QuestionDto
        for (QuestionDto dto : questionDtos) {
            questionService.createQuestion(dto);
        }
    }

    private List<QuestionDto> readQuestionDtosFromCSV() throws Exception {
        List<QuestionDto> dtos = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream("/QuestionData.csv");

        if (is == null) {
            throw new RuntimeException("CSV 文件未找到，请确认路径是否正确（应放在 src/test/resources 下）");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean headerSkipped = false;

        while ((line = reader.readLine()) != null) {
            if (!headerSkipped) {
                headerSkipped = true;
                continue; // skip header
            }

            String[] values = line.split(",",-1);

            QuestionDto dto = new QuestionDto();
            dto.setSubjectCategory(values[0]);
            dto.setTags(values[1]);
            dto.setQuestionText(values[2]);
            dto.setQuestionType(values[3]);
            dto.setCorrectAnswer(values[4]);
            dto.setCreator(values[5]);

            // 添加选项 A - D
            List<OptionDto> optionDtos = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                OptionDto optionDto = new OptionDto();
                optionDto.setOptionIdValue(i + 1);
                optionDto.setOptionText(values[6 + i]);
                optionDtos.add(optionDto);
            }
            dto.setOptions(optionDtos);

            dtos.add(dto);
        }

        return dtos;
    }
}
