package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置 Mapper 接口
 * 
 * @author Library Management System
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @Select("SELECT config_value FROM tb_system_config WHERE config_key = #{configKey}")
    String selectValueByKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置键查询系统配置
     * 
     * @param configKey 配置键
     * @return 系统配置
     */
    @Select("SELECT * FROM tb_system_config WHERE config_key = #{configKey}")
    SystemConfig selectByKey(@Param("configKey") String configKey);
    
    /**
     * 检查配置键是否存在
     * 
     * @param configKey 配置键
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM tb_system_config WHERE config_key = #{configKey}")
    int existsByKey(@Param("configKey") String configKey);
}
