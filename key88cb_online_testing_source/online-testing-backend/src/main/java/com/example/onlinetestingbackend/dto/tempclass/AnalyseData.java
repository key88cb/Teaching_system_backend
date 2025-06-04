package com.example.onlinetestingbackend.dto.tempclass;

import java.util.HashMap;
import java.util.Map;

public class AnalyseData {
    // 各个选项的选择次数
    private Map<String, Integer> optionCounts = new HashMap<>();
    // 总人数
    private int totalStudents = 0;
    // 总得分
    private int totalScore = 0;

    public AnalyseData() {
        // 初始化 A/B/C/D 的计数为 0
        optionCounts.put("A", 0);
        optionCounts.put("B", 0);
        optionCounts.put("C", 0);
        optionCounts.put("D", 0);
    }

    public void recordAnswer(String answer, int score) {
        if (answer == null || answer.isEmpty()) {
            return; // 防空指针或空值
        }

        // 将字符串转换成字符数组遍历处理
        for (char c : answer.toCharArray()) {
            String key = String.valueOf(c).toUpperCase(); // 统一转大写处理
            if (optionCounts.containsKey(key)) {
                optionCounts.put(key, optionCounts.get(key) + 1);
            }
        }

        // 更新总人数和总得分（根据你的业务可以调整）
        totalStudents += 1;
        totalScore += score;
    }

    public float getAverageScore() {
        return totalStudents == 0 ? 0 : (float) totalScore / totalStudents;
    }

    public Map<String, Integer> getOptionCounts() {
        return optionCounts;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public int getTotalScore() {
        return totalScore;
    }
}