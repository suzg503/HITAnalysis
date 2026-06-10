package com.hitanalysis.common.exception;

import com.hitanalysis.common.constant.ErrorCode;

/**
 * Permission denied exception (D3 decision implementation)
 *
 * Thrown when user attempts to access data beyond their permission scope
 */
public class PermissionDeniedException extends BusinessException {

    public PermissionDeniedException() {
        super(ErrorCode.PERMISSION_DENIED);
    }

    public PermissionDeniedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PermissionDeniedException(String message) {
        super(ErrorCode.PERMISSION_DENIED, message);
    }

    /**
     * Cross-hospital permission denied (D3)
     */
    public static PermissionDeniedException crossHospital() {
        return new PermissionDeniedException(ErrorCode.PERMISSION_CROSS_HOSPITAL_DENIED);
    }

    /**
     * AI permission denied
     */
    public static PermissionDeniedException aiDenied() {
        return new PermissionDeniedException(ErrorCode.PERMISSION_AI_DENIED);
    }

    /**
     * Data scope permission denied
     */
    public static PermissionDeniedException dataDenied() {
        return new PermissionDeniedException(ErrorCode.PERMISSION_DATA_DENIED);
    }
}