package com.hitanalysis.ai.service;

import com.hitanalysis.ai.dto.AiConfigDTO;
import com.hitanalysis.ai.dto.AiQueryDTO;
import com.hitanalysis.ai.vo.AiInsightVO;
import com.hitanalysis.ai.vo.AiParseConfirmVO;
import com.hitanalysis.ai.vo.AiPreviewVO;

/**
 * AI service (Text-to-Config implementation)
 */
public interface AiService {

    /**
     * Create AI session
     */
    Long createSession(Long userId);

    /**
     * Submit natural language query (Step 1-4 of Text-to-Config)
     */
    AiParseConfirmVO submitQuery(AiQueryDTO dto);

    /**
     * Get parse result for confirmation (D1 - user must confirm)
     */
    AiParseConfirmVO getParseResult(Long parseId);

    /**
     * Confirm parse result and execute (Step 5-7)
     */
    AiPreviewVO confirmAndExecute(Long parseId, Long userId);

    /**
     * Refine query (追问)
     */
    AiParseConfirmVO refineQuery(Long sessionId, String refineText);

    /**
     * Generate insight (D2 - based on data)
     */
    AiInsightVO generateInsight(Long previewId);

    /**
     * Check AI permission (D3)
     */
    boolean checkAiPermission(Long userId);

    /**
     * Get user's daily query count
     */
    int getDailyQueryCount(Long userId);
}