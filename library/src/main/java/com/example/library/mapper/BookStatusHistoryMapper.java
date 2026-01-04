package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.BookStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 图书状态历史 Mapper 接口
 * 
 * @author Library Management System
 */
@Mapper
public interface BookStatusHistoryMapper extends BaseMapper<BookStatusHistory> {
    
    /**
     * 根据馆藏号查询状态历史
     * 
     * @param collectionNumber 馆藏号
     * @return 状态历史列表
     */
    @Select("SELECT * FROM tb_book_status_history WHERE collection_number = #{collectionNumber} ORDER BY operate_time DESC")
    List<BookStatusHistory> selectByCollectionNumber(@Param("collectionNumber") String collectionNumber);
    
    /**
     * 查询最近的状态历史
     * 
     * @param collectionNumber 馆藏号
     * @param limit 限制数量
     * @return 状态历史列表
     */
    @Select("SELECT * FROM tb_book_status_history WHERE collection_number = #{collectionNumber} ORDER BY operate_time DESC LIMIT #{limit}")
    List<BookStatusHistory> selectRecentByCollectionNumber(@Param("collectionNumber") String collectionNumber, @Param("limit") int limit);
}
