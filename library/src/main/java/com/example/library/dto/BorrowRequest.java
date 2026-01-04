package com.example.library.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 借阅请求DTO
 * 
 * @author Library Management System
 */
@Data
public class BorrowRequest {

  @NotBlank(message = "校园卡号不能为空")
  private String cardNumber;

  @NotEmpty(message = "馆藏号列表不能为空")
  private List<String> collectionNumbers;

  @NotBlank(message = "操作员账号不能为空")
  private String operator;
}
