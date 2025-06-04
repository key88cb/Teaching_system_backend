package com.example.onlinetestingbackend.service;

import com.example.onlinetestingbackend.dto.*;
import com.example.onlinetestingbackend.dto.tempclass.AnalyseData;
import com.example.onlinetestingbackend.entity.*;
import com.example.onlinetestingbackend.entity.id.ExamResultId;
import com.example.onlinetestingbackend.entity.id.PaperInfoId;
import com.example.onlinetestingbackend.entity.PaperInfo;
import com.example.onlinetestingbackend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExamQuestionService {

    @Autowired
    private ExamResultRepository examResultRepository;
    @Autowired
    private PaperInfoRepository paperInfoRepository;
    @Autowired
    private PaperQuestionRepository paperQuestionRepository;
    @Autowired
    private DetailedResultRepository detailedResultRepository;
    @Autowired
    private TemporarySubmissionRepository temporarySubmissionRepository;
    private static Set<Character> toUniqueCharacterSet(String str) {
        Set<Character> set = new HashSet<>();
        for (char c : str.toCharArray()) {
            set.add(c);
        }
        return set;
    }
    @Transactional
    public void judgeResult(ExamPlainRecordDto examResult) {
        try {
            int paperId = examResult.getPaperId();
            int courseId = examResult.getCourseId();
            int studentId = examResult.getStudentId();
            int totalscore = 0;
            List<PlainAnswerDto> plainAnswerDtoList = examResult.getAnswers();
            List<DetailedResult> DetailResults = new ArrayList<>();
            for (PlainAnswerDto plainAnswerDto : plainAnswerDtoList) {
                int questionId = plainAnswerDto.getQuestionId();
                PaperQuestion question = paperQuestionRepository.findByPaperIdAndCourseIdAndQuestionId(paperId, courseId, questionId);
                String answerbystudent = plainAnswerDto.getAnswer();
                String answer = question.getCorrectAnswer();
                String type = question.getQuestionType();
                int score = question.getPoints();
                DetailedResult detailedResult = new DetailedResult();
                detailedResult.setPaperId(paperId);
                detailedResult.setCourseId(courseId);
                detailedResult.setStudentId(studentId);
                detailedResult.setStudentAnswer(answerbystudent);
                detailedResult.setQuestionId(questionId);
                detailedResult.setCorrectAnswer(answer);
                if (type.equals("Multiple Choice")) {
                    if (answerbystudent.equals(answer)) {
                        detailedResult.setPoints(score);
                    } else {
                        Set<Character> characterSet = toUniqueCharacterSet(answer);
                        Set<Character> set = toUniqueCharacterSet(answerbystudent);
                        if (characterSet.containsAll(set)) {
                            detailedResult.setPoints((score + 1) / 2);
                        } else {
                            detailedResult.setPoints(0);
                        }
                    }
                } else {
                    if (answerbystudent.equals(answer)) {
                        detailedResult.setPoints(score);
                    } else {
                        detailedResult.setPoints(0);
                    }
                }
                totalscore += detailedResult.getPoints();
                DetailResults.add(detailedResult);
            }
            detailedResultRepository.saveAll(DetailResults);
            ExamResult examResult1 = new ExamResult();
            examResult1.setPaperId(paperId);
            examResult1.setCourseId(courseId);
            examResult1.setStudentId(studentId);
            examResult1.setTotalScore(totalscore);
            examResult1.setFinishTime(examResult.getFinishTime());
            examResult1.setStartTime(examResult.getStartTime());
            examResultRepository.save(examResult1);
            Optional<ExamResult> temp = examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId)
                    .stream().findFirst();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            throw new RuntimeException("评分失败", e); // 重新抛出异常，确保事务回滚

        }
    }
    @Transactional
    public void editResultforonestudent(ScoreEditDto scoreEditDto) {
        int paperId = scoreEditDto.getPaperId();
        int courseId = scoreEditDto.getCourseId();
        int studentId = scoreEditDto.getStudentId();
        int questionId = scoreEditDto.getQuestionId();
        int score = scoreEditDto.getScore();
        int totalscore = 0;
        DetailedResult detailedResult =detailedResultRepository.findByPaperIdAndCourseIdAndStudentIdAndQuestionId(paperId,courseId,studentId,questionId);
        DetailedResult detailedResult1 = new DetailedResult();
        int score2=detailedResult.getPoints();
        detailedResult1.setPaperId(paperId);
        detailedResult1.setCourseId(courseId);
        detailedResult1.setStudentId(studentId);
        detailedResult1.setQuestionId(questionId);
        detailedResult1.setPoints(score);
        detailedResult1.setCorrectAnswer(detailedResult.getCorrectAnswer());
        detailedResult1.setStudentAnswer(detailedResult.getStudentAnswer());
        detailedResultRepository.save(detailedResult1);
        Optional<ExamResult> examResult=examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId);
        if (!examResult.isPresent()) {
            System.err.println("未查到对应学生");
            return;
        }
        int totalscoretemp=examResult.get().getTotalScore();
        int scoretemp=score-score2;
        totalscore=totalscoretemp+scoretemp;
        ExamResult examResult1=new ExamResult();
        examResult1.setPaperId(paperId);
        examResult1.setCourseId(courseId);
        examResult1.setStudentId(studentId);
        examResult1.setTotalScore(totalscore);
        examResult1.setFinishTime(examResult.get().getFinishTime());
        examResult1.setStartTime(examResult.get().getStartTime());
        examResultRepository.save(examResult1);
    }
    @Transactional
    public void editResultformanystudent(List<ScoreEditDto> scoreEditDtos) {
        for (ScoreEditDto scoreEditDto : scoreEditDtos) {
        int paperId = scoreEditDto.getPaperId();
        int courseId = scoreEditDto.getCourseId();
        int studentId = scoreEditDto.getStudentId();
        int questionId = scoreEditDto.getQuestionId();
        int score = scoreEditDto.getScore();
        int totalscore = 0;
        DetailedResult detailedResult = detailedResultRepository.findByPaperIdAndCourseIdAndStudentIdAndQuestionId(paperId, courseId, studentId, questionId);
        Optional<ExamResult> examResult = examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId, courseId, studentId);
        if (!examResult.isPresent()) {
            System.err.println("未查到对应学生");
            return;
        }
        int totalscoretemp = examResult.get().getTotalScore();
        int score2 = detailedResult.getPoints();
        int scoretemp = score - score2;
        totalscore = totalscoretemp + scoretemp;
        ExamResult examResult1 = new ExamResult();
        examResult1.setPaperId(paperId);
        examResult1.setCourseId(courseId);
        examResult1.setStudentId(studentId);
        examResult1.setTotalScore(totalscore);
        examResult1.setFinishTime(examResult.get().getFinishTime());
            examResult1.setStartTime(examResult.get().getStartTime());
        ExamResult temp=examResultRepository.save(examResult1);
        System.out.println("<UNK>");
        }
    }

    @Transactional
    public String checkExamStatus(PaperInfoDto paperInfoDto) {
        int paperId = paperInfoDto.getPaperId();
        int courseId = paperInfoDto.getCourseId();

        // 根据复合主键查找试卷信息
        Optional<PaperInfo> paperInfoOpt = paperInfoRepository.findById(new PaperInfoId(paperId, courseId));
        if (paperInfoOpt.isEmpty()) {
            return "无效的试卷或课程";
        }

        PaperInfo paperInfo = paperInfoOpt.get();
        LocalDateTime now = LocalDateTime.now();  // 使用 java.util.Date

        LocalDateTime openTime = paperInfo.getOpenTime();
        LocalDateTime closeTime = paperInfo.getCloseTime();

        if (now.isBefore(openTime)) {
            return "考试尚未开始";
        } else if (now.isAfter(closeTime)) {
            return "考试已结束";
        } else {
            return "进入考试";
        }
    }
    @Transactional
    public RecordsDto searchbycourseIdandpaperId(Integer courseId, Integer paperId) {
        RecordsDto results = new RecordsDto();
        List<DetailedResult> temp=detailedResultRepository.findByPaperIdAndCourseId(paperId,courseId);
        Map<Integer, Map<Integer, AnalyseData>> statsMap = new HashMap<>();

        for (DetailedResult record : temp) {
            Integer questionId = record.getQuestionId();
            String studentAnswer = record.getStudentAnswer();
            Integer points = record.getPoints();

            // 如果没有这个课程，创建一个 map
            statsMap.putIfAbsent(courseId, new HashMap<>());
            Map<Integer, AnalyseData> questionMap = statsMap.get(courseId);

            // 如果没有这道题，初始化一个 AnalyseData
            questionMap.putIfAbsent(questionId, new AnalyseData());
            AnalyseData data = questionMap.get(questionId);

            // 记录一次答题
            data.recordAnswer(studentAnswer, points);
        }
        Map<Integer, AnalyseData> questionMap = statsMap.get(courseId);
        List<AnalyseDto> analyseResult = new ArrayList<>();
        for (Integer questionId : questionMap.keySet()) {
            AnalyseData data = questionMap.get(questionId);

            AnalyseDto dto = new AnalyseDto();
            dto.setAvgscore(data.getAverageScore());

            // 将选项 A/B/C/D 的计数顺序放入 List<Integer>
            List<Integer> counts = new ArrayList<>();
            counts.add(data.getOptionCounts().get("A"));
            counts.add(data.getOptionCounts().get("B"));
            counts.add(data.getOptionCounts().get("C"));
            counts.add(data.getOptionCounts().get("D"));

            dto.setCounts(counts);
            dto.setQuestionId(questionId);
            analyseResult.add(dto);
    }
        results.setCourseId(courseId);
        results.setPaperId(paperId);
        results.setAnalyses(analyseResult);
        return results;
}
    @Transactional
    public RecordDto searchbycourseIdandpaperIdandstudentId(Integer courseId, Integer paperId, Integer studentId) {
       List<DetailedResult> result=detailedResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId);
       List<DetailedResultDto> resultDtos=new ArrayList<>();
       RecordDto recordDto=new RecordDto();
       recordDto.setPaperId(paperId);
       recordDto.setCourseId(courseId);
       recordDto.setStudentId(studentId);
       for(DetailedResult detailedResult:result){
           DetailedResultDto detailedResultDto=new DetailedResultDto();
           detailedResultDto.setPaperId(paperId);
           detailedResultDto.setCourseId(courseId);
           detailedResultDto.setStudentId(studentId);
           detailedResultDto.setQuestionId(detailedResult.getQuestionId());
           detailedResultDto.setCorrectAnswer(detailedResult.getCorrectAnswer());
           detailedResultDto.setPoints(detailedResult.getPoints());
           detailedResultDto.setStudentAnswer(detailedResult.getStudentAnswer());
           resultDtos.add(detailedResultDto);
       }
       recordDto.setDetailedResults(resultDtos);
       return recordDto;
    }
    @Transactional
    public void submitExamManually(ExamPlainRecordDto dto) throws Exception {
        int paperId = dto.getPaperId();
        int courseId = dto.getCourseId();
        int studentId = dto.getStudentId();

        Optional<PaperInfo> paperInfoOpt = paperInfoRepository.findById(new PaperInfoId(paperId, courseId));
        if (paperInfoOpt.isEmpty()) {
            throw new RuntimeException("无效的试卷");
        }

        PaperInfo paperInfo = paperInfoOpt.get();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closeTime = paperInfo.getCloseTime();

//        if (now.isAfter(closeTime)) {
//            throw new RuntimeException("考试已结束，无法手动提交");
//        }
        try {
            String answerJson = serializeAnswers(dto);
            TemporarySubmission temporarySubmission = new TemporarySubmission();
            ExamResultId examResultId = new ExamResultId();
            examResultId.setPaperId(paperId);
            examResultId.setCourseId(courseId);
            examResultId.setStudentId(studentId);
            temporarySubmission.setPaperId(paperId);
            temporarySubmission.setCourseId(courseId);
            temporarySubmission.setStudentId(studentId);
            temporarySubmission.setSubmissionTime(now);
            temporarySubmission.setAnswersJson(answerJson);
            temporarySubmissionRepository.save(temporarySubmission);
//          ExamPlainRecordDto examPlainRecordDto =deserializeAnswers(temporarySubmission.getAnswersJson());
//          judgeResult(examPlainRecordDto);

        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("保存考试temp失败");
        }
    }
    @Transactional
    public List<ExamresultDto> searchexamresultbybycourseIdandpaperId(Integer courseId,Integer paperId){
        List<ExamresultDto> examresultDtos=new ArrayList<>();
        List<ExamResult> examResults = examResultRepository.findByPaperIdAndCourseId(paperId,courseId);
        for (ExamResult examResult : examResults) {
            ExamresultDto examresultDto=new ExamresultDto();
            examresultDto.setStudentId(examResult.getStudentId());
            examresultDto.setTotalScore(examResult.getTotalScore());
            examresultDto.setFinishTime(examResult.getFinishTime());
            examresultDto.setStartTime(examResult.getStartTime());
            examresultDtos.add(examresultDto);
        }
        return examresultDtos;
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void autoSubmitExams() {
        List<PaperInfo> papers = paperInfoRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (PaperInfo paper : papers) {
            int paperId = paper.getPaperId();
            int courseId = paper.getCourseId();
            LocalDateTime closeTime = paper.getCloseTime();

            if (now.isBefore(closeTime)) {
                continue;
            }

            List<TemporarySubmission> submissions = temporarySubmissionRepository.findByPaperIdAndCourseId(paperId, courseId);

            for (TemporarySubmission temp : submissions) {
                try {

                    ExamPlainRecordDto dto = deserializeAnswers(temp.getAnswersJson());

                    judgeResult(dto);

                    temporarySubmissionRepository.deleteById(temp.getId());
                } catch (Exception e) {
                    System.err.println("自动交卷失败：" + e.getMessage());
                }
            }
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ExamPlainRecordDto deserializeAnswers(String json) throws Exception {
        return objectMapper.readValue(json, ExamPlainRecordDto.class);
    }
    private String serializeAnswers(ExamPlainRecordDto dto) throws Exception {
    return objectMapper.writeValueAsString(dto);
}

    public ExamresultDto searchexamresultbybycourseIdandpaperIdandstudentId(Integer courseId, Integer paperId, Integer studentId) {
        ExamresultDto examresultDto = new ExamresultDto();
        Optional<ExamResult> examResult =examResultRepository.findByPaperIdAndCourseIdAndStudentId(paperId,courseId,studentId);
        examresultDto.setStudentId(studentId);
        if (!examResult.isPresent()) {
            examresultDto.setTotalScore(-1);
            return examresultDto;
        }
        examresultDto.setTotalScore(examResult.get().getTotalScore());
        return examresultDto;
    }

    public List<ExamwithNameDto> searchexamresultbystudentId(Integer studentId) {
        List<ExamwithNameDto> examwithNameDtos = new ArrayList<>();
        List<ExamResult> examResults = examResultRepository.findByStudentId(studentId);
        for (ExamResult examResult : examResults) {
            PaperInfo paperInfo = paperInfoRepository.findByCourseIdAndPaperId(examResult.getCourseId(), examResult.getPaperId());
            ExamwithNameDto examwithNameDto = new ExamwithNameDto();
            examwithNameDto.setStudentId(studentId);
            examwithNameDto.setPaperId(paperInfo.getPaperId());
            examwithNameDto.setCourseId(examResult.getCourseId());
            examwithNameDto.setTotalScore(examResult.getTotalScore());
            examwithNameDto.setOpenTime(paperInfo.getOpenTime());
            examwithNameDto.setCloseTime(paperInfo.getCloseTime());
            examwithNameDtos.add(examwithNameDto);
        }
        return examwithNameDtos;
    }
}
