package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.dto.AutoPaperCreationRequestDto;
import com.example.onlinetestingbackend.dto.ManualPaperCreationRequestDto;
import com.example.onlinetestingbackend.dto.ManualPaperQuestionDto;
import com.example.onlinetestingbackend.dto.QuestionDto;
import com.example.onlinetestingbackend.service.PaperQuestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class PaperInfoInsert {
    @Autowired
    private PaperQuestionService paperQuestionService;

    @Test
    void testPaperInfo() throws Exception {

        // 读入CSV文件并解析为 QuestionDto 对象
        List<ManualPaperCreationRequestDto> ManualpaperInfoDtos = readManualPaperInfoFromCSV();
        System.out.println("✅ Read " + ManualpaperInfoDtos.size() + " papers from CSV.");

        // 插入每个 ManualPaperInfoDto
        for (ManualPaperCreationRequestDto dto : ManualpaperInfoDtos) {
            paperQuestionService.createManualPaper(dto);
        }

        List<AutoPaperCreationRequestDto> AutoPaperInfoDtos = readAutoPaperInfoFromCSV();
        System.out.println("✅ Read " + AutoPaperInfoDtos.size() + " papers from CSV.");

        // 插入每个 AutoPaperInfoDto
        for (AutoPaperCreationRequestDto dto : AutoPaperInfoDtos) {
            paperQuestionService.createAutoPaper(dto);
        }
    }

    private List<ManualPaperCreationRequestDto> readManualPaperInfoFromCSV() throws Exception {
        List<ManualPaperCreationRequestDto> dtos = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream("/Manual_PaperInfoData.csv");
        if (is == null) {
            throw new RuntimeException("CSV 文件未找到，请确认路径是否正确（应放在 src/test/resources 下）");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean headerSkipped = false;

        while ((line = reader.readLine()) != null) {
            if (!headerSkipped) {
                headerSkipped = true;
                continue; // 跳过表头
            }

            String[] values = line.split(",", 13); // 最多分割成12列

            ManualPaperCreationRequestDto dto = new ManualPaperCreationRequestDto();
            dto.setCourseId(Integer.parseInt(values[0]));
            dto.setCreator(values[1]);
            dto.setSingleChoiceNum(Integer.parseInt(values[2]));
            dto.setMultipleChoiceNum(Integer.parseInt(values[3]));
            dto.setTrueFalseNum(Integer.parseInt(values[4]));
            dto.setOpenTime(java.time.LocalDateTime.parse(values[5])); // 格式需为 yyyy-MM-ddTHH:mm:ss
            dto.setCloseTime(java.time.LocalDateTime.parse(values[6]));
            dto.setTotalScores(Integer.parseInt(values[7]));
            dto.setHighestScoresForSingleChoice(Integer.parseInt(values[8]));
            dto.setHighestScoresForMultipleChoice(Integer.parseInt(values[9]));
            dto.setHighestScoresForTrueFalse(Integer.parseInt(values[10]));
            dto.setPaperName(values[11]);

            System.out.println("=== values[12] ===");
            System.out.println(values[12]);

            ObjectMapper objectMapper = new ObjectMapper();

            // 原始字段值
            String questionsField = values[12].trim();

            // 1. 去除最外层引号（如果有的话）
            String jsonContent = questionsField;
            if (jsonContent.startsWith("\"") && jsonContent.endsWith("\"")) {
                jsonContent = jsonContent.substring(1, jsonContent.length() - 1);
            }

            // 2. 替换双重引号 "" -> " （应对 CSV 中的 JSON 字符串）
            jsonContent = jsonContent.replace("\"\"", "\"");

            // 3. 使用正则提取所有 { ... } 包裹的 JSON 对象
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{.*?\\}");
            java.util.regex.Matcher matcher = pattern.matcher(jsonContent);

            List<ManualPaperQuestionDto> questions = new ArrayList<>();

            while (matcher.find()) {
                String objStr = matcher.group();
                try {
                    ManualPaperQuestionDto q = objectMapper.readValue(objStr, ManualPaperQuestionDto.class);
                    questions.add(q);
                } catch (Exception e) {
                    System.out.println("⚠️ 解析单个 question 失败: " + objStr);
                    e.printStackTrace();
                }
            }

            dto.setQuestions(questions); // 设置问题列表
            dtos.add(dto);
        }

        return dtos;
    }




    private List<AutoPaperCreationRequestDto> readAutoPaperInfoFromCSV() throws Exception {
        List<AutoPaperCreationRequestDto> dtos = new ArrayList<>();
        InputStream is = getClass().getResourceAsStream("/Auto_PaperInfoData.csv");
        if (is == null) {
            throw new RuntimeException("CSV 文件未找到，请确认路径是否正确（应放在 src/test/resources 下）");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean headerSkipped = false;
        while ((line = reader.readLine()) != null) {
            if (!headerSkipped) {
                headerSkipped = true;
                continue; // 跳过表头
            }
            String[] values = line.split(",", -1);
            AutoPaperCreationRequestDto dto = new AutoPaperCreationRequestDto();
            dto.setCourseId(Integer.parseInt(values[0]));
            dto.setCreator(values[1]);
            dto.setOpenTime(java.time.LocalDateTime.parse(values[2]));
            dto.setCloseTime(java.time.LocalDateTime.parse(values[3]));
            dto.setPaperName(values[10]);

            List<AutoPaperCreationRequestDto.QuestionTypeConfig> configs = new ArrayList<>();
            // 单选题
            AutoPaperCreationRequestDto.QuestionTypeConfig single = new AutoPaperCreationRequestDto.QuestionTypeConfig();
            single.setType("Single Choice");
            single.setNumberOfQuestions(Integer.parseInt(values[4]));
            single.setPointsPerQuestion(Integer.parseInt(values[5]));
            single.setTags(null);
            configs.add(single);
            // 多选题
            AutoPaperCreationRequestDto.QuestionTypeConfig multi = new AutoPaperCreationRequestDto.QuestionTypeConfig();
            multi.setType("Multiple Choice");
            multi.setNumberOfQuestions(Integer.parseInt(values[6]));
            multi.setPointsPerQuestion(Integer.parseInt(values[7]));
            multi.setTags(null);
            configs.add(multi);
            // 判断题
            AutoPaperCreationRequestDto.QuestionTypeConfig tf = new AutoPaperCreationRequestDto.QuestionTypeConfig();
            tf.setType("True/False");
            tf.setNumberOfQuestions(Integer.parseInt(values[8]));
            tf.setPointsPerQuestion(Integer.parseInt(values[9]));
            tf.setTags(null);
            configs.add(tf);

            dto.setQuestionTypeConfigs(configs);
            dtos.add(dto);
        }
        return dtos;
    }
}
