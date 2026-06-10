package com.hitanalysis.ai.engine;

import com.hitanalysis.ai.dto.AiConfigDTO;
import com.hitanalysis.metadata.entity.BiIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Config Generator (D1 decision implementation)
 *
 * Generates Config JSON from parsed entities - NEVER generates SQL directly.
 * SQL generation is handled by SqlBuilder separately.
 */
@Slf4j
@Component
public class ConfigGenerator {

    /**
     * Generate config JSON from recognized entities
     * D1: This method only produces JSON config, not SQL
     */
    public AiConfigDTO generate(String intent, List<BiIndicator> matchedIndicators,
                                List<String> matchedDimensions, String timeRangeStart,
                                String timeRangeEnd, String chartHint) {

        AiConfigDTO config = new AiConfigDTO();
        config.setIntent(intent);
        config.setConfidence(0.85); // Placeholder for MVP

        // Set metrics
        List<AiConfigDTO.MetricConfig> metrics = new ArrayList<>();
        for (BiIndicator indicator : matchedIndicators) {
            AiConfigDTO.MetricConfig metric = new AiConfigDTO.MetricConfig();
            metric.setId(indicator.getZbId());
            metric.setCode(indicator.getZbCode());
            metric.setName(indicator.getZbName());
            metric.setUnit(indicator.getUnit());
            metric.setAggregationType("SUM"); // Default aggregation
            metrics.add(metric);
        }
        config.setMetrics(metrics);

        // Set dimensions
        List<AiConfigDTO.DimensionConfig> dimensions = new ArrayList<>();
        for (String dim : matchedDimensions) {
            AiConfigDTO.DimensionConfig dimension = new AiConfigDTO.DimensionConfig();
            dimension.setCode(dim);
            dimension.setName(getDimensionDisplayName(dim));
            dimension.setLevel("day"); // Default level
            dimensions.add(dimension);
        }
        config.setDimensions(dimensions);

        // Set time range
        AiConfigDTO.TimeRangeConfig timeRangeConfig = new AiConfigDTO.TimeRangeConfig();
        timeRangeConfig.setStart(timeRangeStart);
        timeRangeConfig.setEnd(timeRangeEnd);
        timeRangeConfig.setType("day");
        config.setTimeRange(timeRangeConfig);

        // Set calculation
        AiConfigDTO.CalculationConfig calculation = new AiConfigDTO.CalculationConfig();
        calculation.setType("none");
        config.setCalculation(calculation);

        // Set filters (empty for MVP)
        config.setFilters(new ArrayList<>());

        // Set suggested chart type
        config.setSuggestedChartType(suggestChartType(intent, matchedIndicators.size(), chartHint));

        log.info("Config generated: intent={}, metrics={}, dimensions={}",
                intent, metrics.size(), dimensions.size());

        return config;
    }

    /**
     * Suggest chart type based on intent and data
     */
    private String suggestChartType(String intent, int metricCount, String hint) {
        if (hint != null && !hint.isEmpty()) {
            return hint;
        }

        switch (intent) {
            case "trend_analysis":
                return "line";
            case "comparison":
                return "bar";
            case "ranking":
                return "bar";
            case "distribution":
                return "pie";
            case "detail":
                return "table";
            default:
                return "bar";
        }
    }

    private String getDimensionDisplayName(String code) {
        switch (code) {
            case "dim_time":
                return "时间";
            case "dim_dept":
                return "科室";
            case "dim_hospital":
                return "院区";
            case "dim_doctor":
                return "医生";
            default:
                return code;
        }
    }
}