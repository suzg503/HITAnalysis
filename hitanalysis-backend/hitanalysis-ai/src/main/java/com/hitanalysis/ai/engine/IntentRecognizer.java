package com.hitanalysis.ai.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Intent Recognizer (part of Text-to-Config)
 *
 * Recognizes user intent from natural language input.
 * For MVP, uses simple keyword matching. Production would use LLM.
 */
@Slf4j
@Component
public class IntentRecognizer {

    /**
     * Recognize intent from user query
     */
    public String recognize(String queryText) {
        // Simple keyword-based recognition for MVP
        String lowerQuery = queryText.toLowerCase();

        if (containsAny(lowerQuery, "趋势", "变化", "走势", "趋势分析")) {
            return "trend_analysis";
        }

        if (containsAny(lowerQuery, "对比", "比较", "差异", "同比", "环比")) {
            return "comparison";
        }

        if (containsAny(lowerQuery, "排名", "排行", "top", "最高", "最低")) {
            return "ranking";
        }

        if (containsAny(lowerQuery, "分布", "占比", "构成", "百分比")) {
            return "distribution";
        }

        if (containsAny(lowerQuery, "明细", "详细", "列表", "具体")) {
            return "detail";
        }

        // Default to trend analysis
        return "trend_analysis";
    }

    /**
     * Extract time range keywords
     */
    public String extractTimeRange(String queryText) {
        String lowerQuery = queryText.toLowerCase();

        if (containsAny(lowerQuery, "今天", "今日")) {
            return "today";
        }
        if (containsAny(lowerQuery, "本周", "这周")) {
            return "week";
        }
        if (containsAny(lowerQuery, "本月", "这个月")) {
            return "month";
        }
        if (containsAny(lowerQuery, "本季度", "这季度")) {
            return "quarter";
        }
        if (containsAny(lowerQuery, "今年", "本年")) {
            return "year";
        }

        return "month"; // Default
    }

    /**
     * Extract chart type hint
     */
    public String extractChartHint(String queryText) {
        String lowerQuery = queryText.toLowerCase();

        if (containsAny(lowerQuery, "折线图", "线图", "趋势图")) {
            return "line";
        }
        if (containsAny(lowerQuery, "柱状图", "柱图", "条形图")) {
            return "bar";
        }
        if (containsAny(lowerQuery, "饼图", "环形图", "占比图")) {
            return "pie";
        }
        if (containsAny(lowerQuery, "表格", "列表")) {
            return "table";
        }

        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}