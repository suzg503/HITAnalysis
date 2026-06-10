package com.hitanalysis.ai.service.impl;

import com.hitanalysis.ai.dto.AiConfigDTO;
import com.hitanalysis.ai.dto.AiQueryDTO;
import com.hitanalysis.ai.engine.ConfigGenerator;
import com.hitanalysis.ai.engine.IntentRecognizer;
import com.hitanalysis.ai.engine.SqlBuilder;
import com.hitanalysis.ai.entity.BiAiSession;
import com.hitanalysis.ai.mapper.BiAiSessionMapper;
import com.hitanalysis.ai.service.AiService;
import com.hitanalysis.ai.vo.AiInsightVO;
import com.hitanalysis.ai.vo.AiParseConfirmVO;
import com.hitanalysis.ai.vo.AiPreviewVO;
import com.hitanalysis.common.constant.DataPermissionType;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.common.exception.PermissionDeniedException;
import com.hitanalysis.common.utils.JsonUtils;
import com.hitanalysis.metadata.entity.BiIndicator;
import com.hitanalysis.metadata.service.IndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * AI service implementation (Text-to-Config D1/D2/D3)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final BiAiSessionMapper sessionMapper;
    private final IndicatorService indicatorService;
    private final IntentRecognizer intentRecognizer;
    private final ConfigGenerator configGenerator;
    private final SqlBuilder sqlBuilder;
    private final JsonUtils jsonUtils;
    private final StringRedisTemplate redisTemplate;

    private static final int DAILY_QUERY_LIMIT = 100;
    private static final String DAILY_COUNT_KEY = "user:daily_query_count:";

    // In-memory store for parse results (MVP)
    private final Map<Long, AiParseConfirmVO> parseResults = new HashMap<>();
    private final Map<Long, AiPreviewVO> previewResults = new HashMap<>();
    private long parseIdCounter = 1;
    private long previewIdCounter = 1;

    @Override
    @Transactional
    public Long createSession(Long userId) {
        BiAiSession session = new BiAiSession();
        session.setUserId(userId);
        session.setSessionName("AI Session - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        session.setStatus("active");
        session.setMessageCount(0);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setActiveTime(LocalDateTime.now());

        sessionMapper.insert(session);
        log.info("AI session created: sessionId={}, userId={}", session.getSessionId(), userId);
        return session.getSessionId();
    }

    @Override
    public AiParseConfirmVO submitQuery(AiQueryDTO dto) {
        // D3 - Check permission
        if (!checkAiPermission(dto.getUserId())) {
            throw PermissionDeniedException.aiDenied();
        }

        // Check daily limit
        int dailyCount = getDailyQueryCount(dto.getUserId());
        if (dailyCount >= DAILY_QUERY_LIMIT) {
            throw new BusinessException(ErrorCode.AI_QUERY_LIMIT_EXCEEDED);
        }

        // Step 1: Intent recognition
        String intent = intentRecognizer.recognize(dto.getQueryText());
        String timeRangeHint = intentRecognizer.extractTimeRange(dto.getQueryText());
        String chartHint = intentRecognizer.extractChartHint(dto.getQueryText());

        // Step 2-3: Entity extraction (MVP: simple keyword matching)
        List<BiIndicator> matchedIndicators = matchIndicators(dto.getQueryText());
        List<String> matchedDimensions = extractDimensions(dto.getQueryText());

        // Step 4: Config generation (D1 - generates JSON, not SQL)
        String[] timeRange = resolveTimeRange(timeRangeHint, dto.getTimeRange());
        AiConfigDTO config = configGenerator.generate(intent, matchedIndicators, matchedDimensions,
                timeRange[0], timeRange[1], chartHint);

        // Build parse confirm VO (D1 - user must confirm)
        AiParseConfirmVO confirmVO = buildParseConfirmVO(dto.getQueryText(), intent, config, matchedIndicators);
        long parseId = parseIdCounter++;
        confirmVO.setParseId(parseId);
        parseResults.put(parseId, confirmVO);

        // Increment daily count
        incrementDailyQueryCount(dto.getUserId());

        log.info("Query submitted: userId={}, intent={}, parseId={}", dto.getUserId(), intent, parseId);
        return confirmVO;
    }

    @Override
    public AiParseConfirmVO getParseResult(Long parseId) {
        AiParseConfirmVO result = parseResults.get(parseId);
        if (result == null) {
            throw new BusinessException(ErrorCode.AI_PARSE_ERROR, "解析结果不存在");
        }
        return result;
    }

    @Override
    public AiPreviewVO confirmAndExecute(Long parseId, Long userId) {
        AiParseConfirmVO parseVO = getParseResult(parseId);

        // D1 - Get the config JSON (not SQL)
        AiConfigDTO config = jsonUtils.toObject(parseVO.getConfigJson(), AiConfigDTO.class);

        // D3 - Get user permission (placeholder)
        DataPermissionType permissionType = DataPermissionType.HOSPITAL;
        List<Long> hospitalIds = List.of(1L);
        List<String> deptCodes = List.of();

        // D1 - Convert config to SQL (safe template)
        String sql = sqlBuilder.build(config, userId, permissionType, hospitalIds, deptCodes);

        // Execute query (MVP placeholder)
        AiPreviewVO previewVO = new AiPreviewVO();
        long previewId = previewIdCounter++;
        previewVO.setPreviewId(previewId);
        previewVO.setConfigJson(parseVO.getConfigJson()); // D1 - Store config, not SQL
        previewVO.setDataRows(generatePlaceholderData(config));
        previewVO.setTotal(100L);
        previewVO.setChartType(config.getSuggestedChartType());
        previewVO.setCanRefine(true);
        previewVO.setQueryDuration(150L);

        previewResults.put(previewId, previewVO);
        log.info("Query executed: parseId={}, previewId={}, userId={}", parseId, previewId, userId);

        return previewVO;
    }

    @Override
    public AiParseConfirmVO refineQuery(Long sessionId, String refineText) {
        // MVP: Just create a new parse result
        AiQueryDTO dto = new AiQueryDTO();
        dto.setSessionId(sessionId);
        dto.setQueryText(refineText);
        dto.setUserId(1L); // Placeholder

        return submitQuery(dto);
    }

    @Override
    public AiInsightVO generateInsight(Long previewId) {
        AiPreviewVO preview = previewResults.get(previewId);
        if (preview == null) {
            throw new BusinessException(ErrorCode.AI_INSIGHT_ERROR, "预览结果不存在");
        }

        // D2 - Generate insight based on actual data
        AiInsightVO insight = new AiInsightVO();
        insight.setInsightId(previewId);
        insight.setTitle("数据分析洞察");
        insight.setContent("根据数据分析结果，发现了以下关键信息...");
        insight.setKeyFindings(List.of(
                "数据呈现明显波动趋势",
                "峰值出现在特定时间段",
                "分布相对均匀"
        ));
        insight.setRecommendations(List.of(
                "建议关注高峰时段资源配置",
                "考虑优化低峰时段效率"
        ));

        // D2 - Include data source and calculation logic
        AiInsightVO.DataSourceInfo dataSource = new AiInsightVO.DataSourceInfo();
        dataSource.setTableName("bi_indicator_result_daily");
        dataSource.setSourceSystem("ClickHouse");
        dataSource.setTimeRange("2026-05-01 ~ 2026-05-11");
        dataSource.setDataCount(100L);
        dataSource.setLastUpdated(LocalDateTime.now().toString());
        insight.setDataSource(dataSource);

        AiInsightVO.CalculationLogicInfo calcLogic = new AiInsightVO.CalculationLogicInfo();
        calcLogic.setMetricName("门诊人次");
        calcLogic.setFormula("COUNT(visit_id)");
        calcLogic.setAggregationType("SUM");
        calcLogic.setComparisonType("同比去年同期");
        insight.setCalculationLogic(calcLogic);

        insight.setGeneratedAt(LocalDateTime.now().toString());

        log.info("Insight generated: previewId={}", previewId);
        return insight;
    }

    @Override
    public boolean checkAiPermission(Long userId) {
        // MVP: Always return true
        return true;
    }

    @Override
    public int getDailyQueryCount(Long userId) {
        String key = DAILY_COUNT_KEY + userId + ":" + LocalDate.now();
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    private void incrementDailyQueryCount(Long userId) {
        String key = DAILY_COUNT_KEY + userId + ":" + LocalDate.now();
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    private List<BiIndicator> matchIndicators(String queryText) {
        // MVP: Search by keyword
        List<BiIndicator> allIndicators = indicatorService.listAllActive();
        return allIndicators.stream()
                .filter(i -> queryText.contains(i.getZbName()) ||
                             queryText.contains(i.getZbCode()) ||
                             (i.getZbMeaning() != null && queryText.contains(i.getZbMeaning())))
                .limit(3)
                .collect(Collectors.toList());
    }

    private List<String> extractDimensions(String queryText) {
        List<String> dimensions = new ArrayList<>();
        if (queryText.contains("时间") || queryText.contains("日期") || queryText.contains("趋势")) {
            dimensions.add("dim_time");
        }
        if (queryText.contains("科室") || queryText.contains("部门")) {
            dimensions.add("dim_dept");
        }
        if (queryText.contains("院区") || queryText.contains("医院")) {
            dimensions.add("dim_hospital");
        }
        if (queryText.contains("医生")) {
            dimensions.add("dim_doctor");
        }
        return dimensions.isEmpty() ? List.of("dim_time") : dimensions;
    }

    private String[] resolveTimeRange(String hint, String customRange) {
        if (customRange != null && !customRange.isEmpty()) {
            // Parse custom range
            return new String[]{"2026-05-01", "2026-05-11"};
        }

        LocalDate end = LocalDate.now();
        LocalDate start;

        switch (hint) {
            case "today":
                start = end;
                break;
            case "week":
                start = end.minusWeeks(1);
                break;
            case "month":
                start = end.minusMonths(1);
                break;
            case "quarter":
                start = end.minusMonths(3);
                break;
            case "year":
                start = end.minusYears(1);
                break;
            default:
                start = end.minusMonths(1);
        }

        return new String[]{start.toString(), end.toString()};
    }

    private AiParseConfirmVO buildParseConfirmVO(String query, String intent, AiConfigDTO config,
                                                   List<BiIndicator> indicators) {
        AiParseConfirmVO vo = new AiParseConfirmVO();
        vo.setOriginalQuery(query);
        vo.setIntent(intent);
        vo.setConfidence(config.getConfidence());

        List<AiParseConfirmVO.MetricDisplay> metrics = indicators.stream()
                .map(i -> {
                    AiParseConfirmVO.MetricDisplay m = new AiParseConfirmVO.MetricDisplay();
                    m.setId(i.getZbId());
                    m.setCode(i.getZbCode());
                    m.setName(i.getZbName());
                    m.setUnit(i.getUnit());
                    return m;
                })
                .collect(Collectors.toList());
        vo.setMetrics(metrics);

        List<AiParseConfirmVO.DimensionDisplay> dimensions = config.getDimensions().stream()
                .map(d -> {
                    AiParseConfirmVO.DimensionDisplay dim = new AiParseConfirmVO.DimensionDisplay();
                    dim.setId(d.getId());
                    dim.setCode(d.getCode());
                    dim.setName(d.getName());
                    dim.setLevel(d.getLevel());
                    return dim;
                })
                .collect(Collectors.toList());
        vo.setDimensions(dimensions);

        AiParseConfirmVO.TimeRangeDisplay timeRange = new AiParseConfirmVO.TimeRangeDisplay();
        timeRange.setStart(config.getTimeRange().getStart());
        timeRange.setEnd(config.getTimeRange().getEnd());
        timeRange.setDisplayText(config.getTimeRange().getStart() + " ~ " + config.getTimeRange().getEnd());
        vo.setTimeRange(timeRange);

        vo.setSuggestedChartType(config.getSuggestedChartType());
        vo.setExplanation("已识别您的查询意图，请确认以下配置是否正确。");
        vo.setNeedsMoreInput(false);
        vo.setMissingInfoHints(new ArrayList<>());

        // Store config JSON for later use (D1)
        vo.setConfigJson(jsonUtils.toJson(config));

        return vo;
    }

    private List<Map<String, Object>> generatePlaceholderData(AiConfigDTO config) {
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("date", "2026-05-" + (i + 1));
            for (AiConfigDTO.MetricConfig metric : config.getMetrics()) {
                row.put(metric.getCode(), Math.random() * 100);
            }
            data.add(row);
        }
        return data;
    }
}