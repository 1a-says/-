import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 用户状态管理
 * 存储用户角色、登录信息、账号锁定状态等
 */
export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref(null)
  // 用户角色: 'admin' | 'user'
  const userRole = ref('')
  // 是否已登录
  const isLoggedIn = ref(false)
  // 登录失败次数记录 { 账号: { count: 次数, lockTime: 锁定时间 } }
  const loginFailRecords = ref({})

  /**
   * 登录成功 - 保存用户信息
   * @param {Object} user - 用户信息对象
   * @param {string} role - 用户角色 admin/user
   */
  const login = (user, role) => {
    userInfo.value = user
    userRole.value = role
    isLoggedIn.value = true

    // 清除该账号的失败记录
    if (loginFailRecords.value[user.account]) {
      delete loginFailRecords.value[user.account]
    }

    // 持久化到localStorage
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('userRole', role)
    localStorage.setItem('isLoggedIn', 'true')
  }

  /**
   * 登出 - 清除用户信息
   */
  const logout = () => {
    userInfo.value = null
    userRole.value = ''
    isLoggedIn.value = false

    // 清除localStorage
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userRole')
    localStorage.removeItem('isLoggedIn')
  }

  /**
   * 记录登录失败
   * @param {string} account - 账号
   * @returns {Object} - 返回失败次数和是否被锁定
   */
  const recordLoginFail = (account) => {
    const now = Date.now()

    if (!loginFailRecords.value[account]) {
      loginFailRecords.value[account] = {
        count: 1,
        lockTime: null
      }
    } else {
      // 如果已被锁定，检查是否已过30分钟
      if (loginFailRecords.value[account].lockTime) {
        const lockDuration = 30 * 60 * 1000 // 30分钟
        if (now - loginFailRecords.value[account].lockTime < lockDuration) {
          // 仍在锁定期内
          return {
            count: loginFailRecords.value[account].count,
            isLocked: true,
            remainingTime: Math.ceil((lockDuration - (now - loginFailRecords.value[account].lockTime)) / 1000 / 60)
          }
        } else {
          // 锁定期已过，重置
          loginFailRecords.value[account] = {
            count: 1,
            lockTime: null
          }
        }
      } else {
        loginFailRecords.value[account].count++
      }
    }

    const failCount = loginFailRecords.value[account].count

    // 达到3次失败，锁定账号
    if (failCount >= 3) {
      loginFailRecords.value[account].lockTime = now
      return {
        count: failCount,
        isLocked: true,
        remainingTime: 30
      }
    }

    return {
      count: failCount,
      isLocked: false,
      remainingTime: 0
    }
  }

  /**
   * 检查账号是否被锁定
   * @param {string} account - 账号
   * @returns {Object} - 是否被锁定和剩余时间
   */
  const checkAccountLock = (account) => {
    if (!loginFailRecords.value[account] || !loginFailRecords.value[account].lockTime) {
      return { isLocked: false, remainingTime: 0 }
    }

    const now = Date.now()
    const lockDuration = 30 * 60 * 1000 // 30分钟
    const elapsed = now - loginFailRecords.value[account].lockTime

    if (elapsed < lockDuration) {
      return {
        isLocked: true,
        remainingTime: Math.ceil((lockDuration - elapsed) / 1000 / 60)
      }
    } else {
      // 锁定期已过，清除锁定
      delete loginFailRecords.value[account]
      return { isLocked: false, remainingTime: 0 }
    }
  }

  /**
   * 从localStorage恢复登录状态
   */
  const restoreLoginState = () => {
    const savedUserInfo = localStorage.getItem('userInfo')
    const savedUserRole = localStorage.getItem('userRole')
    const savedIsLoggedIn = localStorage.getItem('isLoggedIn')

    if (savedUserInfo && savedUserRole && savedIsLoggedIn === 'true') {
      userInfo.value = JSON.parse(savedUserInfo)
      userRole.value = savedUserRole
      isLoggedIn.value = true
    }
  }

  return {
    // 状态
    userInfo,
    userRole,
    isLoggedIn,
    loginFailRecords,

    // 方法
    login,
    logout,
    recordLoginFail,
    checkAccountLock,
    restoreLoginState
  }
})
