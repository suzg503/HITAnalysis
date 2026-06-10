package com.hitanalysis.ai.engine;

import com.hitanalysis.ai.dto.AiQueryDTO;
import com.hitanalysis.ai.vo.AiParseConfirmVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IntentRecognizer unit tests
 *
 * Tests AI intent recognition including:
 * - Indicator identification
 * - Dimension parsing
 * - Time range extraction
 * - Calculation logic identification
 */
@DisplayName("IntentRecognizer Tests")
class IntentRecognizerTest {

    private IntentRecognizer intentRecognizer;

    @BeforeEach
    void setUp() {
        intentRecognizer = new IntentRecognizer();
    }

    @Test
    @DisplayName("Parse simple query: 查询门诊人次")
    void testParseSimpleQuery() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("查询门诊人次");

        // When
        AiParseConfirmVO result = intentRecognizer.recognize(query);

        // Then
        assertNotNull(result);
        assertEquals("门诊人次", result.getIndicatorName());
        assertNotNull(result.getTimeRange());
    }

    @Test
    @DisplayName("Parse query with dimension: 按科室查询住院人次")
    void testParseQueryWithDimension() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("按科室查询住院人次");

        // When
        AiParseConfirmVO result = intentRecognizer.recognize(query);

        // Then
        assertNotNull(result);
        assertEquals("住院人次", result.getIndicatorName());
        assertTrue(result.getDimensions().contains("科室"));
    }

    @Test
    @DisplayName("Parse query with time range: 查询2026年第一季度医疗收入")
    void testParseQueryWithTimeRange() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("查询2026年第一季度医疗收入");

        // When
        AiParseConfirmVO result = intentRecognizer.recognize(query);

        // Then
        assertNotNull(result);
        assertEquals("医疗收入", result.getIndicatorName());
        assertEquals("2026年第一季度", result.getTimeRange());
    }

    @Test
    @DisplayName("Parse query with calculation: 计算床位使用率的同比")
    void testParseQueryWithCalculation() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("计算床位使用率的同比");

        // When
        AiParseConfirmVO result = intentRecognizer.recognize(query);

        // Then
        assertNotNull(result);
        assertEquals("床位使用率", result.getIndicatorName());
        assertEquals("同比", result.getCalculationType());
    }

    @Test
    @DisplayName("Parse complex query: 按科室和时间查询门诊收入的环比")
    void testParseComplexQuery() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("按科室和时间查询门诊收入的环比");

        // When
        AiParseConfirmVO result = intentRecognizer.recognize(query);

        // Then
        assertNotNull(result);
        assertEquals("门诊收入", result.getIndicatorName());
        assertTrue(result.getDimensions().contains("科室"));
        assertTrue(result.getDimensions().contains("时间"));
        assertEquals("环比", result.getCalculationType());
    }

    @Test
    @DisplayName("Parse invalid query returns error")
    void testParseInvalidQuery() {
        // Given
        AiQueryDTO query = new AiQueryDTO();
        query.setQueryText("随便乱写的查询语句");

        // When & Then
        assertThrows(AiParseException.class, () -> {
            intentRecognizer.recognize(query);
        });
    }
}