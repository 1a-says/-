package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 * 
 * @author Library Management System
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据账号查询用户
     * 
     * @param accountNumber 账号
     * @return 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE account_number = #{accountNumber} AND deleted = 0")
    User selectByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * 根据校园卡号查询用户
     * 
     * @param cardNumber 校园卡号
     * @return 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE card_number = #{cardNumber} AND deleted = 0")
    User selectByCardNumber(@Param("cardNumber") String cardNumber);
    
    /**
     * 根据账号和校园卡号查询用户
     * 
     * @param accountNumber 账号
     * @param cardNumber 校园卡号
     * @return 用户信息
     */
    @Select("SELECT * FROM tb_user WHERE account_number = #{accountNumber} AND card_number = #{cardNumber} AND deleted = 0")
    User selectByAccountAndCard(@Param("accountNumber") String accountNumber, @Param("cardNumber") String cardNumber);
    
    /**
     * 检查账号是否存在
     * 
     * @param accountNumber 账号
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE account_number = #{accountNumber} AND deleted = 0")
    int existsByAccountNumber(@Param("accountNumber") String accountNumber);
    
    /**
     * 检查校园卡号是否存在
     * 
     * @param cardNumber 校园卡号
     * @return 是否存在
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE card_number = #{cardNumber} AND deleted = 0")
    int existsByCardNumber(@Param("cardNumber") String cardNumber);
    
    /**
     * 根据账号更新登录失败次数
     * 
     * @param accountNumber 账号
     * @param failCount 失败次数
     */
    @Update("UPDATE tb_user SET login_fail_count = #{failCount}, update_time = NOW() WHERE account_number = #{accountNumber}")
    void updateLoginFailCount(@Param("accountNumber") String accountNumber, @Param("failCount") int failCount);
}
