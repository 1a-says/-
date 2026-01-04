package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ResultCode;
import com.example.library.dto.BookAddRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BookStatusHistory;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BookStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 图书服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class BookService {

  private final BookMapper bookMapper;
  private final BookStatusHistoryMapper bookStatusHistoryMapper;

  /**
   * 图书入库
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> addBook(BookAddRequest request) {
    // 生成馆藏号
    String collectionNumber = generateCollectionNumber();

    Book book = new Book();
    book.setCollectionNumber(collectionNumber);
    book.setIsbn(request.getIsbn());
    book.setTitle(request.getTitle());
    book.setAuthor(request.getAuthor());
    book.setPublisher(request.getPublisher());
    book.setLocation(request.getLocation());
    book.setStatus("可借阅");
    book.setCreateTime(LocalDateTime.now());
    book.setUpdateTime(LocalDateTime.now());

    bookMapper.insert(book);

    Map<String, Object> result = new HashMap<>();
    result.put("collectionNumber", book.getCollectionNumber());
    result.put("isbn", book.getIsbn());
    result.put("title", book.getTitle());
    result.put("author", book.getAuthor());
    result.put("publisher", book.getPublisher());
    result.put("location", book.getLocation());
    result.put("status", book.getStatus());
    result.put("createTime", book.getCreateTime());

    return result;
  }

  /**
   * 分页查询图书
   */
  public Map<String, Object> pageBooks(String isbn, String title, Integer page, Integer size) {
    page = page == null || page < 1 ? 1 : page;
    size = size == null || size < 1 ? 10 : size;

    Page<Book> bookPage = new Page<>(page, size);
    LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();

    if (isbn != null && !isbn.isEmpty()) {
      wrapper.like(Book::getIsbn, isbn);
    }
    if (title != null && !title.isEmpty()) {
      wrapper.like(Book::getTitle, title);
    }

    wrapper.orderByDesc(Book::getCreateTime);
    bookPage = bookMapper.selectPage(bookPage, wrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("total", bookPage.getTotal());
    result.put("page", bookPage.getCurrent());
    result.put("size", bookPage.getSize());
    result.put("list", bookPage.getRecords());

    return result;
  }

  /**
   * 根据馆藏号查询图书
   */
  public Map<String, Object> getBookByCollectionNumber(String collectionNumber) {
    LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Book::getCollectionNumber, collectionNumber);
    Book book = bookMapper.selectOne(wrapper);

    if (book == null) {
      throw new RuntimeException("图书不存在");
    }

    // 查询状态历史
    LambdaQueryWrapper<BookStatusHistory> historyWrapper = new LambdaQueryWrapper<>();
    historyWrapper.eq(BookStatusHistory::getCollectionNumber, collectionNumber);
    historyWrapper.orderByDesc(BookStatusHistory::getOperateTime);
    List<BookStatusHistory> historyList = bookStatusHistoryMapper.selectList(historyWrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("collectionNumber", book.getCollectionNumber());
    result.put("isbn", book.getIsbn());
    result.put("title", book.getTitle());
    result.put("author", book.getAuthor());
    result.put("publisher", book.getPublisher());
    result.put("location", book.getLocation());
    result.put("status", book.getStatus());
    result.put("statusHistory", historyList);
    result.put("createTime", book.getCreateTime());
    result.put("updateTime", book.getUpdateTime());

    return result;
  }

  /**
   * 更新图书状态
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> updateBookStatus(String collectionNumber, String status, String operator) {
    LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Book::getCollectionNumber, collectionNumber);
    Book book = bookMapper.selectOne(wrapper);

    if (book == null) {
      throw new RuntimeException("图书不存在");
    }

    book.setStatus(status);
    book.setUpdateTime(LocalDateTime.now());
    bookMapper.updateById(book);

    // 记录状态历史
    BookStatusHistory history = new BookStatusHistory();
    history.setCollectionNumber(collectionNumber);
    history.setStatus(status);
    history.setOperator(operator);
    history.setOperateTime(LocalDateTime.now());
    bookStatusHistoryMapper.insert(history);

    Map<String, Object> result = new HashMap<>();
    result.put("collectionNumber", book.getCollectionNumber());
    result.put("status", book.getStatus());
    result.put("updateTime", book.getUpdateTime());

    return result;
  }

  /**
   * 生成馆藏号
   * 规则：TS + 年月日时分秒(14位) + 随机3位数
   */
  private String generateCollectionNumber() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    String timestamp = LocalDateTime.now().format(formatter);
    int random = new Random().nextInt(1000);
    return String.format("TS%s%03d", timestamp, random);
  }
  
  /**
   * 根据ISBN查询图书
   */
  public List<Book> getBooksByIsbn(String isbn) {
    return bookMapper.selectByIsbn(isbn);
  }
  
  /**
   * 根据书名模糊查询图书
   */
  public List<Book> getBooksByTitle(String title) {
    return bookMapper.selectByTitle(title);
  }
  
  /**
   * 获取图书状态统计
   */
  public List<Map<String, Object>> getStatusStatistics() {
    return bookMapper.selectStatusStatistics();
  }
  
  /**
   * 检查馆藏号是否存在
   */
  public boolean existsByCollectionNumber(String collectionNumber) {
    return bookMapper.existsByCollectionNumber(collectionNumber) > 0;
  }
}
