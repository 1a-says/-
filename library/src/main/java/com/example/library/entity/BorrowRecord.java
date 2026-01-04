package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 借阅记录实体类
 * 对应数据库表 tb_borrow_record
 * 
 * @author Library Management System
 */
@Data
@TableName("tb_borrow_record")
public class BorrowRecord implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 借阅记录ID（BR+时间戳）
   */
  private String recordId;

  /**
   * 校园卡号
   */
  private String cardNumber;

  /**
   * 用户姓名
   */
  private String userName;

  /**
   * 用户身份
   */
  private String userIdentity;

  /**
   * 学号/工号
   */
  private String accountNumber;

  /**
   * 馆藏号
   */
  private String collectionNumber;

  /**
   * 图书书名
   */
  private String bookTitle;

  /**
   * 图书作者
   */
  private String bookAuthor;

  /**
   * 借阅日期
   */
  private LocalDateTime borrowDate;

  /**
   * 应还日期
   */
  private LocalDateTime dueDate;

  /**
   * 归还日期
   */
  private LocalDateTime returnDate;

  /**
   * 超期天数
   */
  private Integer overdueDays;

  /**
   * 借阅状态（借阅中/已归还）
   */
  private String status;

  /**
   * 操作员账号
   */
  private String operator;

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
}
