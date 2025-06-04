package com.example.onlinetestingbackend.service;

import com.example.onlinetestingbackend.dto.OptionDto;
import com.example.onlinetestingbackend.dto.QuestionDto;
import com.example.onlinetestingbackend.entity.Option;
import com.example.onlinetestingbackend.entity.Question;
import com.example.onlinetestingbackend.entity.id.OptionId;
import com.example.onlinetestingbackend.repository.OptionRepository;
import com.example.onlinetestingbackend.repository.QuestionRepository;
import com.example.onlinetestingbackend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils; // For checking if collection is empty

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository, OptionRepository optionRepository) {
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
    }
    private QuestionDto convertToDto(Question question) {
        if(question == null) return null;
        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(question.getQuestionId());
        dto.setSubjectCategory(question.getSubjectCategory());
        dto.setTags(question.getTags());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setCorrectAnswer(question.getCorrectAnswer());
        dto.setCreator(question.getCreator());
        if (question.getOptions() != null) {
            dto.setOptions(question.getOptions().stream()
                    .map(this::convertOptionToDto)
                    .collect(Collectors.toList()));
        }
        return dto;    }
    private OptionDto convertOptionToDto(Option option) {
        if (option == null || option.getId() == null) {
            return null;
        }
        OptionDto dto = new OptionDto();
        dto.setOptionIdValue(option.getId().getOptionIdValue()); // 从 OptionId 获取
        dto.setOptionText(option.getOptionText());
        return dto;
    }
    @Transactional
    public QuestionDto createQuestion(QuestionDto questionDto) {
        Question question = new Question();
        question.setSubjectCategory(questionDto.getSubjectCategory());
        question.setTags(questionDto.getTags());
        question.setQuestionText(questionDto.getQuestionText());
        question.setQuestionType(questionDto.getQuestionType());
        question.setCorrectAnswer(questionDto.getCorrectAnswer());
        question.setCreator(questionDto.getCreator());
        // 不要在这里初始化 options 列表或添加 option

        // 1. 先保存 Question 实体，确保它获得数据库生成的 ID
        Question savedQuestionEntity = questionRepository.save(question);
        // 对于 GenerationType.IDENTITY，此时 savedQuestionEntity.getQuestionId() 应该有值了
        // 如果不确定，可以强制刷新
        // questionRepository.flush(); // 可选，通常对 IDENTITY 策略，save()后ID即用

        // 2. 创建并设置 Option 实体，现在可以使用 savedQuestionEntity 的 ID
        List<Option> optionsToSave = new ArrayList<>();
        if (!CollectionUtils.isEmpty(questionDto.getOptions())) {
            for (OptionDto optionDto : questionDto.getOptions()) {
                Option option = new Option();
                OptionId optionId = new OptionId();
                optionId.setOptionIdValue(optionDto.getOptionIdValue());
                // optionId.setQuestionId(savedQuestionEntity.getQuestionId()); // 不要手动设置，@MapsId 会做
                option.setId(optionId);
                option.setOptionText(optionDto.getOptionText());
                option.setQuestion(savedQuestionEntity); // **关键：将 Option 与已保存且有 ID 的 Question 关联**
                optionsToSave.add(option);
            }
        }

        // 3. 如果有 Option，批量保存它们（或者逐个保存）
        // 由于 Option 与 Question 的关联已设置，并且 Question 已持久化，
        // @MapsId 应该能正确工作。
        // 如果 OptionRepository 存在：
        if (!optionsToSave.isEmpty()) {
            optionRepository.saveAll(optionsToSave); // 假设有 optionRepository
        }
        // 同时，需要将这些保存的 option 添加回 savedQuestionEntity 的集合中，如果后续需要从 Question 对象访问它们
        // 或者，重新从数据库加载 Question 以获取其关联的 Options
        // savedQuestionEntity.setOptions(optionsToSave); // 这可能再次触发merge，要小心

        // 为了确保返回的 DTO 包含选项，需要重新加载或手动组装
        // 最简单的方式是重新加载 Question
        Question finalQuestion = questionRepository.findById(savedQuestionEntity.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Failed to reload question"));

        return convertToDto(finalQuestion); // convertToDto 会处理 Options
    }

    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(Integer questionId) {
        Question question = questionRepository.findById(questionId).orElseThrow(()-> new ResourceNotFoundException("Question not found"));
        return convertToDto(question);
    }
    @Transactional(readOnly = true)
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(this::convertToDto).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<QuestionDto> findBySubjectCategory(String category) {
        return questionRepository.findBySubjectCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> findByQuestionType(String type) {
        return questionRepository.findByQuestionType(type).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<QuestionDto> findByTagsContaining(String keyword) {
        return questionRepository.findByTagsContaining(keyword).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public QuestionDto updateQuestion(Integer questionId, QuestionDto questionDetailsDto) {
        Question question = questionRepository.findById(questionId).orElseThrow(()-> new ResourceNotFoundException("Question not found for updating with id: " + questionId));
        question.setSubjectCategory(questionDetailsDto.getSubjectCategory());
        question.setTags(questionDetailsDto.getTags());
        question.setQuestionText(questionDetailsDto.getQuestionText());
        question.setQuestionType(questionDetailsDto.getQuestionType());
        question.setCorrectAnswer(questionDetailsDto.getCorrectAnswer());
        question.setCreator(questionDetailsDto.getCreator()); // 或者不允许更改创建者

        // 使用JPA友好的 直接依赖cascade
        if(question.getOptions() != null) {

            List<Option> oldOptions = new ArrayList<>(question.getOptions());
            for(Option oldOpt: oldOptions){
                question.removeOption(oldOpt);
            }
            questionRepository.save(question);
        }else{
            question.setOptions(new ArrayList<>());
        }
        // 简单粗暴的方法：直接删除所有旧的，然后添加新的。
        // 这依赖于 Option 的主键生成策略和 Question 中 Option 集合的级联设置。
        question.getOptions().clear(); // 清空当前集合，触发 orphan removal
        questionRepository.saveAndFlush(question); // 先保存一次以应用删除
        // 重新加载question实体，以确保获取最新的状态（特别是options集合）
        question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found after clearing options with id: " + questionId));
        if (!CollectionUtils.isEmpty(questionDetailsDto.getOptions())) {
            for (OptionDto optionDto : questionDetailsDto.getOptions()) {
                Option option = new Option();
                OptionId optionId = new OptionId();
                optionId.setOptionIdValue(optionDto.getOptionIdValue());
                // questionId 将由 JPA 在保存 Question 时通过 @MapsId 和关联自动设置
                option.setId(optionId);
                option.setOptionText(optionDto.getOptionText());
                question.addOption(option); // addOption 会设置 option.setQuestion(this)
            }
        }
        Question updatedQuestion = questionRepository.save(question);
        return convertToDto(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Integer questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found for deletion with id: " + questionId);
        }
        // 由于 Question 实体中对 options 集合设置了 CascadeType.ALL 和 orphanRemoval=true,
        // 删除 Question 时，与之关联的 Option 应该会被自动删除。
        questionRepository.deleteById(questionId);
    }

    public List<QuestionDto> findQuestionsByCriteria(String subjectCategory, String questionType, List<String> tags) {
        List<Question> questions;

        if (questionType != null && tags != null) {
            questions = questionRepository.findByQuestionTypeAndTags(questionType, tags);
        } else if (subjectCategory != null) {
            questions = questionRepository.findBySubjectCategory(subjectCategory);
        } else if (questionType != null) {
            questions = questionRepository.findByQuestionType(questionType);
        } else if (tags != null) {
            questions = questionRepository.findByTags(tags);
        } else {
            questions = questionRepository.findAll();
        }

        return questions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

}
