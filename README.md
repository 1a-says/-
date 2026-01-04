# 高校图书馆管理系统

基于 Vue3 + Element Plus 的轻量级图书馆管理系统

## 测试账号

系统已内置以下测试账号供您测试：

### 管理员账号
- 账号：`admin`
- 密码：`admin123`

### 普通用户账号
- 账号：`2021001` 密码：`123456` （学生张三）
- 账号：`2021002` 密码：`123456` （学生李四）
- 账号：`T001` 密码：`123456` （王老师）

## 安装运行

由于npm权限问题，请按以下步骤操作：

### 方式一：使用管理员权限运行
1. 以管理员身份打开 PowerShell
2. 进入项目目录：
   ```powershell
   cd "d:\图书馆管理系统"
   ```
3. 安装依赖：
   ```powershell
   npm install
   ```
4. 启动开发服务器：
   ```powershell
   npm run dev
   ```

### 方式二：使用 yarn（推荐）
如果您安装了 yarn，可以使用以下命令：

1. 进入项目目录：
   ```powershell
   cd "d:\图书馆管理系统"
   ```
2. 安装依赖：
   ```powershell
   yarn
   ```
3. 启动开发服务器：
   ```powershell
   yarn dev
   ```

### 方式三：清理 npm 缓存后重试
```powershell
npm cache clean --force
cd "d:\图书馆管理系统"
npm install
npm run dev
```

## 项目结构

```
图书馆管理系统/
├── src/
│   ├── views/              # 页面组件
│   │   ├── Login.vue       # 登录页面
│   │   ├── AdminHome.vue   # 管理员首页
│   │   └── UserHome.vue    # 普通用户首页
│   ├── stores/             # 状态管理
│   │   └── user.js         # 用户状态管理
│   ├── router/             # 路由配置
│   │   └── index.js        # 路由定义
│   ├── styles/             # 全局样式
│   │   └── global.css      # 全局CSS
│   ├── App.vue             # 根组件
│   └── main.js             # 入口文件
├── index.html              # HTML模板
├── vite.config.js          # Vite配置
└── package.json            # 项目依赖
```

## 核心功能

### 1. 登录页面 (Login.vue)
- ✅ 账号密码输入（支持学号/工号/管理员账号）
- ✅ 密码SHA256加密校验
- ✅ 连续3次错误锁定30分钟
- ✅ 忘记密码提示（关联管理员重置）
- ✅ 根据角色跳转不同首页

### 2. 管理员首页 (AdminHome.vue)
- ✅ 全功能菜单（图书管理、借阅管理、用户管理、统计报表、系统设置）
- ✅ 数据统计展示
- ✅ 快捷操作按钮

### 3. 普通用户首页 (UserHome.vue)
- ✅ 简化菜单（图书查询、借阅管理）
- ✅ 个人借阅统计
- ✅ 在借图书列表
- ✅ 续借功能

### 4. 全局配置
- ✅ Pinia状态管理（用户信息、角色、登录状态）
- ✅ Vue Router路由守卫（权限验证）
- ✅ 简洁办公风格样式
- ✅ Element Plus组件库

## 技术栈

- **前端框架**: Vue 3.4.0 (Composition API)
- **UI组件库**: Element Plus 2.5.0
- **状态管理**: Pinia 2.1.7
- **路由管理**: Vue Router 4.2.5
- **加密库**: Crypto-JS 4.2.0
- **构建工具**: Vite 5.0.0

## 浏览器兼容性

- ✅ Chrome 90+ （推荐）
- ✅ Edge 90+
- ✅ Firefox 88+

## 后续扩展建议

1. **后端接口集成**
   - 将模拟数据替换为真实API调用
   - 添加 axios 进行HTTP请求
   
2. **功能完善**
   - 实现图书查询、借阅、归还等完整流程
   - 添加图书分类、搜索、推荐功能
   - 完善用户管理和权限控制

3. **性能优化**
   - 添加路由懒加载
   - 实现虚拟滚动（长列表优化）
   - 添加请求防抖和节流

## 注意事项

- 本系统为前端Demo，数据存储在浏览器localStorage中
- 密码锁定功能基于前端实现，刷新页面会重置
- 生产环境需要连接真实后端API
- 建议使用Chrome浏览器获得最佳体验

## 开发规范

- 代码注释完整，便于维护
- 遵循Vue3 Composition API最佳实践
- 使用ES6+语法
- 统一使用2空格缩进
