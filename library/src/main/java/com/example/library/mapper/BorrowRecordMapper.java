package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

import java.time.LocalDateTime;

/**
 * 借阅记录 Mapper 接口
 * 
 * @author Library Management System
 */
@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

  /**
   * 统计借阅TOP5图书
   * 
   * @param dimension 时间维度（week/month）
   * @return TOP5图书列表
   */
  @Select({
      "<script>",
      "SELECT ",
      "    br.collection_number,",
      "    br.book_title,",
      "    br.book_author AS author,",
      "    COUNT(*) AS borrow_count ",
      "FROM tb_borrow_record br ",
      "WHERE br.borrow_date >= ",
      "    <if test='dimension == \"week\"'>",
      "        DATE_SUB(NOW(), INTERVAL 7 DAY)",
      "    </if>",
      "    <if test='dimension == \"month\"'>",
      "        DATE_SUB(NOW(), INTERVAL 30 DAY)",
      "    </if>",
      "GROUP BY br.collection_number, br.book_title, br.book_author ",
      "ORDER BY borrow_count DESC ",
      "LIMIT 5",
      "</script>"
  })
  List<Map<String, Object>> selectTopBooks(@Param("dimension") String dimension);
    
    /**
     * 根据馆藏号查询借阅记录
     * 
     * @param collectionNumber 馆藏号
     * @return 借阅记录
     */
    @Select("SELECT * FROM tb_borrow_record WHERE collection_number = #{collectionNumber} ORDER BY borrow_date DESC")
    List<BorrowRecord> selectByCollectionNumber(@Param("collectionNumber") String collectionNumber);
    
    /**
     * 根据用户账号查询借阅记录
     * 
     * @param accountNumber 用户账号
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM tb_borrow_record WHERE account_number = #{accountNumber} ORDER BY borrow_date DESC")
    List<BorrowRecord> selectByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * 查询用户的超期图书
     * 
     * @param cardNumber 校园卡号
     * @return 超期借阅记录列表
     */
    @Select("SELECT * FROM tb_borrow_record WHERE card_number = #{cardNumber} AND status = '借阅中' AND due_date < NOW()")
    List<BorrowRecord> selectOverdueByCardNumber(@Param("cardNumber") String cardNumber);
    
    /**
     * 查询用户的在借图书
     * 
     * @param cardNumber 校园卡号
     * @return 在借借阅记录列表
     */
    @Select("SELECT * FROM tb_borrow_record WHERE card_number = #{cardNumber} AND status = '借阅中'")
    List<BorrowRecord> selectBorrowingByCardNumber(@Param("cardNumber") String cardNumber);
    
    /**
     * 统计用户借阅总数
     * 
     * @param accountNumber 用户账号
     * @return 借阅总数
     */
    @Select("SELECT COUNT(*) FROM tb_borrow_record WHERE account_number = #{accountNumber}")
    int countByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * 统计用户当前在借数量
     * 
     * @param cardNumber 校园卡号
     * @return 当前在借数量
     */
    @Select("SELECT COUNT(*) FROM tb_borrow_record WHERE card_number = #{cardNumber} AND status = '借阅中'")
    int countBorrowingByCardNumber(@Param("cardNumber") String cardNumber);
    
    /**
     * 查询指定时间段内的借阅记录
     * 
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 借阅记录列表
     */
    @Select("SELECT * FROM tb_borrow_record WHERE borrow_date BETWEEN #{startDate} AND #{endDate} ORDER BY borrow_date DESC")
    List<BorrowRecord> selectByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
