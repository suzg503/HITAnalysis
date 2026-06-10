package com.hitanalysis.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Menu mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "INNER JOIN sys_user u ON u.role_id = rm.role_id " +
            "WHERE u.user_id = #{userId} AND m.status = 1 AND m.is_deleted = 0 " +
            "ORDER BY m.menu_level, m.sort_num")
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} AND m.status = 1 AND m.is_deleted = 0 " +
            "ORDER BY m.menu_level, m.sort_num")
    List<SysMenu> selectMenusByRoleId(@Param("roleId") Long roleId);
}