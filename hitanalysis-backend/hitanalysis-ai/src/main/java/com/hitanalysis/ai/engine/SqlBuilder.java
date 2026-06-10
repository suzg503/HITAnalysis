package com.hitanalysis.ai.engine;

import com.hitanalysis.ai.dto.AiConfigDTO;
import com.hitanalysis.common.constant.DataPermissionType;
import com.hitanalysis.common.exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL Builder (D1 decision implementation)
 *
 * Converts Config JSON to safe SQL templates.
 * This is the ONLY place where SQL is generated - AI never produces SQL directly.
 *
 * D3: Injects permission filtering into SQL
 */
@Slf4j
@Component
public class SqlBuilder {

    /**
     * Build SQL from config JSON (D1)
     * D3: Inject permission filter
     */
    public String build(AiConfigDTO config, Long userId, DataPermissionType permissionType,
                        List<Long> authorizedHospitalIds, List<String> authorizedDeptCodes) {

        // D3 - Check cross-hospital permission
        if (permissionType == DataPermissionType.CROSS_HOSPITAL) {
            if (authorizedHospitalIds == null || authorizedHospitalIds.isEmpty()) {
                throw PermissionDeniedException.crossHospital();
            }
        }

        StringBuilder sql = new StringBuilder();

        // Build SELECT clause
        sql.append("SELECT ");

        // Add dimensions
        List<String> dimensionFields = config.getDimensions().stream()
                .map(d -> getDimensionField(d.getCode()))
                .collect(Collectors.toList());
        sql.append(String.join(", ", dimensionFields));

        if (!dimensionFields.isEmpty()) {
            sql.append(", ");
        }

        // Add metrics
        List<String> metricFields = config.getMetrics().stream()
                .map(m -> getMetricExpression(m))
                .collect(Collectors.toList());
        sql.append(String.join(", ", metricFields));

        // Build FROM clause (placeholder for MVP)
        sql.append(" FROM placeholder_table ");

        // Build WHERE clause with time range
        sql.append(" WHERE 1=1 ");

        if (config.getTimeRange() != null) {
            sql.append(" AND date BETWEEN '")
               .append(config.getTimeRange().getStart())
               .append("' AND '")
               .append(config.getTimeRange().getEnd())
               .append("' ");
        }

        // D3 - Inject permission filter
        sql.append(buildPermissionFilter(permissionType, userId, authorizedHospitalIds, authorizedDeptCodes));

        // Add user filters
        if (config.getFilters() != null && !config.getFilters().isEmpty()) {
            for (AiConfigDTO.FilterConfig filter : config.getFilters()) {
                sql.append(" AND ")
                   .append(filter.getField())
                   .append(" ")
                   .append(getOperatorSymbol(filter.getOperator()))
                   .append(" ")
                   .append(formatValue(filter.getValue()));
            }
        }

        // Build GROUP BY clause
        if (!dimensionFields.isEmpty()) {
            sql.append(" GROUP BY ")
               .append(String.join(", ", dimensionFields));
        }

        log.info("SQL built from config: intent={}, userId={}, permissionType={}",
                config.getIntent(), userId, permissionType);

        return sql.toString();
    }

    /**
     * D3 - Build permission filter clause
     */
    private String buildPermissionFilter(DataPermissionType permissionType, Long userId,
                                          List<Long> authorizedHospitalIds, List<String> authorizedDeptCodes) {

        StringBuilder filter = new StringBuilder();

        switch (permissionType) {
            case SELF:
                filter.append(" AND creator_id = ").append(userId);
                break;

            case DEPT:
                if (authorizedDeptCodes != null && !authorizedDeptCodes.isEmpty()) {
                    filter.append(" AND dept_id IN (")
                          .append(authorizedDeptCodes.stream()
                                  .map(c -> "'" + c + "'")
                                  .collect(Collectors.joining(",")))
                          .append(")");
                }
                break;

            case HOSPITAL:
                filter.append(" AND hospital_id = ").append(authorizedHospitalIds.get(0));
                break;

            case CROSS_HOSPITAL:
                if (authorizedHospitalIds != null && !authorizedHospitalIds.isEmpty()) {
                    filter.append(" AND hospital_id IN (")
                          .append(authorizedHospitalIds.stream()
                                  .map(String::valueOf)
                                  .collect(Collectors.joining(",")))
                          .append(")");
                }
                break;

            case ALL:
                // No additional filter
                break;
        }

        return filter.toString();
    }

    private String getDimensionField(String code) {
        switch (code) {
            case "dim_time":
                return "date";
            case "dim_dept":
                return "dept_id";
            case "dim_hospital":
                return "hospital_id";
            case "dim_doctor":
                return "doctor_id";
            default:
                return code;
        }
    }

    private String getMetricExpression(AiConfigDTO.MetricConfig metric) {
        String agg = metric.getAggregationType() != null ? metric.getAggregationType() : "SUM";
        return agg + "(value) as " + metric.getCode();
    }

    private String getOperatorSymbol(String operator) {
        switch (operator) {
            case "eq":
                return "=";
            case "ne":
                return "!=";
            case "gt":
                return ">";
            case "lt":
                return "<";
            case "in":
                return "IN";
            default:
                return "=";
        }
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            return "(" + list.stream()
                    .map(v -> v instanceof String ? "'" + v + "'" : String.valueOf(v))
                    .collect(Collectors.joining(",")) + ")";
        }
        return String.valueOf(value);
    }
}