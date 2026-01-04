package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.dto.BorrowRequest;
import com.example.library.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.library.entity.BorrowRecord;

/**
 * 借阅控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

  private final BorrowService borrowService;

  /**
   * 借阅验证
   */
  @PostMapping("/validate")
  public Result<Map<String, Object>> validateBorrow(@Valid @RequestBody BorrowRequest request) {
    try {
      Map<String, Object> data = borrowService.validateBorrow(request.getCardNumber(), request.getCollectionNumbers());
      return Result.success("验证通过", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 执行借阅
   */
  @PostMapping
  public Result<Map<String, Object>> borrowBooks(@Valid @RequestBody BorrowRequest request) {
    try {
      Map<String, Object> data = borrowService.borrowBooks(request);
      return Result.success("借阅成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 图书归还
   */
  @PostMapping("/return")
  public Result<Map<String, Object>> returnBook(@RequestBody Map<String, String> request) {
    try {
      String collectionNumber = request.get("collectionNumber");
      String operator = request.get("operator");
      Map<String, Object> data = borrowService.returnBook(collectionNumber, operator);

      int overdueDays = (int) ((Map<String, Object>) data.get("record")).get("overdueDays");
      String message = overdueDays > 0 ? "该图书超期" + overdueDays + "天" : "归还成功";

      return Result.success(message, data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 查询个人借阅记录
   */
  @GetMapping("/my-records")
  public Result<Map<String, Object>> getMyRecords(@RequestParam String accountNumber) {
    try {
      Map<String, Object> data = borrowService.getMyRecords(accountNumber);
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 全量借阅记录查询
   */
  @GetMapping("/all-records")
  public Result<Map<String, Object>> pageAllRecords(
      @RequestParam(required = false) String accountNumber,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
    try {
      Map<String, Object> data = borrowService.pageAllRecords(accountNumber, keyword, page, size);
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 根据馆藏号查询借阅记录
   */
  @GetMapping("/records/{collectionNumber}")
  public Result<List<BorrowRecord>> getBorrowRecordsByCollectionNumber(@PathVariable String collectionNumber) {
    try {
      return Result.success("查询成功", borrowService.getBorrowRecordsByCollectionNumber(collectionNumber));
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 查询用户的超期图书
   */
  @GetMapping("/overdue")
  public Result<List<BorrowRecord>> getOverdueBooksByCardNumber(@RequestParam String cardNumber) {
    try {
      return Result.success("查询成功", borrowService.getOverdueBooksByCardNumber(cardNumber));
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 统计用户当前在借数量
   */
  @GetMapping("/borrowing-count")
  public Result<Map<String, Object>> countUserBorrowing(@RequestParam String cardNumber) {
    try {
      int count = borrowService.countUserBorrowing(cardNumber);
      Map<String, Object> result = new HashMap<>();
      result.put("count", count);
      return Result.success("查询成功", result);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
