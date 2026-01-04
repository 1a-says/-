package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.dto.BorrowRequest;
import com.example.library.entity.Book;
import com.example.library.entity.BorrowRecord;
import com.example.library.entity.SystemConfig;
import com.example.library.entity.User;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.BorrowRecordMapper;
import com.example.library.mapper.SystemConfigMapper;
import com.example.library.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 借阅服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class BorrowService {

  private final BorrowRecordMapper borrowRecordMapper;
  private final UserMapper userMapper;
  private final BookMapper bookMapper;
  private final SystemConfigMapper systemConfigMapper;
  private final BookService bookService;

  /**
   * 借阅验证
   */
  public Map<String, Object> validateBorrow(String cardNumber, List<String> collectionNumbers) {
    // 验证用户
    LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
    userWrapper.eq(User::getCardNumber, cardNumber);
    User user = userMapper.selectOne(userWrapper);

    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    // 检查超期图书
    LambdaQueryWrapper<BorrowRecord> overdueWrapper = new LambdaQueryWrapper<>();
    overdueWrapper.eq(BorrowRecord::getCardNumber, cardNumber);
    overdueWrapper.eq(BorrowRecord::getStatus, "借阅中");
    overdueWrapper.lt(BorrowRecord::getDueDate, LocalDateTime.now());
    long overdueCount = borrowRecordMapper.selectCount(overdueWrapper);

    if (overdueCount > 0) {
      throw new RuntimeException("该用户存在" + overdueCount + "本超期图书，无法借阅");
    }

    // 验证图书状态
    List<Map<String, Object>> bookList = new ArrayList<>();
    for (String collectionNumber : collectionNumbers) {
      LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
      bookWrapper.eq(Book::getCollectionNumber, collectionNumber);
      Book book = bookMapper.selectOne(bookWrapper);

      if (book == null) {
        throw new RuntimeException("图书 " + collectionNumber + " 不存在");
      }

      if (!"可借阅".equals(book.getStatus())) {
        throw new RuntimeException("图书 " + book.getTitle() + " 状态不可借阅");
      }

      Map<String, Object> bookInfo = new HashMap<>();
      bookInfo.put("collectionNumber", book.getCollectionNumber());
      bookInfo.put("title", book.getTitle());
      bookInfo.put("author", book.getAuthor());
      bookInfo.put("status", book.getStatus());
      bookList.add(bookInfo);
    }

    // 计算应还日期
    LocalDateTime dueDate = calculateDueDate(user.getIdentity());

    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("accountNumber", user.getAccountNumber());
    userInfo.put("name", user.getName());
    userInfo.put("identity", user.getIdentity());
    userInfo.put("cardNumber", user.getCardNumber());

    Map<String, Object> result = new HashMap<>();
    result.put("user", userInfo);
    result.put("books", bookList);
    result.put("dueDate", dueDate);

    return result;
  }

  /**
   * 执行借阅
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> borrowBooks(BorrowRequest request) {
    // 先验证
    Map<String, Object> validation = validateBorrow(request.getCardNumber(), request.getCollectionNumbers());

    Map<String, Object> userInfo = (Map<String, Object>) validation.get("user");
    List<Map<String, Object>> books = (List<Map<String, Object>>) validation.get("books");
    LocalDateTime dueDate = (LocalDateTime) validation.get("dueDate");

    List<Map<String, Object>> records = new ArrayList<>();

    for (Map<String, Object> bookInfo : books) {
      String collectionNumber = (String) bookInfo.get("collectionNumber");

      // 查询图书详细信息
      LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
      bookWrapper.eq(Book::getCollectionNumber, collectionNumber);
      Book book = bookMapper.selectOne(bookWrapper);

      // 创建借阅记录
      BorrowRecord record = new BorrowRecord();
      record.setRecordId(generateRecordId());
      record.setCardNumber(request.getCardNumber());
      record.setUserName((String) userInfo.get("name"));
      record.setUserIdentity((String) userInfo.get("identity"));
      record.setAccountNumber((String) userInfo.get("accountNumber"));
      record.setCollectionNumber(collectionNumber);
      record.setBookTitle(book.getTitle());
      record.setBookAuthor(book.getAuthor());
      record.setBorrowDate(LocalDateTime.now());
      record.setDueDate(dueDate);
      record.setOverdueDays(0);
      record.setStatus("借阅中");
      record.setOperator(request.getOperator());
      record.setCreateTime(LocalDateTime.now());
      record.setUpdateTime(LocalDateTime.now());

      borrowRecordMapper.insert(record);

      // 更新图书状态为"已借出"
      bookService.updateBookStatus(collectionNumber, "已借出", request.getOperator());

      Map<String, Object> recordInfo = new HashMap<>();
      recordInfo.put("id", record.getRecordId());
      recordInfo.put("cardNumber", record.getCardNumber());
      recordInfo.put("userName", record.getUserName());
      recordInfo.put("userIdentity", record.getUserIdentity());
      recordInfo.put("accountNumber", record.getAccountNumber());
      recordInfo.put("collectionNumber", record.getCollectionNumber());
      recordInfo.put("bookTitle", record.getBookTitle());
      recordInfo.put("bookAuthor", record.getBookAuthor());
      recordInfo.put("borrowDate", record.getBorrowDate());
      recordInfo.put("dueDate", record.getDueDate());
      recordInfo.put("status", record.getStatus());
      records.add(recordInfo);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("records", records);

    return result;
  }

  /**
   * 图书归还
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> returnBook(String collectionNumber, String operator) {
    // 查找借阅记录
    LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(BorrowRecord::getCollectionNumber, collectionNumber);
    wrapper.eq(BorrowRecord::getStatus, "借阅中");
    BorrowRecord record = borrowRecordMapper.selectOne(wrapper);

    if (record == null) {
      throw new RuntimeException("该图书未借出，无法归还");
    }

    // 计算超期天数
    LocalDateTime now = LocalDateTime.now();
    long overdueDays = ChronoUnit.DAYS.between(record.getDueDate(), now);
    if (overdueDays < 0) {
      overdueDays = 0;
    }

    // 更新借阅记录
    record.setReturnDate(now);
    record.setOverdueDays((int) overdueDays);
    record.setStatus("已归还");
    record.setUpdateTime(now);
    borrowRecordMapper.updateById(record);

    // 更新图书状态为"可借阅"
    bookService.updateBookStatus(collectionNumber, "可借阅", operator);

    Map<String, Object> recordInfo = new HashMap<>();
    recordInfo.put("id", record.getRecordId());
    recordInfo.put("status", record.getStatus());
    recordInfo.put("returnDate", record.getReturnDate());
    recordInfo.put("overdueDays", record.getOverdueDays());

    Map<String, Object> result = new HashMap<>();
    result.put("record", recordInfo);

    return result;
  }

  /**
   * 查询个人借阅记录
   */
  public Map<String, Object> getMyRecords(String accountNumber) {
    LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(BorrowRecord::getAccountNumber, accountNumber);
    wrapper.orderByDesc(BorrowRecord::getBorrowDate);
    List<BorrowRecord> records = borrowRecordMapper.selectList(wrapper);

    // 统计信息
    long currentBorrowing = records.stream().filter(r -> "借阅中".equals(r.getStatus())).count();
    int totalBorrowed = records.size();
    long overdueCount = records.stream()
        .filter(r -> "借阅中".equals(r.getStatus()) && r.getDueDate().isBefore(LocalDateTime.now()))
        .count();

    Map<String, Object> statistics = new HashMap<>();
    statistics.put("currentBorrowing", currentBorrowing);
    statistics.put("totalBorrowed", totalBorrowed);
    statistics.put("overdueCount", overdueCount);

    Map<String, Object> result = new HashMap<>();
    result.put("statistics", statistics);
    result.put("records", records);

    return result;
  }

  /**
   * 分页查询全量借阅记录
   */
  public Map<String, Object> pageAllRecords(String accountNumber, String keyword, Integer page, Integer size) {
    page = page == null || page < 1 ? 1 : page;
    size = size == null || size < 1 ? 10 : size;

    Page<BorrowRecord> recordPage = new Page<>(page, size);
    LambdaQueryWrapper<BorrowRecord> wrapper = new LambdaQueryWrapper<>();

    if (accountNumber != null && !accountNumber.isEmpty()) {
      wrapper.like(BorrowRecord::getAccountNumber, accountNumber);
    }
    if (keyword != null && !keyword.isEmpty()) {
      wrapper.and(w -> w.like(BorrowRecord::getCollectionNumber, keyword)
          .or().like(BorrowRecord::getBookTitle, keyword));
    }

    wrapper.orderByDesc(BorrowRecord::getBorrowDate);
    recordPage = borrowRecordMapper.selectPage(recordPage, wrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("total", recordPage.getTotal());
    result.put("page", recordPage.getCurrent());
    result.put("size", recordPage.getSize());
    result.put("list", recordPage.getRecords());

    return result;
  }

  /**
   * 计算应还日期
   */
  private LocalDateTime calculateDueDate(String identity) {
    int days = 60; // 默认60天

    // 从系统配置读取
    LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
    if ("教师".equals(identity)) {
      wrapper.eq(SystemConfig::getConfigKey, "teacher_borrow_days");
    } else {
      wrapper.eq(SystemConfig::getConfigKey, "student_borrow_days");
    }

    SystemConfig config = systemConfigMapper.selectOne(wrapper);
    if (config != null) {
      days = Integer.parseInt(config.getConfigValue());
    }

    return LocalDateTime.now().plusDays(days);
  }

  /**
   * 生成借阅记录ID
   * 规则：BR + 年月日时分秒
   */
  private String generateRecordId() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    return "BR" + LocalDateTime.now().format(formatter);
  }
  
  /**
   * 根据馆藏号查询借阅记录
   */
  public List<BorrowRecord> getBorrowRecordsByCollectionNumber(String collectionNumber) {
    return borrowRecordMapper.selectByCollectionNumber(collectionNumber);
  }
  
  /**
   * 根据用户账号查询借阅记录
   */
  public List<BorrowRecord> getBorrowRecordsByAccountNumber(String accountNumber) {
    return borrowRecordMapper.selectByAccountNumber(accountNumber);
  }
  
  /**
   * 查询用户的超期图书
   */
  public List<BorrowRecord> getOverdueBooksByCardNumber(String cardNumber) {
    return borrowRecordMapper.selectOverdueByCardNumber(cardNumber);
  }
  
  /**
   * 查询用户的在借图书
   */
  public List<BorrowRecord> getBorrowingBooksByCardNumber(String cardNumber) {
    return borrowRecordMapper.selectBorrowingByCardNumber(cardNumber);
  }
  
  /**
   * 统计用户借阅总数
   */
  public int countUserBorrowed(String accountNumber) {
    return borrowRecordMapper.countByAccountNumber(accountNumber);
  }
  
  /**
   * 统计用户当前在借数量
   */
  public int countUserBorrowing(String cardNumber) {
    return borrowRecordMapper.countBorrowingByCardNumber(cardNumber);
  }
  
  /**
   * 查询指定时间段内的借阅记录
   */
  public List<BorrowRecord> getBorrowRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    return borrowRecordMapper.selectByDateRange(startDate, endDate);
  }
}
