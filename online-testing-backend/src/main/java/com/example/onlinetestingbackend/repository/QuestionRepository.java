// 文件路径: src/main/java/com/example/onlinetestingbackend/repository/QuestionRepository.java
package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // 明确声明这是一个 Repository Bean
public interface QuestionRepository extends JpaRepository<Question, Integer> { // Question 是实体类型, Integer 是主键类型

    /**
     * 根据科目类别查询题目列表。
     * Spring Data JPA 会根据方法名自动生成 SQL 查询。
     * @param subjectCategory 科目类别
     * @return 对应科目类别的题目列表
     */
    List<Question> findBySubjectCategory(String subjectCategory);

    /**
     * 根据题目类型查询题目列表。
     * @param questionType 题目类型 (例如："单选题", "多选题")
     * @return 对应题目类型的题目列表
     */
    List<Question> findByQuestionType(String questionType);

    /**
     * 根据创建者查询题目列表。
     * @param creator 创建者用户名或ID
     * @return 该创建者创建的题目列表
     */
    List<Question> findByCreator(String creator);

    /**
     * 根据标签模糊查询题目列表 (查询 tags 字段包含指定关键字的题目)。
     * @param keyword 标签中的关键字
     * @return 包含该关键字标签的题目列表
     */
    List<Question> findByTagsContaining(String keyword);

    /**
     * 根据题干内容模糊查询题目列表。
     * @param text 题干中包含的文本
     * @return 题干包含指定文本的题目列表
     */
    List<Question> findByQuestionTextContaining(String text);

    /**
     * 示例：使用 @Query 注解编写自定义 JPQL 查询。
     * 根据科目类别和题目类型查询。
     * @param subjectCategory 科目类别
     * @param questionType 题目类型
     * @return 符合条件的题目列表
     */
    @Query("SELECT q FROM Question q WHERE q.subjectCategory = :category AND q.questionType = :type")
    List<Question> findBySubjectCategoryAndQuestionType(@Param("category") String subjectCategory, @Param("type") String questionType);

    /**
     * 统计特定科目类别的题目数量。
     * @param subjectCategory 科目类别
     * @return 该科目类别的题目总数
     */
    long countBySubjectCategory(String subjectCategory);

    /**
     * 检查是否存在特定创建者创建的题目。
     * @param creator 创建者
     * @return 如果存在则返回 true，否则返回 false
     */
    boolean existsByCreator(String creator);

    @Query("SELECT q FROM Question q WHERE q.questionType = :type AND (:tags IS NULL OR q.tags IN (:tags))")
    List<Question> findByQuestionTypeAndTags(@Param("type") String type, @Param("tags") List<String> tags);

    @Query("SELECT q FROM Question q WHERE (:tags IS NULL OR q.tags IN (:tags))")
    List<Question> findByTags(@Param("tags") List<String> tags);
}