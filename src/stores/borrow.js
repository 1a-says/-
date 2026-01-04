import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useBookStore } from './book'
import { useUserDataStore } from './userData'
import { useSystemConfigStore } from './systemConfig'

/**
 * 借阅管理状态管理
 * 管理图书借阅和归还操作
 */
export const useBorrowStore = defineStore('borrow', () => {
  // 借阅记录列表
  const borrowRecords = ref([])

  /**
   * 计算应还日期
   * 根据系统配置的借阅期限计算应还日期
   * @param {string} identity - 用户身份（教师/学生）
   * @returns {string} - 应还日期（ISO格式）
   */
  const calculateDueDate = (identity) => {
    const configStore = useSystemConfigStore()
    const now = new Date()
    const days = configStore.getBorrowDays(identity)
    now.setDate(now.getDate() + days)
    return now.toISOString()
  }

  /**
   * 检查用户是否有超期图书
   * @param {string} cardNumber - 校园卡号
   * @returns {Object} - 检查结果
   */
  const checkUserOverdue = (cardNumber) => {
    const now = new Date()

    // 查找该用户的所有未归还记录
    const userRecords = borrowRecords.value.filter(record =>
      record.cardNumber === cardNumber &&
      record.status === '借阅中'
    )

    // 检查是否有超期
    const overdueRecords = userRecords.filter(record => {
      const dueDate = new Date(record.dueDate)
      return now > dueDate
    })

    if (overdueRecords.length > 0) {
      return {
        hasOverdue: true,
        count: overdueRecords.length,
        records: overdueRecords
      }
    }

    return {
      hasOverdue: false,
      count: 0,
      records: []
    }
  }

  /**
   * 验证借阅条件
   * @param {string} cardNumber - 校园卡号
   * @param {Array} collectionNumbers - 馆藏号数组
   * @returns {Object} - 验证结果
   */
  const validateBorrow = (cardNumber, collectionNumbers) => {
    const bookStore = useBookStore()
    const userDataStore = useUserDataStore()

    // 1. 验证用户
    const user = userDataStore.userList.find(u => u.cardNumber === cardNumber)
    if (!user) {
      return {
        success: false,
        message: '未找到该校园卡对应的用户'
      }
    }

    // 2. 检查用户是否有超期图书
    const overdueCheck = checkUserOverdue(cardNumber)
    if (overdueCheck.hasOverdue) {
      return {
        success: false,
        message: `该用户存在${overdueCheck.count}本超期图书，无法借阅`
      }
    }

    // 3. 验证图书
    const bookValidations = []
    for (const collectionNumber of collectionNumbers) {
      const book = bookStore.getBookByCollectionNumber(collectionNumber)

      if (!book) {
        bookValidations.push({
          collectionNumber,
          success: false,
          message: '未找到该图书'
        })
      } else if (book.status !== '可借阅') {
        bookValidations.push({
          collectionNumber,
          success: false,
          message: `该图书不可借阅（当前状态：${book.status}）`
        })
      } else {
        bookValidations.push({
          collectionNumber,
          success: true,
          book
        })
      }
    }

    // 检查是否所有图书都验证通过
    const failedBooks = bookValidations.filter(v => !v.success)
    if (failedBooks.length > 0) {
      return {
        success: false,
        message: failedBooks.map(f => `${f.collectionNumber}: ${f.message}`).join('\n'),
        bookValidations
      }
    }

    return {
      success: true,
      user,
      books: bookValidations.map(v => v.book),
      bookValidations
    }
  }

  /**
   * 执行借阅操作
   * @param {string} cardNumber - 校园卡号
   * @param {Array} collectionNumbers - 馆藏号数组
   * @param {string} operator - 操作员
   * @returns {Object} - 借阅结果
   */
  const borrowBooks = (cardNumber, collectionNumbers, operator) => {
    const bookStore = useBookStore()

    // 先验证
    const validation = validateBorrow(cardNumber, collectionNumbers)
    if (!validation.success) {
      return validation
    }

    const { user, books } = validation
    const borrowDate = new Date().toISOString()
    const dueDate = calculateDueDate(user.identity)

    // 创建借阅记录并更新图书状态
    const newRecords = []
    for (const book of books) {
      // 创建借阅记录
      const record = {
        id: `BR${Date.now()}${Math.floor(Math.random() * 1000)}`,
        cardNumber: user.cardNumber,
        userName: user.name,
        userIdentity: user.identity,
        accountNumber: user.accountNumber,
        collectionNumber: book.collectionNumber,
        bookTitle: book.title,
        bookAuthor: book.author,
        borrowDate,
        dueDate,
        returnDate: null,
        status: '借阅中',
        operator,
        createTime: borrowDate
      }

      borrowRecords.value.push(record)
      newRecords.push(record)

      // 更新图书状态
      bookStore.updateBookStatus(book.collectionNumber, '已借出', operator)
    }

    // 持久化
    saveToStorage()

    return {
      success: true,
      message: '借阅成功',
      records: newRecords,
      dueDate
    }
  }

  /**
   * 根据馆藏号查询借阅记录
   * @param {string} collectionNumber - 馆藏号
   * @returns {Object|null} - 借阅记录
   */
  const getBorrowRecordByCollection = (collectionNumber) => {
    // 查找该图书的借阅中记录
    return borrowRecords.value.find(record =>
      record.collectionNumber === collectionNumber &&
      record.status === '借阅中'
    ) || null
  }

  /**
   * 计算超期天数
   * @param {string} dueDate - 应还日期（ISO格式）
   * @returns {number} - 超期天数（负数表示未超期）
   */
  const calculateOverdueDays = (dueDate) => {
    const now = new Date()
    const due = new Date(dueDate)
    const diffTime = now - due
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays
  }

  /**
   * 归还图书
   * @param {string} collectionNumber - 馆藏号
   * @param {string} operator - 操作员
   * @returns {Object} - 归还结果
   */
  const returnBook = (collectionNumber, operator) => {
    const bookStore = useBookStore()

    // 查找借阅记录
    const record = getBorrowRecordByCollection(collectionNumber)
    if (!record) {
      return {
        success: false,
        message: '未找到该图书的借阅记录或该图书已归还'
      }
    }

    // 计算超期天数
    const overdueDays = calculateOverdueDays(record.dueDate)
    const returnDate = new Date().toISOString()

    // 更新借阅记录
    record.status = '已归还'
    record.returnDate = returnDate
    record.overdueDays = overdueDays > 0 ? overdueDays : 0
    record.returnOperator = operator

    // 更新图书状态
    bookStore.updateBookStatus(collectionNumber, '可借阅', operator)

    // 持久化
    saveToStorage()

    return {
      success: true,
      message: overdueDays > 0 ? `该图书超期${overdueDays}天` : '归还成功',
      overdueDays: overdueDays > 0 ? overdueDays : 0,
      record
    }
  }

  /**
   * 保存到localStorage
   */
  const saveToStorage = () => {
    localStorage.setItem('borrowRecords', JSON.stringify(borrowRecords.value))
  }

  /**
   * 从localStorage恢复数据
   */
  const restoreFromStorage = () => {
    const saved = localStorage.getItem('borrowRecords')
    if (saved) {
      try {
        borrowRecords.value = JSON.parse(saved)
      } catch (e) {
        console.error('恢复借阅记录失败:', e)
        borrowRecords.value = []
      }
    }
  }

  /**
   * 初始化模拟数据（仅用于演示）
   */
  const initMockData = () => {
    if (borrowRecords.value.length === 0) {
      // 添加一些示例借阅记录
      const mockRecords = [
        {
          id: 'BR20260101120000001',
          cardNumber: 'C2021001',
          userName: '张三',
          userIdentity: '学生',
          accountNumber: '2021001',
          collectionNumber: 'TS20260101130000002',
          bookTitle: 'Java核心技术 卷I',
          bookAuthor: 'Cay S. Horstmann',
          borrowDate: '2025-12-20T12:00:00.000Z',
          dueDate: '2026-02-18T12:00:00.000Z', // 60天后
          returnDate: null,
          status: '借阅中',
          operator: '系统管理员',
          createTime: '2025-12-20T12:00:00.000Z'
        }
      ]

      borrowRecords.value = mockRecords
      saveToStorage()
    }
  }

  return {
    // 状态
    borrowRecords,

    // 方法
    calculateDueDate,
    checkUserOverdue,
    validateBorrow,
    borrowBooks,
    getBorrowRecordByCollection,
    calculateOverdueDays,
    returnBook,
    restoreFromStorage,
    initMockData
  }
})
