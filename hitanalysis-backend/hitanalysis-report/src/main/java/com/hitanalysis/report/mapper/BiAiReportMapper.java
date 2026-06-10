package com.hitanalysis.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.report.entity.BiAiReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI report mapper
 */
@Mapper
public interface BiAiReportMapper extends BaseMapper<BiAiReport> {

    @Select("SELECT * FROM bi_ai_report WHERE created_by = #{userId} AND status != 'deleted' ORDER BY create_time DESC")
    List<BiAiReport> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM bi_ai_report WHERE folder_id = #{folderId} AND status != 'deleted' ORDER BY create_time DESC")
    List<BiAiReport> selectByFolderId(@Param("folderId") Long folderId);

    @Select("SELECT * FROM bi_ai_report WHERE visibility = #{visibility} AND status = 'published' ORDER BY create_time DESC")
    List<BiAiReport> selectByVisibility(@Param("visibility") String visibility);
}