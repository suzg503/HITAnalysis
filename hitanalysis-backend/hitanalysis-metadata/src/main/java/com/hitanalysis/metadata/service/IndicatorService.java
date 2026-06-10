package com.hitanalysis.metadata.service;

import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.metadata.entity.BiIndicator;
import com.hitanalysis.metadata.vo.IndicatorVO;
import com.hitanalysis.metadata.vo.IndicatorTreeVO;

import java.util.List;

/**
 * Indicator service
 */
public interface IndicatorService {

    /**
     * Get indicator by ID
     */
    IndicatorVO getById(Long zbId);

    /**
     * Get indicator by code
     */
    BiIndicator getByCode(String zbCode);

    /**
     * List indicators with pagination
     */
    PageResult<IndicatorVO> list(int pageNum, int pageSize, Long systemId, Long catId, String keyword);

    /**
     * Get indicator tree structure
     */
    List<IndicatorTreeVO> getIndicatorTree();

    /**
     * Search indicators by keyword (for AI matching)
     */
    List<IndicatorVO> searchByKeyword(String keyword, int limit);

    /**
     * Create indicator
     */
    Long create(BiIndicator indicator);

    /**
     * Update indicator
     */
    void update(BiIndicator indicator);

    /**
     * Delete indicator (soft delete)
     */
    void delete(Long zbId);

    /**
     * List all active indicators
     */
    List<BiIndicator> listAllActive();
}