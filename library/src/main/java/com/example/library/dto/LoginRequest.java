package com.example.library.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author Library Management System
 */
@Data
public class LoginRequest {

  @NotBlank(message = "账号不能为空")
  private String account;

  @NotBlank(message = "密码不能为空")
  private String password;
}
