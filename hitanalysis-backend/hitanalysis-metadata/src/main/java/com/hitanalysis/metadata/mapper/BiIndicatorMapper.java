package com.hitanalysis.metadata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.metadata.entity.BiIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Indicator mapper
 */
@Mapper
public interface BiIndicatorMapper extends BaseMapper<BiIndicator> {

    @Select("SELECT * FROM bi_indicator WHERE status = 1 AND is_deleted = 0 ORDER BY system_id, cat_id, zb_code")
    List<BiIndicator> selectAllActive();

    @Select("SELECT * FROM bi_indicator WHERE system_id = #{systemId} AND status = 1 AND is_deleted = 0")
    List<BiIndicator> selectBySystemId(@Param("systemId") Long systemId);

    @Select("SELECT * FROM bi_indicator WHERE cat_id = #{catId} AND status = 1 AND is_deleted = 0")
    List<BiIndicator> selectByCategoryId(@Param("catId") Long catId);

    @Select("SELECT * FROM bi_indicator WHERE zb_code = #{zbCode} AND is_deleted = 0 LIMIT 1")
    BiIndicator selectByZbCode(@Param("zbCode") String zbCode);

    @Select("SELECT * FROM bi_indicator WHERE zb_name LIKE #{keyword} AND status = 1 AND is_deleted = 0 LIMIT #{limit}")
    List<BiIndicator> searchByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}