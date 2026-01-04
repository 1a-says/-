package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class SystemConfigController {

  private final SystemConfigService systemConfigService;

  /**
   * 获取系统配置
   */
  @GetMapping
  public Result<Map<String, Object>> getConfig() {
    try {
      Map<String, Object> data = systemConfigService.getConfig();
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新系统配置
   */
  @PutMapping
  public Result<Map<String, Object>> updateConfig(@RequestBody Map<String, Integer> request) {
    try {
      Integer teacherBorrowDays = request.get("teacherBorrowDays");
      Integer studentBorrowDays = request.get("studentBorrowDays");
      Integer maxBorrowCount = request.get("maxBorrowCount");

      Map<String, Object> data = systemConfigService.updateConfig(teacherBorrowDays, studentBorrowDays, maxBorrowCount);
      return Result.success("参数保存成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 恢复默认配置
   */
  @PostMapping("/reset")
  public Result<Map<String, Object>> resetConfig() {
    try {
      Map<String, Object> data = systemConfigService.resetConfig();
      return Result.success("已恢复默认参数", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
