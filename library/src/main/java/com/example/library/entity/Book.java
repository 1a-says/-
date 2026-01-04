package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图书实体类
 * 对应数据库表 tb_book
 * 
 * @author Library Management System
 */
@Data
@TableName("tb_book")
public class Book implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 馆藏号（TS+时间戳+随机数）
   */
  private String collectionNumber;

  /**
   * ISBN号
   */
  private String isbn;

  /**
   * 书名
   */
  private String title;

  /**
   * 作者
   */
  private String author;

  /**
   * 出版社
   */
  private String publisher;

  /**
   * 馆藏位置（如：A区-3架-2层）
   */
  private String location;

  /**
   * 图书状态（可借阅/已借出/遗失/损坏/维护中）
   */
  private String status;

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
