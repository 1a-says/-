package com.example.library.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件等
 * 
 * @author Library Management System
 */
@Configuration
public class MybatisPlusConfig {

  /**
   * 配置 MyBatis-Plus 拦截器
   * 主要用于分页功能
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

    // 添加分页插件
    PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
    // 设置请求的页面大于最大页后操作，true调回到首页，false继续请求
    paginationInterceptor.setOverflow(false);
    // 设置最大单页限制数量，默认500条，-1不受限制
    paginationInterceptor.setMaxLimit(500L);

    interceptor.addInnerInterceptor(paginationInterceptor);

    return interceptor;
  }
}
