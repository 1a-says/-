package com.example.library.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 密码工具类
 * 用于密码加密
 * 
 * @author Library Management System
 */
public class PasswordUtil {

  /**
   * SHA256 加密
   * 
   * @param password 原始密码
   * @return 加密后的密码
   */
  public static String sha256(String password) {
    return DigestUtils.sha256Hex(password);
  }

  /**
   * 生成初始密码
   * 规则：学号/工号长度 >= 6位，取后6位；否则使用全部字符
   * 
   * @param accountNumber 学号/工号
   * @return 初始密码
   */
  public static String generateInitialPassword(String accountNumber) {
    if (accountNumber == null || accountNumber.isEmpty()) {
      return "";
    }

    if (accountNumber.length() >= 6) {
      return accountNumber.substring(accountNumber.length() - 6);
    } else {
      return accountNumber;
    }
  }
}
