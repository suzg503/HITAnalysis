package com.hitanalysis.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hitanalysis.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * User mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT u.*, r.role_name, r.role_code FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.role_id " +
            "WHERE u.username = #{username} AND u.is_deleted = 0")
    SysUser selectByUsernameWithRole(@Param("username") String username);

    @Select("SELECT dept_code FROM sys_user_dept WHERE user_id = #{userId}")
    List<String> selectUserDeptCodes(@Param("userId") Long userId);

    @Select("SELECT hospital_id FROM sys_user_hospital WHERE user_id = #{userId}")
    List<Long> selectUserHospitalIds(@Param("userId") Long userId);
}