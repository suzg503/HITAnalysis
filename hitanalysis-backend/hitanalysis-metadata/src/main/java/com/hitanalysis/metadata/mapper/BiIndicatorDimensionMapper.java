package com.hitanalysis.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.metadata.entity.BiIndicatorDimension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Indicator dimension mapper
 */
@Mapper
public interface BiIndicatorDimensionMapper extends BaseMapper<BiIndicatorDimension> {

    @Select("SELECT * FROM bi_indicator_dimension WHERE zb_id = #{zbId} AND status = 1")
    BiIndicatorDimension selectByZbId(@Param("zbId") Long zbId);
}