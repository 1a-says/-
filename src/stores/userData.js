import { defineStore } from 'pinia'
import { ref } from 'vue'
import CryptoJS from 'crypto-js'

/**
 * 用户数据管理Store
 * 管理系统用户的增删改查操作
 */
export const useUserDataStore = defineStore('userData', () => {
  // 用户列表数据
  const userList = ref([])

  /**
   * 密码加密函数
   * 使用SHA256加密，与登录页保持一致
   * @param {string} password - 明文密码
   * @returns {string} - 加密后的密码
   */
  const encryptPassword = (password) => {
    return CryptoJS.SHA256(password).toString()
  }

  /**
   * 生成初始密码
   * 规则：学号/工号后6位
   * @param {string} accountNumber - 学号/工号
   * @returns {string} - 初始密码
   */
  const generateInitialPassword = (accountNumber) => {
    if (accountNumber.length >= 6) {
      return accountNumber.slice(-6)
    } else {
      // 如果长度不足6位，直接返回原账号
      return accountNumber
    }
  }

  /**
   * 检查校园卡号是否已存在
   * @param {string} cardNumber - 校园卡号
   * @returns {boolean} - 是否已存在
   */
  const isCardNumberExist = (cardNumber) => {
    return userList.value.some(user => user.cardNumber === cardNumber)
  }

  /**
   * 检查学号/工号是否已存在
   * @param {string} accountNumber - 学号/工号
   * @returns {boolean} - 是否已存在
   */
  const isAccountNumberExist = (accountNumber) => {
    return userList.value.some(user => user.accountNumber === accountNumber)
  }

  /**
   * 添加用户
   * @param {Object} userData - 用户信息
   * @returns {Object} - 添加结果
   */
  const addUser = (userData) => {
    // 检查学号/工号是否已存在
    if (isAccountNumberExist(userData.accountNumber)) {
      return {
        success: false,
        message: '该学号/工号已存在'
      }
    }

    // 检查校园卡号是否已存在
    if (isCardNumberExist(userData.cardNumber)) {
      return {
        success: false,
        message: '该校园卡号已存在'
      }
    }

    // 生成初始密码
    const initialPassword = generateInitialPassword(userData.accountNumber)

    // 创建新用户
    const newUser = {
      accountNumber: userData.accountNumber,  // 学号/工号
      name: userData.name,                    // 姓名
      identity: userData.identity,            // 身份（教师/学生）
      cardNumber: userData.cardNumber,        // 校园卡号
      password: encryptPassword(initialPassword),  // 加密后的密码
      role: 'user',                          // 角色（普通用户）
      status: '正常',                        // 账号状态
      createTime: new Date().toISOString()   // 创建时间
    }

    userList.value.push(newUser)

    // 持久化到localStorage
    saveToStorage()

    return {
      success: true,
      message: '用户录入成功',
      initialPassword  // 返回初始密码用于提示
    }
  }

  /**
   * 根据学号/工号和校园卡号查询用户
   * @param {string} accountNumber - 学号/工号
   * @param {string} cardNumber - 校园卡号
   * @returns {Object|null} - 用户信息
   */
  const getUserByAccountAndCard = (accountNumber, cardNumber) => {
    return userList.value.find(user =>
      user.accountNumber === accountNumber &&
      user.cardNumber === cardNumber
    ) || null
  }

  /**
   * 重置用户密码
   * @param {string} accountNumber - 学号/工号
   * @param {string} cardNumber - 校园卡号
   * @returns {Object} - 重置结果
   */
  const resetUserPassword = (accountNumber, cardNumber) => {
    const user = getUserByAccountAndCard(accountNumber, cardNumber)

    if (!user) {
      return {
        success: false,
        message: '未找到该用户，请确认学号/工号和校园卡号'
      }
    }

    // 生成初始密码
    const initialPassword = generateInitialPassword(accountNumber)

    // 更新密码（加密存储）
    user.password = encryptPassword(initialPassword)
    user.updateTime = new Date().toISOString()

    // 持久化
    saveToStorage()

    return {
      success: true,
      message: '密码重置成功',
      initialPassword,
      userName: user.name,
      userIdentity: user.identity
    }
  }

  /**
   * 保存到localStorage
   */
  const saveToStorage = () => {
    localStorage.setItem('userDataList', JSON.stringify(userList.value))
  }

  /**
   * 从localStorage恢复数据
   */
  const restoreFromStorage = () => {
    const saved = localStorage.getItem('userDataList')
    if (saved) {
      try {
        userList.value = JSON.parse(saved)
      } catch (e) {
        console.error('恢复用户数据失败:', e)
        userList.value = []
      }
    }
  }

  /**
   * 初始化模拟数据（仅用于演示）
   */
  const initMockData = () => {
    if (userList.value.length === 0) {
      // 添加一些示例用户（已在登录页面定义的用户）
      const mockUsers = [
        {
          accountNumber: '2021001',
          name: '张三',
          identity: '学生',
          cardNumber: 'C2021001',
          password: encryptPassword('21001'), // 后6位
          role: 'user',
          status: '正常',
          createTime: '2026-01-01T10:00:00.000Z'
        },
        {
          accountNumber: '2021002',
          name: '李四',
          identity: '学生',
          cardNumber: 'C2021002',
          password: encryptPassword('21002'),
          role: 'user',
          status: '正常',
          createTime: '2026-01-01T10:30:00.000Z'
        },
        {
          accountNumber: 'T001',
          name: '王老师',
          identity: '教师',
          cardNumber: 'CT001',
          password: encryptPassword('T001'), // 不足6位，直接使用原账号
          role: 'user',
          status: '正常',
          createTime: '2026-01-01T11:00:00.000Z'
        }
      ]

      userList.value = mockUsers
      saveToStorage()
    }
  }

  return {
    // 状态
    userList,

    // 方法
    encryptPassword,
    generateInitialPassword,
    isCardNumberExist,
    isAccountNumberExist,
    addUser,
    getUserByAccountAndCard,
    resetUserPassword,
    restoreFromStorage,
    initMockData
  }
})
