package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表 tb_user
 * 
 * @author Library Management System
 */
@Data
@TableName("tb_user")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 学号/工号
   */
  private String accountNumber;

  /**
   * 姓名
   */
  private String name;

  /**
   * 身份（教师/学生）
   */
  private String identity;

  /**
   * 校园卡号
   */
  private String cardNumber;

  /**
   * 密码（SHA256加密）
   */
  private String password;

  /**
   * 初始密码（明文，用于重置）
   */
  private String initialPassword;

  /**
   * 角色（admin/user）
   */
  private String role;

  /**
   * 账号状态（正常/锁定/停用）
   */
  private String status;

  /**
   * 锁定时间
   */
  private LocalDateTime lockTime;

  /**
   * 登录失败次数
   */
  private Integer loginFailCount;

  /**
   * 创建时间
   */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  /**
   * 逻辑删除标识（0-未删除，1-已删除）
   */
  @TableLogic
  private Integer deleted;
}
