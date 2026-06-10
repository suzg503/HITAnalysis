package com.hitanalysis.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.metadata.entity.BiIndicatorSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Indicator system mapper
 */
@Mapper
public interface BiIndicatorSystemMapper extends BaseMapper<BiIndicatorSystem> {

    @Select("SELECT * FROM bi_indicator_system WHERE status = 1 AND is_deleted = 0 ORDER BY sort_num")
    List<BiIndicatorSystem> selectAllActive();
}