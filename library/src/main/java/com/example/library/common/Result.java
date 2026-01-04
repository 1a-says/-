package com.example.library.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 所有接口统一返回此格式
 * 
 * @author Library Management System
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 响应码（200-成功，400-参数错误，401-未授权，403-禁止访问，404-资源不存在，500-服务器错误）
   */
  private Integer code;

  /**
   * 响应消息
   */
  private String message;

  /**
   * 响应数据
   */
  private T data;

  /**
   * 成功响应（无数据）
   */
  public static <T> Result<T> success() {
    return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
  }

  /**
   * 成功响应（带数据）
   */
  public static <T> Result<T> success(T data) {
    return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
  }

  /**
   * 成功响应（自定义消息）
   */
  public static <T> Result<T> success(String message, T data) {
    return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
  }

  /**
   * 失败响应（默认错误）
   */
  public static <T> Result<T> error() {
    return new Result<>(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage(), null);
  }

  /**
   * 失败响应（自定义消息）
   */
  public static <T> Result<T> error(String message) {
    return new Result<>(ResultCode.SERVER_ERROR.getCode(), message, null);
  }

  /**
   * 失败响应（指定错误码）
   */
  public static <T> Result<T> error(ResultCode resultCode) {
    return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
  }

  /**
   * 失败响应（指定错误码和自定义消息）
   */
  public static <T> Result<T> error(ResultCode resultCode, String message) {
    return new Result<>(resultCode.getCode(), message, null);
  }

  /**
   * 自定义响应
   */
  public static <T> Result<T> build(Integer code, String message, T data) {
    return new Result<>(code, message, data);
  }
}
