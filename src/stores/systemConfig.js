import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSystemConfigStore = defineStore('systemConfig', () => {
  // 默认配置
  const DEFAULT_CONFIG = {
    teacherBorrowDays: 90, // 教师借阅期限
    studentBorrowDays: 60, // 学生借阅期限
    maxBorrowCount: 5      // 单次最大借阅数
  }

  // 系统配置参数
  const config = ref({
    teacherBorrowDays: DEFAULT_CONFIG.teacherBorrowDays,
    studentBorrowDays: DEFAULT_CONFIG.studentBorrowDays,
    maxBorrowCount: DEFAULT_CONFIG.maxBorrowCount
  })

  /**
   * 从localStorage恢复配置
   */
  const restoreFromStorage = () => {
    try {
      const stored = localStorage.getItem('library_system_config')
      if (stored) {
        const parsedConfig = JSON.parse(stored)
        config.value = {
          teacherBorrowDays: parsedConfig.teacherBorrowDays || DEFAULT_CONFIG.teacherBorrowDays,
          studentBorrowDays: parsedConfig.studentBorrowDays || DEFAULT_CONFIG.studentBorrowDays,
          maxBorrowCount: parsedConfig.maxBorrowCount || DEFAULT_CONFIG.maxBorrowCount
        }
      }
    } catch (error) {
      console.error('恢复系统配置失败:', error)
    }
  }

  /**
   * 保存配置到localStorage
   */
  const saveToStorage = () => {
    try {
      localStorage.setItem('library_system_config', JSON.stringify(config.value))
    } catch (error) {
      console.error('保存系统配置失败:', error)
    }
  }

  /**
   * 更新配置
   * @param {Object} newConfig - 新配置对象
   */
  const updateConfig = (newConfig) => {
    config.value = {
      teacherBorrowDays: newConfig.teacherBorrowDays,
      studentBorrowDays: newConfig.studentBorrowDays,
      maxBorrowCount: newConfig.maxBorrowCount
    }
    saveToStorage()
  }

  /**
   * 恢复默认配置
   */
  const resetToDefault = () => {
    config.value = {
      teacherBorrowDays: DEFAULT_CONFIG.teacherBorrowDays,
      studentBorrowDays: DEFAULT_CONFIG.studentBorrowDays,
      maxBorrowCount: DEFAULT_CONFIG.maxBorrowCount
    }
    saveToStorage()
  }

  /**
   * 获取指定身份的借阅期限
   * @param {String} identity - 身份（教师/学生）
   * @returns {Number} 借阅期限（天数）
   */
  const getBorrowDays = (identity) => {
    return identity === '教师'
      ? config.value.teacherBorrowDays
      : config.value.studentBorrowDays
  }

  /**
   * 获取最大借阅数
   * @returns {Number} 最大借阅数
   */
  const getMaxBorrowCount = () => {
    return config.value.maxBorrowCount
  }

  return {
    config,
    DEFAULT_CONFIG,
    restoreFromStorage,
    saveToStorage,
    updateConfig,
    resetToDefault,
    getBorrowDays,
    getMaxBorrowCount
  }
})
