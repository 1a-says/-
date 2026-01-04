package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图书状态历史实体类
 * 对应数据库表 tb_book_status_history
 * 
 * @author Library Management System
 */
@Data
@TableName("tb_book_status_history")
public class BookStatusHistory implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 馆藏号
   */
  private String collectionNumber;

  /**
   * 状态
   */
  private String status;

  /**
   * 操作员账号
   */
  private String operator;

  /**
   * 操作时间
   */
  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime operateTime;
}
