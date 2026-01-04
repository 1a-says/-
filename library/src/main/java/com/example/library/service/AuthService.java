package com.example.library.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.library.entity.User;
import com.example.library.mapper.UserMapper;
import com.example.library.util.JwtUtil;
import com.example.library.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务类
 * 
 * @author Library Management System
 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserMapper userMapper;
  private final JwtUtil jwtUtil;

  /**
   * 用户登录
   */
  @Transactional(rollbackFor = Exception.class)
  public Map<String, Object> login(String account, String password) {
    // 查询用户
    LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(User::getAccountNumber, account);
    User user = userMapper.selectOne(wrapper);

    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    // 检查账号状态
    if ("锁定".equals(user.getStatus())) {
      // 检查锁定时间是否超过30分钟
      if (user.getLockTime() != null) {
        long minutes = java.time.Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes();
        if (minutes < 30) {
          throw new RuntimeException("账号已被锁定，剩余" + (30 - minutes) + "分钟");
        } else {
          // 解锁账号
          user.setStatus("正常");
          user.setLoginFailCount(0);
          user.setLockTime(null);
          userMapper.updateById(user);
        }
      }
    }

    // 验证密码（前端已经 SHA256 加密，后端再次加密比对）
    String encryptedPassword = PasswordUtil.sha256(password);
    if (!encryptedPassword.equals(user.getPassword())) {
      // 密码错误，增加失败次数
      user.setLoginFailCount(user.getLoginFailCount() + 1);
      if (user.getLoginFailCount() >= 3) {
        // 锁定账号
        user.setStatus("锁定");
        user.setLockTime(LocalDateTime.now());
        userMapper.updateById(user);
        throw new RuntimeException("密码错误次数过多，账号已被锁定30分钟");
      }
      userMapper.updateById(user);
      throw new RuntimeException("密码错误");
    }

    // 登录成功，重置失败次数
    user.setLoginFailCount(0);
    userMapper.updateById(user);

    // 生成 Token
    String token = jwtUtil.generateToken(user.getAccountNumber(), user.getRole());

    Map<String, Object> userInfo = new HashMap<>();
    userInfo.put("account", user.getAccountNumber());
    userInfo.put("name", user.getName());
    userInfo.put("role", user.getRole());

    Map<String, Object> result = new HashMap<>();
    result.put("token", token);
    result.put("userInfo", userInfo);

    return result;
  }
  
  /**
   * 验证用户账号和密码
   */
  public User validateUser(String account, String password) {
    User user = userMapper.selectByAccountNumber(account);
    
    if (user == null) {
      return null;
    }
    
    String encryptedPassword = PasswordUtil.sha256(password);
    if (encryptedPassword.equals(user.getPassword())) {
      return user;
    }
    
    return null;
  }
  
  /**
   * 更新用户登录失败次数
   */
  public void updateUserLoginFailCount(String accountNumber, int failCount) {
    userMapper.updateLoginFailCount(accountNumber, failCount);
  }
  
  /**
   * 检查用户是否存在
   */
  public boolean existsUserByAccount(String accountNumber) {
    return userMapper.existsByAccountNumber(accountNumber) > 0;
  }
}
