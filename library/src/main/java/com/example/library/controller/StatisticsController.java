package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

  private final BorrowRecordMapper borrowRecordMapper;
  private final BookService bookService;

  /**
   * 借阅TOP5统计
   */
  @GetMapping("/top-books")
  public Result<Map<String, Object>> getTopBooks(
      @RequestParam(required = false, defaultValue = "week") String dimension) {
    try {
      List<Map<String, Object>> topBooks = borrowRecordMapper.selectTopBooks(dimension);

      // 计算总数用于百分比
      long totalCount = topBooks.stream()
          .mapToLong(book -> ((Number) book.get("borrow_count")).longValue())
          .sum();

      // 添加百分比
      topBooks.forEach(book -> {
        long borrowCount = ((Number) book.get("borrow_count")).longValue();
        int percentage = totalCount > 0 ? (int) ((borrowCount * 100) / totalCount) : 0;
        book.put("borrowCount", borrowCount);
        book.put("percentage", percentage);
        book.remove("borrow_count");
      });

      Map<String, Object> result = new HashMap<>();
      result.put("dimension", dimension);
      result.put("topBooks", topBooks);

      return Result.success("统计完成", result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 图书状态统计
   */
  @GetMapping("/book-status")
  public Result<Map<String, Object>> getBookStatusStatistics() {
    try {
      List<Map<String, Object>> statistics = bookService.getStatusStatistics();
      Map<String, Object> result = new HashMap<>();
      result.put("statistics", statistics);
      return Result.success("查询成功", result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 借阅趋势统计
   */
  @GetMapping("/borrow-trend")
  public Result<Map<String, Object>> getBorrowTrend(
      @RequestParam(required = false, defaultValue = "7") Integer days) {
    try {
      return Result.error("功能待实现");
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
