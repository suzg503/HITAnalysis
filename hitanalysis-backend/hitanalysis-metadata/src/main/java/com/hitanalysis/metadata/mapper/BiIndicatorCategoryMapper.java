package com.hitanalysis.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.metadata.entity.BiIndicatorCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Indicator category mapper
 */
@Mapper
public interface BiIndicatorCategoryMapper extends BaseMapper<BiIndicatorCategory> {

    @Select("SELECT * FROM bi_indicator_category WHERE system_id = #{systemId} AND status = 1 AND is_deleted = 0 ORDER BY sort_num")
    List<BiIndicatorCategory> selectBySystemId(@Param("systemId") Long systemId);

    @Select("SELECT * FROM bi_indicator_category WHERE status = 1 AND is_deleted = 0 ORDER BY system_id, sort_num")
    List<BiIndicatorCategory> selectAllActive();
}