package com.example.onlinetestingbackend.controller;

import com.example.onlinetestingbackend.dto.QuestionDto;
import com.example.onlinetestingbackend.service.QuestionService;
import com.example.onlinetestingbackend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 可能需要校验
//import jakarta.validation.Valid;
import java.util.List;
@RestController
@RequestMapping("/api/questions")
// controller处要自己加上下面这行跨域部分 之后@getmapping里面要写相对位置而不是绝对位置 这样最后好改动！
// 后端的dto要充分利用到 现在的dto类过多 如果再创建dto的话请自己创建文件夹!
// 后面的部分是直接遵循restful规则编写的 也可以只post或get 一切以代码方便使用为准！
@CrossOrigin(origins = "http://localhost:5173")
public class QuestionController {
    private final QuestionService questionService;
    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }
    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(/*@Valid*/ @RequestBody QuestionDto questionDto) {
        // 返回的请求体有可能不是典型的数据类型，比如一张查找表，这时可以自己创建dto
        QuestionDto createdQuestion = questionService.createQuestion(questionDto);
        return new ResponseEntity<>(createdQuestion, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Integer id) {
        // QuestionService 中的 getQuestionById 可能会抛出 ResourceNotFoundException,
        // 如果有全局异常处理器 (@ControllerAdvice) 会处理它并返回 404。
        // 否则，这里也可以 try-catch 或让它直接抛出。
        QuestionDto questionDto = questionService.getQuestionById(id);
        return ResponseEntity.ok(questionDto); // 返回 HTTP 200 OK 和题目数据
    }
    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions(
            @RequestParam(required = false) String subjectCategory,
            @RequestParam(required = false) String questionType,
            @RequestParam(required = false) List<String> tags
    ) {
        List<QuestionDto> questions = questionService.findQuestionsByCriteria(subjectCategory, questionType, tags);
        return ResponseEntity.ok(questions);
    }



    // 4. 更新题目 (PUT /api/questions/{id})
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Integer id, /*@Valid*/ @RequestBody QuestionDto questionDetailsDto) {
        QuestionDto updatedQuestion = questionService.updateQuestion(id, questionDetailsDto);
        return ResponseEntity.ok(updatedQuestion);
    }

    // 5. 删除题目 (DELETE /api/questions/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteQuestion(id);
        // 返回 HTTP 204 No Content 表示成功删除且响应体为空
        return ResponseEntity.noContent().build();
    }
    // 十分建议在类中加这个全局异常处理器 调试起来会很方便！
    @ControllerAdvice
    public class GlobalExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
