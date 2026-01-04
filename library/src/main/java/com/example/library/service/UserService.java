package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.dto.UserAddRequest;
import com.example.library.entity.User;
import com.example.library.mapper.UserMapper;
import com.example.library.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserMapper userMapper;

  /**
   * 用户录入
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> addUser(UserAddRequest request) {
    // 检查账号是否已存在
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getAccountNumber, request.getAccountNumber());
    if (userMapper.selectCount(wrapper) > 0) {
      throw new RuntimeException("账号已存在");
    }

    // 检查校园卡号是否已存在
    wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getCardNumber, request.getCardNumber());
    if (userMapper.selectCount(wrapper) > 0) {
      throw new RuntimeException("校园卡号已存在");
    }

    // 生成初始密码
    String initialPassword = PasswordUtil.generateInitialPassword(request.getAccountNumber());
    String encryptedPassword = PasswordUtil.sha256(initialPassword);

    User user = new User();
    user.setAccountNumber(request.getAccountNumber());
    user.setName(request.getName());
    user.setIdentity(request.getIdentity());
    user.setCardNumber(request.getCardNumber());
    user.setPassword(encryptedPassword);
    user.setInitialPassword(initialPassword);
    user.setRole("user");
    user.setStatus("正常");
    user.setLoginFailCount(0);
    user.setCreateTime(LocalDateTime.now());
    user.setUpdateTime(LocalDateTime.now());

    userMapper.insert(user);

    Map<String, Object> result = new HashMap<>();
    result.put("accountNumber", user.getAccountNumber());
    result.put("name", user.getName());
    result.put("identity", user.getIdentity());
    result.put("cardNumber", user.getCardNumber());
    result.put("initialPassword", user.getInitialPassword());
    result.put("role", user.getRole());
    result.put("status", user.getStatus());
    result.put("createTime", user.getCreateTime());

    return result;
  }

  /**
   * 用户查询
   */
  public User getUserByAccountAndCard(String accountNumber, String cardNumber) {
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getAccountNumber, accountNumber);
    wrapper.eq(User::getCardNumber, cardNumber);
    return userMapper.selectOne(wrapper);
  }

  /**
   * 重置密码
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> resetPassword(String accountNumber, String cardNumber) {
    User user = getUserByAccountAndCard(accountNumber, cardNumber);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    // 重置为初始密码
    String encryptedPassword = PasswordUtil.sha256(user.getInitialPassword());
    user.setPassword(encryptedPassword);
    user.setUpdateTime(LocalDateTime.now());
    userMapper.updateById(user);

    Map<String, Object> result = new HashMap<>();
    result.put("userName", user.getName());
    result.put("userIdentity", user.getIdentity());
    result.put("initialPassword", user.getInitialPassword());

    return result;
  }

  /**
   * 用户列表查询
   */
  public Map<String, Object> pageUsers(Integer page, Integer size) {
    page = page == null || page < 1 ? 1 : page;
    size = size == null || size < 1 ? 10 : size;

    Page<User> userPage = new Page<>(page, size);
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByDesc(User::getCreateTime);
    userPage = userMapper.selectPage(userPage, wrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("total", userPage.getTotal());
    result.put("page", userPage.getCurrent());
    result.put("size", userPage.getSize());
    result.put("list", userPage.getRecords());

    return result;
  }
  
  /**
   * 根据账号查询用户
   */
  public User getUserByAccountNumber(String accountNumber) {
    return userMapper.selectByAccountNumber(accountNumber);
  }
  
  /**
   * 根据校园卡号查询用户
   */
  public User getUserByCardNumber(String cardNumber) {
    return userMapper.selectByCardNumber(cardNumber);
  }
  
  /**
   * 检查账号是否存在
   */
  public boolean existsByAccountNumber(String accountNumber) {
    return userMapper.existsByAccountNumber(accountNumber) > 0;
  }
  
  /**
   * 检查校园卡号是否存在
   */
  public boolean existsByCardNumber(String cardNumber) {
    return userMapper.existsByCardNumber(cardNumber) > 0;
  }
  
  /**
   * 更新登录失败次数
   */
  public void updateLoginFailCount(String accountNumber, int failCount) {
    userMapper.updateLoginFailCount(accountNumber, failCount);
  }
}
