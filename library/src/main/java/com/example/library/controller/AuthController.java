package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.dto.LoginRequest;
import com.example.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  /**
   * 用户登录
   */
  @PostMapping("/login")
  public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
    try {
      Map<String, Object> data = authService.login(request.getAccount(), request.getPassword());
      return Result.success("登录成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 用户登出
   */
  @PostMapping("/logout")
  public Result<Void> logout() {
    return Result.success("退出成功", null);
  }
}
