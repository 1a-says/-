package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.SystemConfig;
import com.example.library.mapper.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class SystemConfigService {

  private final SystemConfigMapper systemConfigMapper;

  /**
   * 获取系统配置
   */
  public Map<String, Object> getConfig() {
    Map<String, Object> config = new HashMap<>();

    SystemConfig teacherBorrowDays = getConfigByKey("teacher_borrow_days");
    SystemConfig studentBorrowDays = getConfigByKey("student_borrow_days");
    SystemConfig maxBorrowCount = getConfigByKey("max_borrow_count");

    config.put("teacherBorrowDays",
        teacherBorrowDays != null ? Integer.parseInt(teacherBorrowDays.getConfigValue()) : 90);
    config.put("studentBorrowDays",
        studentBorrowDays != null ? Integer.parseInt(studentBorrowDays.getConfigValue()) : 60);
    config.put("maxBorrowCount", maxBorrowCount != null ? Integer.parseInt(maxBorrowCount.getConfigValue()) : 5);

    Map<String, Object> defaultConfig = new HashMap<>();
    defaultConfig.put("teacherBorrowDays", 90);
    defaultConfig.put("studentBorrowDays", 60);
    defaultConfig.put("maxBorrowCount", 5);
    config.put("defaultConfig", defaultConfig);

    return config;
  }

  /**
   * 更新系统配置
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> updateConfig(Integer teacherBorrowDays, Integer studentBorrowDays,
      Integer maxBorrowCount) {
    // 参数范围验证
    if (teacherBorrowDays < 30 || teacherBorrowDays > 365) {
      throw new RuntimeException("教师借阅期限应在30-365天之间");
    }
    if (studentBorrowDays < 30 || studentBorrowDays > 365) {
      throw new RuntimeException("学生借阅期限应在30-365天之间");
    }
    if (maxBorrowCount < 1 || maxBorrowCount > 20) {
      throw new RuntimeException("单次最大借阅数应在1-20本之间");
    }

    updateConfigByKey("teacher_borrow_days", teacherBorrowDays.toString());
    updateConfigByKey("student_borrow_days", studentBorrowDays.toString());
    updateConfigByKey("max_borrow_count", maxBorrowCount.toString());

    Map<String, Object> result = new HashMap<>();
    result.put("teacherBorrowDays", teacherBorrowDays);
    result.put("studentBorrowDays", studentBorrowDays);
    result.put("maxBorrowCount", maxBorrowCount);

    return result;
  }

  /**
   * 恢复默认配置
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> resetConfig() {
    updateConfigByKey("teacher_borrow_days", "90");
    updateConfigByKey("student_borrow_days", "60");
    updateConfigByKey("max_borrow_count", "5");

    Map<String, Object> result = new HashMap<>();
    result.put("teacherBorrowDays", 90);
    result.put("studentBorrowDays", 60);
    result.put("maxBorrowCount", 5);

    return result;
  }

  private SystemConfig getConfigByKey(String key) {
    LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemConfig::getConfigKey, key);
    return systemConfigMapper.selectOne(wrapper);
  }

  private void updateConfigByKey(String key, String value) {
    LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SystemConfig::getConfigKey, key);
    SystemConfig config = systemConfigMapper.selectOne(wrapper);

    if (config != null) {
      config.setConfigValue(value);
      systemConfigMapper.updateById(config);
    }
  }
  
  /**
   * 根据配置键查询配置值
   */
  public String getConfigValueByKey(String configKey) {
    return systemConfigMapper.selectValueByKey(configKey);
  }
  
  /**
   * 根据配置键查询系统配置
   */
  public SystemConfig getConfigByKeyFull(String configKey) {
    return systemConfigMapper.selectByKey(configKey);
  }
  
  /**
   * 检查配置键是否存在
   */
  public boolean existsConfigKey(String configKey) {
    return systemConfigMapper.existsByKey(configKey) > 0;
  }
}
