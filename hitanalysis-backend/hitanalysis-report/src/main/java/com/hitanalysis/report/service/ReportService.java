package com.hitanalysis.report.service;

import com.hitanalysis.report.dto.AiReportSaveDTO;
import com.hitanalysis.report.entity.BiAiReport;
import com.hitanalysis.report.entity.BiStandardReport;
import com.hitanalysis.report.vo.ReportDataVO;
import com.hitanalysis.report.vo.ReportTreeVO;

import java.util.List;

/**
 * Report service
 */
public interface ReportService {

    /**
     * Get standard report by ID
     */
    BiStandardReport getStandardById(Long reportId);

    /**
     * Get standard report tree
     */
    List<ReportTreeVO> getStandardReportTree();

    /**
     * Query report data (with permission filtering D3)
     */
    ReportDataVO queryReportData(Long reportId, Long userId, String timeRange);

    /**
     * Get user's AI reports
     */
    List<BiAiReport> getUserAiReports(Long userId);

    /**
     * Get AI report by ID
     */
    BiAiReport getAiById(Long reportId);

    /**
     * Save AI report (D1 - saves Config JSON)
     */
    Long saveAiReport(AiReportSaveDTO dto, Long userId);

    /**
     * Update AI report
     */
    void updateAiReport(Long reportId, AiReportSaveDTO dto);

    /**
     * Delete AI report
     */
    void deleteAiReport(Long reportId);

    /**
     * Change AI report visibility
     */
    void changeVisibility(Long reportId, String visibility);
}