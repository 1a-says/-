package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.dto.BookAddRequest;
import com.example.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.library.entity.Book;

/**
 * 图书控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BookService bookService;

  /**
   * 图书入库
   */
  @PostMapping
  public Result<Map<String, Object>> addBook(@Valid @RequestBody BookAddRequest request) {
    try {
      Map<String, Object> data = bookService.addBook(request);
      return Result.success("入库成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 分页查询图书
   */
  @GetMapping
  public Result<Map<String, Object>> pageBooks(
      @RequestParam(required = false) String isbn,
      @RequestParam(required = false) String title,
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
    try {
      Map<String, Object> data = bookService.pageBooks(isbn, title, page, size);
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 根据馆藏号查询图书
   */
  @GetMapping("/{collectionNumber}")
  public Result<Map<String, Object>> getBookByCollectionNumber(@PathVariable String collectionNumber) {
    try {
      Map<String, Object> data = bookService.getBookByCollectionNumber(collectionNumber);
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 更新图书状态
   */
  @PutMapping("/{collectionNumber}/status")
  public Result<Map<String, Object>> updateBookStatus(
      @PathVariable String collectionNumber,
      @RequestBody Map<String, String> request) {
    try {
      String status = request.get("status");
      String operator = request.get("operator");
      Map<String, Object> data = bookService.updateBookStatus(collectionNumber, status, operator);
      return Result.success("状态修改成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 根据ISBN查询图书
   */
  @GetMapping("/isbn/{isbn}")
  public Result<List<Book>> getBooksByIsbn(@PathVariable String isbn) {
    try {
      return Result.success("查询成功", bookService.getBooksByIsbn(isbn));
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 根据书名模糊查询图书
   */
  @GetMapping("/search")
  public Result<List<Book>> searchBooksByTitle(@RequestParam String title) {
    try {
      return Result.success("查询成功", bookService.getBooksByTitle(title));
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 获取图书状态统计
   */
  @GetMapping("/statistics/status")
  public Result<List<Map<String, Object>>> getStatusStatistics() {
    try {
      return Result.success("查询成功", bookService.getStatusStatistics());
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 检查馆藏号是否存在
   */
  @GetMapping("/exists/{collectionNumber}")
  public Result<Boolean> existsByCollectionNumber(@PathVariable String collectionNumber) {
    try {
      boolean exists = bookService.existsByCollectionNumber(collectionNumber);
      return Result.success("查询成功", exists);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
