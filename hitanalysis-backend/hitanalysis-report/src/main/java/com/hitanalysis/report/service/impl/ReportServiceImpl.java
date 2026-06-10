package com.hitanalysis.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hitanalysis.common.constant.ErrorCode;
import com.hitanalysis.common.exception.BusinessException;
import com.hitanalysis.report.dto.AiReportSaveDTO;
import com.hitanalysis.report.entity.BiAiReport;
import com.hitanalysis.report.entity.BiStandardReport;
import com.hitanalysis.report.mapper.BiAiReportMapper;
import com.hitanalysis.report.mapper.BiStandardReportMapper;
import com.hitanalysis.report.service.ReportService;
import com.hitanalysis.report.vo.ReportDataVO;
import com.hitanalysis.report.vo.ReportTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Report service implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final BiStandardReportMapper standardReportMapper;
    private final BiAiReportMapper aiReportMapper;

    @Override
    public BiStandardReport getStandardById(Long reportId) {
        BiStandardReport report = standardReportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        return report;
    }

    @Override
    public List<ReportTreeVO> getStandardReportTree() {
        List<BiStandardReport> reports = standardReportMapper.selectAllActive();
        return buildReportTree(reports);
    }

    @Override
    public ReportDataVO queryReportData(Long reportId, Long userId, String timeRange) {
        BiStandardReport report = getStandardById(reportId);

        // D3 - Permission filtering would be injected here
        // For MVP, return placeholder data
        ReportDataVO result = new ReportDataVO();
        result.setReportId(reportId);
        result.setReportName(report.getReportName());
        result.setMetrics(List.of(report.getZbNames()));
        result.setTimeRange(timeRange);
        result.setDataRows(List.of(Map.of("placeholder", "data")));
        result.setTotal(0);
        result.setQueryTime(LocalDateTime.now());
        result.setQueryDuration(100);

        // D2 - Include data source and calculation logic
        result.setDataSource("ClickHouse - " + timeRange);
        result.setCalculationLogic(report.getZbIds() + " - 聚合方式: SUM");

        log.info("Report queried: reportId={}, userId={}, timeRange={}", reportId, userId, timeRange);
        return result;
    }

    @Override
    public List<BiAiReport> getUserAiReports(Long userId) {
        return aiReportMapper.selectByUserId(userId);
    }

    @Override
    public BiAiReport getAiById(Long reportId) {
        BiAiReport report = aiReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_FOUND);
        }
        return report;
    }

    @Override
    @Transactional
    public Long saveAiReport(AiReportSaveDTO dto, Long userId) {
        BiAiReport report = new BiAiReport();
        report.setReportName(dto.getReportName());
        report.setConfigJson(dto.getConfigJson()); // D1 - Save Config JSON, not SQL
        report.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "private");
        report.setFolderId(dto.getFolderId());
        report.setZbIds(dto.getZbIds());
        report.setStatus(dto.getStatus() != null ? dto.getStatus() : "saved");
        report.setCreatedBy(userId);

        aiReportMapper.insert(report);
        log.info("AI Report saved: reportId={}, userId={}", report.getReportId(), userId);
        return report.getReportId();
    }

    @Override
    @Transactional
    public void updateAiReport(Long reportId, AiReportSaveDTO dto) {
        BiAiReport report = getAiById(reportId);

        if (dto.getReportName() != null) {
            report.setReportName(dto.getReportName());
        }
        if (dto.getConfigJson() != null) {
            report.setConfigJson(dto.getConfigJson());
        }
        if (dto.getVisibility() != null) {
            report.setVisibility(dto.getVisibility());
        }
        if (dto.getStatus() != null) {
            report.setStatus(dto.getStatus());
        }

        aiReportMapper.updateById(report);
        log.info("AI Report updated: reportId={}", reportId);
    }

    @Override
    @Transactional
    public void deleteAiReport(Long reportId) {
        BiAiReport report = getAiById(reportId);
        report.setStatus("deleted");
        aiReportMapper.updateById(report);
        log.info("AI Report deleted: reportId={}", reportId);
    }

    @Override
    @Transactional
    public void changeVisibility(Long reportId, String visibility) {
        BiAiReport report = getAiById(reportId);
        report.setVisibility(visibility);
        aiReportMapper.updateById(report);
        log.info("AI Report visibility changed: reportId={}, visibility={}", reportId, visibility);
    }

    private List<ReportTreeVO> buildReportTree(List<BiStandardReport> reports) {
        Map<Long, List<BiStandardReport>> byParent = reports.stream()
                .collect(Collectors.groupingBy(BiStandardReport::getParentId));

        List<ReportTreeVO> rootReports = new ArrayList<>();
        List<BiStandardReport> rootList = byParent.getOrDefault(0L, new ArrayList<>());

        for (BiStandardReport root : rootList) {
            rootReports.add(convertToTreeVo(root, byParent));
        }

        return rootReports;
    }

    private ReportTreeVO convertToTreeVo(BiStandardReport report, Map<Long, List<BiStandardReport>> byParent) {
        ReportTreeVO vo = new ReportTreeVO();
        vo.setReportId(report.getReportId());
        vo.setReportName(report.getReportName());
        vo.setReportCode(report.getReportCode());
        vo.setReportLevel(report.getReportLevel());
        vo.setReportUrl(report.getReportUrl());
        vo.setZbNames(report.getZbNames());

        List<BiStandardReport> children = byParent.getOrDefault(report.getReportId(), new ArrayList<>());
        if (!children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(c -> convertToTreeVo(c, byParent))
                    .collect(Collectors.toList()));
        } else {
            vo.setChildren(new ArrayList<>());
        }

        return vo;
    }
}