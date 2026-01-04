package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 图书 Mapper 接口
 * 
 * @author Library Management System
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

  /**
   * 根据ISBN查询图书
   * 
   * @param isbn ISBN号
   * @return 图书信息
   */
  @Select("SELECT * FROM tb_book WHERE isbn = #{isbn} AND deleted = 0")
  List<Book> selectByIsbn(@Param("isbn") String isbn);

  /**
   * 根据书名模糊查询图书
   * 
   * @param title 书名
   * @return 图书列表
   */
  @Select("SELECT * FROM tb_book WHERE title LIKE CONCAT('%', #{title}, '%') AND deleted = 0")
  List<Book> selectByTitle(@Param("title") String title);

  /**
   * 根据馆藏号查询图书
   * 
   * @param collectionNumber 馆藏号
   * @return 图书信息
   */
  @Select("SELECT * FROM tb_book WHERE collection_number = #{collectionNumber} AND deleted = 0")
  Book selectByCollectionNumber(@Param("collectionNumber") String collectionNumber);

  /**
   * 根据状态统计图书数量
   * 
   * @param status 状态
   * @return 数量
   */
  @Select("SELECT COUNT(*) FROM tb_book WHERE status = #{status} AND deleted = 0")
  int countByStatus(@Param("status") String status);

  /**
   * 获取图书状态统计
   * 
   * @return 状态统计信息
   */
  @Select("SELECT status, COUNT(*) as count FROM tb_book WHERE deleted = 0 GROUP BY status")
  List<Map<String, Object>> selectStatusStatistics();

  /**
   * 检查馆藏号是否存在
   * 
   * @param collectionNumber 馆藏号
   * @return 是否存在
   */
  @Select("SELECT COUNT(*) FROM tb_book WHERE collection_number = #{collectionNumber} AND deleted = 0")
  int existsByCollectionNumber(@Param("collectionNumber") String collectionNumber);
}
