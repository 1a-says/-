package com.example.library.controller;

import com.example.library.common.Result;
import com.example.library.dto.UserAddRequest;
import com.example.library.entity.User;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 
 * @author Library Management System
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * 用户录入
   */
  @PostMapping
  public Result<Map<String, Object>> addUser(@Valid @RequestBody UserAddRequest request) {
    try {
      Map<String, Object> data = userService.addUser(request);
      return Result.success("用户录入成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 用户查询
   */
  @GetMapping
  public Result<User> getUser(
      @RequestParam String accountNumber,
      @RequestParam String cardNumber) {
    try {
      User user = userService.getUserByAccountAndCard(accountNumber, cardNumber);
      if (user == null) {
        return Result.error("用户不存在");
      }
      return Result.success("查询成功", user);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 重置密码
   */
  @PutMapping("/reset-password")
  public Result<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
    try {
      String accountNumber = request.get("accountNumber");
      String cardNumber = request.get("cardNumber");
      Map<String, Object> data = userService.resetPassword(accountNumber, cardNumber);
      return Result.success("密码重置成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  /**
   * 用户列表查询
   */
  @GetMapping("/list")
  public Result<Map<String, Object>> pageUsers(
      @RequestParam(required = false, defaultValue = "1") Integer page,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
    try {
      Map<String, Object> data = userService.pageUsers(page, size);
      return Result.success("查询成功", data);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 根据账号查询用户
   */
  @GetMapping("/account/{accountNumber}")
  public Result<User> getUserByAccountNumber(@PathVariable String accountNumber) {
    try {
      User user = userService.getUserByAccountNumber(accountNumber);
      if (user == null) {
        return Result.error("用户不存在");
      }
      return Result.success("查询成功", user);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 根据校园卡号查询用户
   */
  @GetMapping("/card/{cardNumber}")
  public Result<User> getUserByCardNumber(@PathVariable String cardNumber) {
    try {
      User user = userService.getUserByCardNumber(cardNumber);
      if (user == null) {
        return Result.error("用户不存在");
      }
      return Result.success("查询成功", user);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 检查用户账号是否存在
   */
  @GetMapping("/exists/{accountNumber}")
  public Result<Boolean> existsByAccountNumber(@PathVariable String accountNumber) {
    try {
      boolean exists = userService.existsByAccountNumber(accountNumber);
      return Result.success("查询成功", exists);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
  
  /**
   * 检查校园卡号是否存在
   */
  @GetMapping("/exists-card/{cardNumber}")
  public Result<Boolean> existsByCardNumber(@PathVariable String cardNumber) {
    try {
      boolean exists = userService.existsByCardNumber(cardNumber);
      return Result.success("查询成功", exists);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
