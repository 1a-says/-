package com.example.library.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成和解析 JWT Token
 * 
 * @author Library Management System
 */
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration}")
  private Long expiration;

  /**
   * 生成密钥
   */
  private Key getSigningKey() {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * 生成 Token
   * 
   * @param account 账号
   * @param role    角色
   * @return Token 字符串
   */
  public String generateToken(String account, String role) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("account", account);
    claims.put("role", role);

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(account)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * 从 Token 中获取账号
   * 
   * @param token Token 字符串
   * @return 账号
   */
  public String getAccountFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims != null ? claims.get("account", String.class) : null;
  }

  /**
   * 从 Token 中获取角色
   * 
   * @param token Token 字符串
   * @return 角色
   */
  public String getRoleFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims != null ? claims.get("role", String.class) : null;
  }

  /**
   * 从 Token 中获取 Claims
   * 
   * @param token Token 字符串
   * @return Claims 对象
   */
  private Claims getClaimsFromToken(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * 验证 Token 是否有效
   * 
   * @param token Token 字符串
   * @return 是否有效
   */
  public boolean validateToken(String token) {
    try {
      Claims claims = getClaimsFromToken(token);
      if (claims == null) {
        return false;
      }

      Date expiration = claims.getExpiration();
      return expiration.after(new Date());
    } catch (Exception e) {
      return false;
    }
  }
}
