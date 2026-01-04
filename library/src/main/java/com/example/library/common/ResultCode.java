package com.example.library.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举类
 * 定义系统所有响应状态码
 * 
 * @author Library Management System
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

  // ========== 通用状态码 ==========
  /** 成功 */
  SUCCESS(200, "操作成功"),

  /** 参数错误 */
  BAD_REQUEST(400, "请求参数错误"),

  /** 未授权 */
  UNAUTHORIZED(401, "未授权，请先登录"),

  /** 权限不足 */
  FORBIDDEN(403, "权限不足"),

  /** 资源不存在 */
  NOT_FOUND(404, "资源不存在"),

  /** 服务器错误 */
  SERVER_ERROR(500, "服务器内部错误"),

  // ========== 用户模块错误码 (1000-1999) ==========
  /** 用户不存在 */
  USER_NOT_FOUND(1001, "用户不存在"),

  /** 密码错误 */
  PASSWORD_ERROR(1002, "密码错误"),

  /** 账号已锁定 */
  ACCOUNT_LOCKED(1003, "账号已被锁定"),

  /** 账号已存在 */
  ACCOUNT_EXISTS(1004, "账号已存在"),

  /** 校园卡号已存在 */
  CARD_NUMBER_EXISTS(1005, "校园卡号已存在"),

  /** Token无效 */
  TOKEN_INVALID(1006, "Token无效或已过期"),

  // ========== 图书模块错误码 (2000-2999) ==========
  /** 图书不存在 */
  BOOK_NOT_FOUND(2001, "图书不存在"),

  /** 图书状态不可借阅 */
  BOOK_STATUS_UNAVAILABLE(2002, "图书状态不可借阅"),

  /** ISBN格式错误 */
  ISBN_FORMAT_ERROR(2003, "ISBN格式错误"),

  /** 馆藏号已存在 */
  COLLECTION_NUMBER_EXISTS(2004, "馆藏号已存在"),

  // ========== 借阅模块错误码 (3000-3999) ==========
  /** 用户存在超期图书 */
  USER_HAS_OVERDUE(3001, "用户存在超期图书，无法借阅"),

  /** 超过最大借阅数量 */
  EXCEED_MAX_BORROW_COUNT(3002, "超过最大借阅数量"),

  /** 借阅记录不存在 */
  BORROW_RECORD_NOT_FOUND(3003, "借阅记录不存在"),

  /** 图书未借出 */
  BOOK_NOT_BORROWED(3004, "该图书未借出，无法归还"),

  // ========== 系统配置模块错误码 (5000-5999) ==========
  /** 配置参数超出范围 */
  CONFIG_OUT_OF_RANGE(5001, "配置参数超出允许范围");

  /**
   * 响应码
   */
  private final Integer code;

  /**
   * 响应消息
   */
  private final String message;
}
