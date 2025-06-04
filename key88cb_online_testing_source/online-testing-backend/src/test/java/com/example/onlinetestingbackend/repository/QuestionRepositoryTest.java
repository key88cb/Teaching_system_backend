package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Question;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class QuestionRepositoryTest {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void testQuestions() throws Exception {

        // 测试插入
        questionRepository.deleteAllInBatch();
        // 读入CSV文件并解析为 QuestionDto 对象
        List<QuestionDto> questionDtos = readQuestionDtosFromCSV();
        System.out.println("✅ Read " + questionDtos.size() + " questions from CSV.");

        // 插入每个 QuestionDto
        for (QuestionDto dto : questionDtos) {
            questionService.createQuestion(dto);
        }

        // 测试查询
        // 查询所有题目以验证插入结果
        List<QuestionDto> savedDtos = questionService.getAllQuestions();
        System.out.println("✅ 成功插入" + savedDtos.size() + " 条题目");

        // 1. 按科目类别查询：Math
        List<Question> mathQuestions = questionRepository.findBySubjectCategory("Math");
        assertThat(mathQuestions).hasSize(6); // CSV中有两个 Math 题目

        // 2. 按题目类型查询：Single Choice
        List<Question> singleChoiceQuestions = questionRepository.findByQuestionType("Single Choice");
        assertThat(singleChoiceQuestions).hasSize(20); // 所有题目都是 Single Choice

        // 3. 按创建者查询：Teacher1
        List<Question> teacher1Questions = questionRepository.findByCreator("Teacher1");
        assertThat(teacher1Questions).hasSize(3); // Teacher1 创建了一个问题

        // 4. 按标签模糊查询
        List<Question> forceTaggedQuestions = questionRepository.findByTagsContaining("Force");
        assertThat(forceTaggedQuestions).hasSize(3); // 标签为 Force 的题目只有一个

        // 5. 按题干内容模糊查询："gravity"
        List<Question> gravityTextQuestions = questionRepository.findByQuestionTextContaining("gravity");
        assertThat(gravityTextQuestions).hasSize(2); // 含 "gravity" 的题目一个

        // 6. 自定义查询：按科目类别和题目类型查询
        List<Question> physicsSingleChoice = questionRepository.findBySubjectCategoryAndQuestionType("Physics", "Single Choice");
        assertThat(physicsSingleChoice).hasSize(2); // Physics 类别下有两个单选题

        // 7. 统计特定科目类别的题目数量：History
        long historyCount = questionRepository.countBySubjectCategory("History");
        assertThat(historyCount).isEqualTo(6); // History 类别有两个题目

        // 8. 检查是否存在特定创建者的题目：Teacher10
        boolean existsTeacher10 = questionRepository.existsByCreator("Teacher10");
        assertThat(existsTeacher10).isTrue(); // Teacher10 存在

        // 测试删除
        // 查询删除前所有题目数量
        System.out.println("✅ 删除前共" + savedDtos.size() + " 条题目");

        // 删除id = 1,5,10,15,20的题目
        System.out.println("✅ 将要删除" + 5 + " 条题目");
        questionService.deleteQuestion(1);
        questionService.deleteQuestion(5);
        questionService.deleteQuestion(10);
        questionService.deleteQuestion(15);
        questionService.deleteQuestion(20);

        // 查询删除前所有题目数量
        savedDtos = questionService.getAllQuestions();
        System.out.println("✅ 删除后共" + savedDtos.size() + " 条题目");

        // 测试更新
        // 更新id = 3,4,6,7,8,9的题目与3相同
        QuestionDto questionDto;
        questionDto = questionDtos.get(2);// 数组下标
        //questionService.updateQuestion(3,questionDto);
        questionService.updateQuestion(4,questionDto);
        questionService.updateQuestion(6,questionDto);
        questionService.updateQuestion(7,questionDto);
        questionService.updateQuestion(8,questionDto);
        questionService.updateQuestion(9,questionDto);
        System.out.println("✅ 更新" + 6 + " 条题目");
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
