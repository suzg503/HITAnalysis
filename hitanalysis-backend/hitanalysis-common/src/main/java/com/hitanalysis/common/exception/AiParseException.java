package com.hitanalysis.common.exception;

import com.hitanalysis.common.constant.ErrorCode;

/**
 * AI parse exception (D1 decision implementation)
 *
 * Thrown when AI fails to parse natural language to config JSON
 */
public class AiParseException extends BusinessException {

    public AiParseException() {
        super(ErrorCode.AI_PARSE_ERROR);
    }

    public AiParseException(String message) {
        super(ErrorCode.AI_PARSE_ERROR, message);
    }

    public AiParseException(ErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Indicator not matched in RAG lookup
     */
    public static AiParseException indicatorNotMatched(String indicatorName) {
        return new AiParseException("无法识别指标: " + indicatorName + "，请检查是否存在于指标库中");
    }

    /**
     * Dimension not matched in RAG lookup
     */
    public static AiParseException dimensionNotMatched(String dimensionName) {
        return new AiParseException("无法识别维度: " + dimensionName + "，请检查是否存在于维度库中");
    }
}