package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 对应数据库表 tb_system_config
 * 
 * @author Library Management System
 */
@Data
@TableName("tb_system_config")
public class SystemConfig implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 主键ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 配置键
   */
  private String configKey;

  /**
   * 配置值
   */
  private String configValue;

  /**
   * 配置描述
   */
  private String description;

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
