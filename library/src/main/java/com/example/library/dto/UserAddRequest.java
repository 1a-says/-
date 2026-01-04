package com.example.library.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 用户录入请求DTO
 * 
 * @author Library Management System
 */
@Data
public class UserAddRequest {

  @NotBlank(message = "学号/工号不能为空")
  private String accountNumber;

  @NotBlank(message = "姓名不能为空")
  private String name;

  @NotBlank(message = "身份不能为空")
  private String identity;

  @NotBlank(message = "校园卡号不能为空")
  private String cardNumber;
}
