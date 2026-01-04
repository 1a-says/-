import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 图书数据状态管理
 * 管理图书的增删改查操作
 */
export const useBookStore = defineStore('book', () => {
  // 图书列表数据
  const bookList = ref([])

  /**
   * 生成唯一馆藏号
   * 规则：TS + 时间戳(年月日时分秒) + 随机3位数
   * 示例：TS20260102170000123
   */
  const generateCollectionNumber = () => {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const hours = String(now.getHours()).padStart(2, '0')
    const minutes = String(now.getMinutes()).padStart(2, '0')
    const seconds = String(now.getSeconds()).padStart(2, '0')
    const random = String(Math.floor(Math.random() * 1000)).padStart(3, '0')

    return `TS${year}${month}${day}${hours}${minutes}${seconds}${random}`
  }

  /**
   * 添加图书入库
   * @param {Object} bookData - 图书信息
   * @returns {Object} - 入库结果
   */
  const addBook = (bookData) => {
    const collectionNumber = generateCollectionNumber()

    const newBook = {
      collectionNumber,        // 馆藏号
      isbn: bookData.isbn,     // ISBN
      title: bookData.title,   // 书名
      author: bookData.author, // 作者
      publisher: bookData.publisher,  // 出版社
      location: bookData.location,    // 馆藏位置
      status: '可借阅',         // 当前状态
      createTime: new Date().toISOString(),  // 创建时间
      updateTime: new Date().toISOString()   // 更新时间
    }

    bookList.value.push(newBook)

    // 持久化到localStorage
    saveToStorage()

    return {
      success: true,
      collectionNumber,
      message: '入库成功'
    }
  }

  /**
   * 查询图书（模糊查询）
   * @param {Object} query - 查询条件 { isbn, title }
   * @returns {Array} - 查询结果
   */
  const queryBooks = (query) => {
    let results = [...bookList.value]

    // ISBN模糊查询
    if (query.isbn && query.isbn.trim()) {
      const searchISBN = query.isbn.trim().toLowerCase()
      results = results.filter(book =>
        book.isbn.toLowerCase().includes(searchISBN)
      )
    }

    // 书名模糊查询
    if (query.title && query.title.trim()) {
      const searchTitle = query.title.trim().toLowerCase()
      results = results.filter(book =>
        book.title.toLowerCase().includes(searchTitle)
      )
    }

    return results
  }

  /**
   * 根据馆藏号查询图书
   * @param {string} collectionNumber - 馆藏号
   * @returns {Object|null} - 图书信息
   */
  const getBookByCollectionNumber = (collectionNumber) => {
    return bookList.value.find(book =>
      book.collectionNumber === collectionNumber
    ) || null
  }

  /**
   * 更新图书状态
   * @param {string} collectionNumber - 馆藏号
   * @param {string} newStatus - 新状态
   * @param {string} operator - 操作人
   * @returns {Object} - 更新结果
   */
  const updateBookStatus = (collectionNumber, newStatus, operator) => {
    const book = bookList.value.find(b =>
      b.collectionNumber === collectionNumber
    )

    if (!book) {
      return {
        success: false,
        message: '未找到该图书'
      }
    }

    // 更新状态
    book.status = newStatus
    book.updateTime = new Date().toISOString()

    // 记录操作历史
    if (!book.statusHistory) {
      book.statusHistory = []
    }
    book.statusHistory.push({
      status: newStatus,
      operator,
      operateTime: new Date().toISOString()
    })

    // 持久化
    saveToStorage()

    return {
      success: true,
      message: '状态修改成功'
    }
  }

  /**
   * 保存到localStorage
   */
  const saveToStorage = () => {
    localStorage.setItem('bookList', JSON.stringify(bookList.value))
  }

  /**
   * 从localStorage恢复数据
   */
  const restoreFromStorage = () => {
    const saved = localStorage.getItem('bookList')
    if (saved) {
      try {
        bookList.value = JSON.parse(saved)
      } catch (e) {
        console.error('恢复图书数据失败:', e)
        bookList.value = []
      }
    }
  }

  /**
   * 初始化模拟数据（仅用于演示）
   */
  const initMockData = () => {
    if (bookList.value.length === 0) {
      // 添加一些示例图书
      const mockBooks = [
        {
          collectionNumber: 'TS20260101120000001',
          isbn: '978-7-115-54602-3',
          title: '算法导论（原书第3版）',
          author: 'Thomas H.Cormen',
          publisher: '机械工业出版社',
          location: 'A区-3架-2层',
          status: '可借阅',
          createTime: '2026-01-01T12:00:00.000Z',
          updateTime: '2026-01-01T12:00:00.000Z'
        },
        {
          collectionNumber: 'TS20260101130000002',
          isbn: '978-7-111-54742-6',
          title: 'Java核心技术 卷I',
          author: 'Cay S. Horstmann',
          publisher: '机械工业出版社',
          location: 'A区-5架-1层',
          status: '已借出',
          createTime: '2026-01-01T13:00:00.000Z',
          updateTime: '2026-01-01T13:00:00.000Z'
        },
        {
          collectionNumber: 'TS20260101140000003',
          isbn: '978-7-111-54493-7',
          title: '深入理解计算机系统',
          author: 'Randal E. Bryant',
          publisher: '机械工业出版社',
          location: 'A区-3架-3层',
          status: '可借阅',
          createTime: '2026-01-01T14:00:00.000Z',
          updateTime: '2026-01-01T14:00:00.000Z'
        }
      ]

      bookList.value = mockBooks
      saveToStorage()
    }
  }

  return {
    // 状态
    bookList,

    // 方法
    generateCollectionNumber,
    addBook,
    queryBooks,
    getBookByCollectionNumber,
    updateBookStatus,
    restoreFromStorage,
    initMockData
  }
})
