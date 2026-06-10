package com.hitanalysis.metadata.service;

import com.hitanalysis.metadata.entity.BiIndicator;
import com.hitanalysis.metadata.mapper.BiIndicatorMapper;
import com.hitanalysis.metadata.vo.IndicatorTreeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * IndicatorService unit tests
 *
 * Tests indicator management operations including:
 * - Indicator creation
 * - Indicator tree retrieval
 * - Indicator update
 * - Indicator deletion
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IndicatorService Tests")
class IndicatorServiceTest {

    @Mock
    private BiIndicatorMapper indicatorMapper;

    @Mock
    private BiIndicatorSystemMapper systemMapper;

    @Mock
    private BiIndicatorCategoryMapper categoryMapper;

    @InjectMocks
    private IndicatorServiceImpl indicatorService;

    private BiIndicator testIndicator;
    private BiIndicatorSystem testSystem;
    private BiIndicatorCategory testCategory;

    @BeforeEach
    void setUp() {
        // Setup test data
        testSystem = new BiIndicatorSystem();
        testSystem.setSystemId(1L);
        testSystem.setSystemCode("YWTX");
        testSystem.setSystemName("业务体系");

        testCategory = new BiIndicatorCategory();
        testCategory.setCatId(1L);
        testCategory.setCatCode("MZFX");
        testCategory.setCatName("门诊分析");

        testIndicator = new BiIndicator();
        testIndicator.setZbId(1L);
        testIndicator.setZbCode("A001");
        testIndicator.setZbName("门急诊人次");
        testIndicator.setSystemId(1L);
        testIndicator.setCatId(1L);
        testIndicator.setParentZbId(0L);
        testIndicator.setStatus(1);
    }

    @Test
    @DisplayName("Create indicator successfully")
    void testCreateIndicator_Success() {
        // Given
        when(indicatorMapper.checkCodeExists(anyString(), any())).thenReturn(0);
        when(systemMapper.selectById(anyLong())).thenReturn(testSystem);
        when(categoryMapper.selectById(anyLong())).thenReturn(testCategory);
        when(indicatorMapper.insertIndicator(any(BiIndicator.class))).thenReturn(1);

        BiIndicator newIndicator = new BiIndicator();
        newIndicator.setZbCode("A002");
        newIndicator.setZbName("门诊人次");
        newIndicator.setSystemId(1L);
        newIndicator.setCatId(1L);

        // When
        Long zbId = indicatorService.createIndicator(newIndicator);

        // Then
        assertNotNull(zbId);
        verify(indicatorMapper, times(1)).insertIndicator(any(BiIndicator.class));
    }

    @Test
    @DisplayName("Create indicator with duplicate code fails")
    void testCreateIndicator_DuplicateCode() {
        // Given
        when(indicatorMapper.checkCodeExists(anyString(), any())).thenReturn(1);

        BiIndicator newIndicator = new BiIndicator();
        newIndicator.setZbCode("A001");
        newIndicator.setZbName("重复指标");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            indicatorService.createIndicator(newIndicator);
        });

        verify(indicatorMapper, never()).insertIndicator(any(BiIndicator.class));
    }

    @Test
    @DisplayName("Get indicator tree successfully")
    void testGetIndicatorTree_Success() {
        // Given
        when(indicatorMapper.selectIndicatorTree(anyLong())).thenReturn(List.of(testIndicator));

        // When
        List<IndicatorTreeVO> tree = indicatorService.getIndicatorTree(1L);

        // Then
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        verify(indicatorMapper, times(1)).selectIndicatorTree(anyLong());
    }

    @Test
    @DisplayName("Update indicator successfully")
    void testUpdateIndicator_Success() {
        // Given
        when(indicatorMapper.selectById(anyLong())).thenReturn(testIndicator);
        when(indicatorMapper.updateIndicator(any(BiIndicator.class))).thenReturn(1);

        testIndicator.setZbName("更新后的指标名称");

        // When
        indicatorService.updateIndicator(testIndicator);

        // Then
        verify(indicatorMapper, times(1)).updateIndicator(any(BiIndicator.class));
    }

    @Test
    @DisplayName("Update non-existent indicator fails")
    void testUpdateIndicator_IndicatorNotFound() {
        // Given
        when(indicatorMapper.selectById(anyLong())).thenReturn(null);

        BiIndicator indicator = new BiIndicator();
        indicator.setZbId(999L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            indicatorService.updateIndicator(indicator);
        });

        verify(indicatorMapper, never()).updateIndicator(any(BiIndicator.class));
    }

    @Test
    @DisplayName("Delete indicator successfully")
    void testDeleteIndicator_Success() {
        // Given
        when(indicatorMapper.selectById(anyLong())).thenReturn(testIndicator);
        when(indicatorMapper.softDeleteById(anyLong())).thenReturn(1);

        // When
        indicatorService.deleteIndicator(1L);

        // Then
        verify(indicatorMapper, times(1)).softDeleteById(anyLong());
    }

    @Test
    @DisplayName("Get indicator list with filters")
    void testGetIndicatorList_Success() {
        // Given
        when(indicatorMapper.selectIndicatorList(any())).thenReturn(List.of(testIndicator));

        // When
        List<BiIndicator> indicators = indicatorService.getIndicatorList(1L, 1L, null, null, 1);

        // Then
        assertNotNull(indicators);
        assertFalse(indicators.isEmpty());
        verify(indicatorMapper, times(1)).selectIndicatorList(any());
    }
}