package com.example.library.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 图书入库请求DTO
 * 
 * @author Library Management System
 */
@Data
public class BookAddRequest {

  @NotBlank(message = "ISBN号不能为空")
  private String isbn;

  @NotBlank(message = "书名不能为空")
  private String title;

  @NotBlank(message = "作者不能为空")
  private String author;

  @NotBlank(message = "出版社不能为空")
  private String publisher;

  @NotBlank(message = "馆藏位置不能为空")
  private String location;
}
