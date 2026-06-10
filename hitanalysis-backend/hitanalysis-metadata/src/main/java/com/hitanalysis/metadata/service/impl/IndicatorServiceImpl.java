package com.hitanalysis.metadata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.common.result.PageResult;
import com.hitanalysis.metadata.entity.BiIndicator;
import com.hitanalysis.metadata.entity.BiIndicatorCategory;
import com.hitanalysis.metadata.entity.BiIndicatorDimension;
import com.hitanalysis.metadata.entity.BiIndicatorSystem;
import com.hitanalysis.metadata.mapper.BiIndicatorMapper;
import com.hitanalysis.metadata.mapper.BiIndicatorCategoryMapper;
import com.hitanalysis.metadata.mapper.BiIndicatorDimensionMapper;
import com.hitanalysis.metadata.mapper.BiIndicatorSystemMapper;
import com.hitanalysis.metadata.service.IndicatorService;
import com.hitanalysis.metadata.vo.IndicatorVO;
import com.hitanalysis.metadata.vo.IndicatorTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Indicator service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorServiceImpl implements IndicatorService {

    private final BiIndicatorMapper indicatorMapper;
    private final BiIndicatorSystemMapper systemMapper;
    private final BiIndicatorCategoryMapper categoryMapper;
    private final BiIndicatorDimensionMapper dimensionMapper;

    @Override
    public IndicatorVO getById(Long zbId) {
        BiIndicator indicator = indicatorMapper.selectById(zbId);
        if (indicator == null || indicator.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND, "指标不存在");
        }
        return convertToVo(indicator);
    }

    @Override
    public BiIndicator getByCode(String zbCode) {
        return indicatorMapper.selectByZbCode(zbCode);
    }

    @Override
    public PageResult<IndicatorVO> list(int pageNum, int pageSize, Long systemId, Long catId, String keyword) {
        LambdaQueryWrapper<BiIndicator> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BiIndicator::getIsDeleted, 0);

        if (systemId != null) {
            wrapper.eq(BiIndicator::getSystemId, systemId);
        }
        if (catId != null) {
            wrapper.eq(BiIndicator::getCatId, catId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(BiIndicator::getZbName, keyword);
        }

        wrapper.orderByAsc(BiIndicator::getSystemId, BiIndicator::getCatId, BiIndicator::getZbCode);

        Page<BiIndicator> page = indicatorMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<IndicatorVO> voList = page.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<IndicatorTreeVO> getIndicatorTree() {
        List<BiIndicatorSystem> systems = systemMapper.selectAllActive();
        List<BiIndicatorCategory> categories = categoryMapper.selectAllActive();
        List<BiIndicator> indicators = indicatorMapper.selectAllActive();

        // Build category map
        Map<Long, List<BiIndicatorCategory>> categoryBySystem = categories.stream()
                .collect(Collectors.groupingBy(BiIndicatorCategory::getSystemId));

        // Build indicator map
        Map<Long, List<BiIndicator>> indicatorByCategory = indicators.stream()
                .collect(Collectors.groupingBy(BiIndicator::getCatId));

        // Build tree
        List<IndicatorTreeVO> tree = new ArrayList<>();
        for (BiIndicatorSystem system : systems) {
            IndicatorTreeVO systemNode = new IndicatorTreeVO();
            systemNode.setSystemId(system.getSystemId());
            systemNode.setSystemCode(system.getSystemCode());
            systemNode.setSystemName(system.getSystemName());

            List<IndicatorTreeVO.CategoryNode> categoryNodes = new ArrayList<>();
            List<BiIndicatorCategory> systemCategories = categoryBySystem.getOrDefault(system.getSystemId(), new ArrayList<>());

            for (BiIndicatorCategory category : systemCategories) {
                IndicatorTreeVO.CategoryNode catNode = new IndicatorTreeVO.CategoryNode();
                catNode.setCatId(category.getCatId());
                catNode.setCatCode(category.getCatCode());
                catNode.setCatName(category.getCatName());

                List<IndicatorTreeVO.IndicatorNode> indicatorNodes = new ArrayList<>();
                List<BiIndicator> catIndicators = indicatorByCategory.getOrDefault(category.getCatId(), new ArrayList<>());

                for (BiIndicator indicator : catIndicators) {
                    IndicatorTreeVO.IndicatorNode indNode = new IndicatorTreeVO.IndicatorNode();
                    indNode.setZbId(indicator.getZbId());
                    indNode.setZbCode(indicator.getZbCode());
                    indNode.setZbName(indicator.getZbName());
                    indNode.setUnit(indicator.getUnit());
                    indicatorNodes.add(indNode);
                }

                catNode.setIndicators(indicatorNodes);
                categoryNodes.add(catNode);
            }

            systemNode.setCategories(categoryNodes);
            tree.add(systemNode);
        }

        return tree;
    }

    @Override
    public List<IndicatorVO> searchByKeyword(String keyword, int limit) {
        List<BiIndicator> indicators = indicatorMapper.searchByKeyword(keyword, limit);
        return indicators.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long create(BiIndicator indicator) {
        BiIndicator existing = indicatorMapper.selectByZbCode(indicator.getZbCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.REPORT_CONFIG_ERROR, "指标代码已存在");
        }

        indicatorMapper.insert(indicator);
        log.info("Indicator created: zbId={}, zbCode={}", indicator.getZbId(), indicator.getZbCode());
        return indicator.getZbId();
    }

    @Override
    @Transactional
    public void update(BiIndicator indicator) {
        BiIndicator existing = getById(indicator.getZbId());

        if (!existing.getZbCode().equals(indicator.getZbCode())) {
            BiIndicator byCode = indicatorMapper.selectByZbCode(indicator.getZbCode());
            if (byCode != null) {
                throw new BusinessException(ErrorCode.REPORT_CONFIG_ERROR, "指标代码已存在");
            }
        }

        indicator.setVersion(existing.getVersion() + 1);
        indicatorMapper.updateById(indicator);
        log.info("Indicator updated: zbId={}", indicator.getZbId());
    }

    @Override
    @Transactional
    public void delete(Long zbId) {
        BiIndicator indicator = indicatorMapper.selectById(zbId);
        if (indicator == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND, "指标不存在");
        }

        indicator.setIsDeleted(1);
        indicator.setDeletedAt(java.time.LocalDateTime.now());
        indicatorMapper.updateById(indicator);
        log.info("Indicator deleted (soft): zbId={}", zbId);
    }

    @Override
    public List<BiIndicator> listAllActive() {
        return indicatorMapper.selectAllActive();
    }

    private IndicatorVO convertToVo(BiIndicator indicator) {
        IndicatorVO vo = new IndicatorVO();
        vo.setZbId(indicator.getZbId());
        vo.setZbCode(indicator.getZbCode());
        vo.setZbName(indicator.getZbName());
        vo.setSystemId(indicator.getSystemId());
        vo.setCatId(indicator.getCatId());
        vo.setZbMeaning(indicator.getZbMeaning());
        vo.setZbCaliber(indicator.getZbCaliber());
        vo.setUnit(indicator.getUnit());
        vo.setIsRealTime(indicator.getIsRealTime());
        vo.setHasDecimal(indicator.getHasDecimal());
        vo.setConfigType(indicator.getConfigType());
        vo.setStatus(indicator.getStatus());
        vo.setCreateTime(indicator.getCreateTime());

        // Get system and category names
        BiIndicatorSystem system = systemMapper.selectById(indicator.getSystemId());
        if (system != null) {
            vo.setSystemName(system.getSystemName());
        }

        BiIndicatorCategory category = categoryMapper.selectById(indicator.getCatId());
        if (category != null) {
            vo.setCatName(category.getCatName());
        }

        // Get dimension config
        BiIndicatorDimension dimension = dimensionMapper.selectByZbId(indicator.getZbId());
        if (dimension != null) {
            vo.setFactTable(dimension.getFactTable());
            vo.setMeasureField(dimension.getMeasureField());
            vo.setDimensionField(dimension.getDimensionField());
            vo.setAggregationType(dimension.getAggregationType());
        }

        return vo;
    }
}