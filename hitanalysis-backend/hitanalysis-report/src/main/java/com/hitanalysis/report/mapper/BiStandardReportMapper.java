package com.hitanalysis.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.report.entity.BiStandardReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Standard report mapper
 */
@Mapper
public interface BiStandardReportMapper extends BaseMapper<BiStandardReport> {

    @Select("SELECT * FROM bi_standard_report WHERE status = 1 AND is_deleted = 0 ORDER BY report_level, sort_num")
    List<BiStandardReport> selectAllActive();

    @Select("SELECT * FROM bi_standard_report WHERE parent_id = #{parentId} AND status = 1 AND is_deleted = 0 ORDER BY sort_num")
    List<BiStandardReport> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM bi_standard_report WHERE report_level = #{level} AND status = 1 AND is_deleted = 0 ORDER BY sort_num")
    List<BiStandardReport> selectByLevel(@Param("level") int level);

    @Select("SELECT * FROM bi_standard_report WHERE system_id = #{systemId} AND status = 1 AND is_deleted = 0 ORDER BY sort_num")
    List<BiStandardReport> selectBySystemId(@Param("systemId") Long systemId);
}