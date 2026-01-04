# 图书馆管理系统 API 接口文档

## 文档信息

- **项目名称**：轻量级高校图书馆管理系统
- **技术栈**：SpringBoot 2.7+ / MySQL 8.0 / Redis / JWT
- **前端技术**：Vue3 + Element Plus
- **文档版本**：v1.0
- **更新时间**：2026-01-02

---

## 目录

1. [认证授权模块](#1-认证授权模块)
2. [图书管理模块](#2-图书管理模块)
3. [用户管理模块](#3-用户管理模块)
4. [借阅管理模块](#4-借阅管理模块)
5. [查询统计模块](#5-查询统计模块)
6. [系统配置模块](#6-系统配置模块)
7. [通用响应格式](#7-通用响应格式)
8. [错误码说明](#8-错误码说明)

---

## 1. 认证授权模块

### 1.1 用户登录

**接口地址**：`POST /api/auth/login`

**接口描述**：用户登录认证，支持管理员和普通用户登录

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| account | String | 是 | 账号（管理员账号或学号/工号） |
| password | String | 是 | 密码（前端SHA256加密后传输） |

**请求示例**：

```json
{
  "account": "admin",
  "password": "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码（200成功） |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| data.token | String | JWT Token |
| data.userInfo | Object | 用户信息 |
| data.userInfo.account | String | 账号 |
| data.userInfo.name | String | 姓名 |
| data.userInfo.role | String | 角色（admin/user） |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "account": "admin",
      "name": "系统管理员",
      "role": "admin"
    }
  }
}
```

**失败响应示例（账号锁定）**：

```json
{
  "code": 403,
  "message": "账号已被锁定，剩余28分钟",
  "data": {
    "locked": true,
    "remainingTime": 28
  }
}
```

**业务规则**：

- 连续3次密码错误，账号锁定30分钟
- 密码使用SHA256加密
- Token有效期2小时

---

### 1.2 用户登出

**接口地址**：`POST /api/auth/logout`

**接口描述**：用户退出登录，清除Token

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**响应示例**：

```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

---

## 2. 图书管理模块

### 2.1 图书入库

**接口地址**：`POST /api/books`

**接口描述**：单本图书入库，自动生成馆藏号

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isbn | String | 是 | ISBN号（支持10位或13位） |
| title | String | 是 | 书名 |
| author | String | 是 | 作者 |
| publisher | String | 是 | 出版社 |
| location | String | 是 | 馆藏位置（如：A区-3架-2层） |

**请求示例**：

```json
{
  "isbn": "978-7-115-54602-3",
  "title": "算法导论（原书第3版）",
  "author": "Thomas H.Cormen",
  "publisher": "机械工业出版社",
  "location": "A区-3架-2层"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 图书信息 |
| data.collectionNumber | String | 馆藏号（自动生成） |
| data.isbn | String | ISBN |
| data.title | String | 书名 |
| data.author | String | 作者 |
| data.publisher | String | 出版社 |
| data.location | String | 馆藏位置 |
| data.status | String | 当前状态 |
| data.createTime | String | 创建时间 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "入库成功",
  "data": {
    "collectionNumber": "TS20260102170523001",
    "isbn": "978-7-115-54602-3",
    "title": "算法导论（原书第3版）",
    "author": "Thomas H.Cormen",
    "publisher": "机械工业出版社",
    "location": "A区-3架-2层",
    "status": "可借阅",
    "createTime": "2026-01-02T17:05:23.000Z"
  }
}
```

**业务规则**：

- 馆藏号规则：TS + 年月日时分秒(14位) + 随机3位数
- 默认状态：可借阅
- ISBN号需符合标准格式

---

### 2.2 图书查询（分页）

**接口地址**：`GET /api/books`

**接口描述**：分页查询图书，支持ISBN和书名模糊查询

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| isbn | String | 否 | ISBN号（模糊查询） |
| title | String | 否 | 书名（模糊查询） |
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页数量（默认10） |

**请求示例**：

```
GET /api/books?title=算法&page=1&size=10
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 分页数据 |
| data.total | Integer | 总记录数 |
| data.page | Integer | 当前页码 |
| data.size | Integer | 每页数量 |
| data.list | Array | 图书列表 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "page": 1,
    "size": 10,
    "list": [
      {
        "collectionNumber": "TS20260101120000001",
        "isbn": "978-7-115-54602-3",
        "title": "算法导论（原书第3版）",
        "author": "Thomas H.Cormen",
        "publisher": "机械工业出版社",
        "location": "A区-3架-2层",
        "status": "可借阅",
        "createTime": "2026-01-01T12:00:00.000Z",
        "updateTime": "2026-01-01T12:00:00.000Z"
      }
    ]
  }
}
```

---

### 2.3 根据馆藏号查询图书

**接口地址**：`GET /api/books/{collectionNumber}`

**接口描述**：根据馆藏号查询图书详细信息，包含状态历史

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| collectionNumber | String | 是 | 馆藏号 |

**请求示例**：

```
GET /api/books/TS20260101120000001
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 图书详细信息 |
| data.collectionNumber | String | 馆藏号 |
| data.isbn | String | ISBN |
| data.title | String | 书名 |
| data.author | String | 作者 |
| data.publisher | String | 出版社 |
| data.location | String | 馆藏位置 |
| data.status | String | 当前状态 |
| data.statusHistory | Array | 状态历史记录 |
| data.createTime | String | 创建时间 |
| data.updateTime | String | 更新时间 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "collectionNumber": "TS20260101120000001",
    "isbn": "978-7-115-54602-3",
    "title": "算法导论（原书第3版）",
    "author": "Thomas H.Cormen",
    "publisher": "机械工业出版社",
    "location": "A区-3架-2层",
    "status": "可借阅",
    "statusHistory": [
      {
        "status": "已借出",
        "operator": "admin",
        "operateTime": "2026-01-01T14:00:00.000Z"
      },
      {
        "status": "可借阅",
        "operator": "admin",
        "operateTime": "2026-01-02T10:00:00.000Z"
      }
    ],
    "createTime": "2026-01-01T12:00:00.000Z",
    "updateTime": "2026-01-02T10:00:00.000Z"
  }
}
```

---

### 2.4 更新图书状态

**接口地址**：`PUT /api/books/{collectionNumber}/status`

**接口描述**：修改图书状态，记录操作历史

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| collectionNumber | String | 是 | 馆藏号 |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 新状态（可借阅/已借出/遗失/损坏/维护中） |
| operator | String | 是 | 操作员账号 |

**请求示例**：

```json
{
  "status": "维护中",
  "operator": "admin"
}
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "状态修改成功",
  "data": {
    "collectionNumber": "TS20260101120000001",
    "status": "维护中",
    "updateTime": "2026-01-02T17:10:00.000Z"
  }
}
```

**业务规则**：

- 状态枚举：可借阅、已借出、遗失、损坏、维护中
- 记录状态变更历史
- 已借出状态只能由借阅系统修改

---

## 3. 用户管理模块

### 3.1 用户录入

**接口地址**：`POST /api/users`

**接口描述**：录入新用户，自动生成初始密码

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountNumber | String | 是 | 学号/工号 |
| name | String | 是 | 姓名 |
| identity | String | 是 | 身份（教师/学生） |
| cardNumber | String | 是 | 校园卡号 |

**请求示例**：

```json
{
  "accountNumber": "2021001",
  "name": "张三",
  "identity": "学生",
  "cardNumber": "C2021001"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 用户信息 |
| data.accountNumber | String | 学号/工号 |
| data.name | String | 姓名 |
| data.identity | String | 身份 |
| data.cardNumber | String | 校园卡号 |
| data.initialPassword | String | 初始密码（明文） |
| data.role | String | 角色 |
| data.status | String | 账号状态 |
| data.createTime | String | 创建时间 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "用户录入成功",
  "data": {
    "accountNumber": "2021001",
    "name": "张三",
    "identity": "学生",
    "cardNumber": "C2021001",
    "initialPassword": "021001",
    "role": "user",
    "status": "正常",
    "createTime": "2026-01-02T17:15:00.000Z"
  }
}
```

**业务规则**：

- 初始密码规则：学号/工号后6位（不足6位则使用全部）
- 密码使用SHA256加密存储
- 校园卡号和学号/工号必须唯一
- 默认角色：user（普通用户）

---

### 3.2 用户查询

**接口地址**：`GET /api/users`

**接口描述**：根据学号/工号和校园卡号查询用户

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountNumber | String | 是 | 学号/工号 |
| cardNumber | String | 是 | 校园卡号 |

**请求示例**：

```
GET /api/users?accountNumber=2021001&cardNumber=C2021001
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "accountNumber": "2021001",
    "name": "张三",
    "identity": "学生",
    "cardNumber": "C2021001",
    "role": "user",
    "status": "正常",
    "createTime": "2026-01-01T10:00:00.000Z"
  }
}
```

---

### 3.3 重置用户密码

**接口地址**：`PUT /api/users/reset-password`

**接口描述**：重置用户密码为初始密码

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountNumber | String | 是 | 学号/工号 |
| cardNumber | String | 是 | 校园卡号 |

**请求示例**：

```json
{
  "accountNumber": "2021001",
  "cardNumber": "C2021001"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 重置结果 |
| data.userName | String | 用户姓名 |
| data.userIdentity | String | 用户身份 |
| data.initialPassword | String | 新密码（明文） |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": {
    "userName": "张三",
    "userIdentity": "学生",
    "initialPassword": "021001"
  }
}
```

**业务规则**：

- 需同时提供学号/工号和校园卡号验证用户身份
- 重置为初始密码（学号/工号后6位）
- 密码SHA256加密存储

---

### 3.4 用户列表查询

**接口地址**：`GET /api/users/list`

**接口描述**：分页查询用户列表

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页数量（默认10） |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "list": [
      {
        "accountNumber": "2021001",
        "name": "张三",
        "identity": "学生",
        "cardNumber": "C2021001",
        "role": "user",
        "status": "正常",
        "createTime": "2026-01-01T10:00:00.000Z"
      }
    ]
  }
}
```

---

## 4. 借阅管理模块

### 4.1 借阅验证

**接口地址**：`POST /api/borrow/validate`

**接口描述**：验证借阅条件，检查用户状态和图书状态

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cardNumber | String | 是 | 校园卡号 |
| collectionNumbers | Array | 是 | 馆藏号数组 |

**请求示例**：

```json
{
  "cardNumber": "C2021001",
  "collectionNumbers": [
    "TS20260101120000001",
    "TS20260101130000002"
  ]
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 验证结果 |
| data.user | Object | 用户信息 |
| data.books | Array | 图书列表 |
| data.dueDate | String | 应还日期（ISO格式） |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "验证通过",
  "data": {
    "user": {
      "accountNumber": "2021001",
      "name": "张三",
      "identity": "学生",
      "cardNumber": "C2021001"
    },
    "books": [
      {
        "collectionNumber": "TS20260101120000001",
        "title": "算法导论（原书第3版）",
        "author": "Thomas H.Cormen",
        "status": "可借阅"
      }
    ],
    "dueDate": "2026-03-03T17:20:00.000Z"
  }
}
```

**失败响应示例（用户超期）**：

```json
{
  "code": 400,
  "message": "该用户存在2本超期图书，无法借阅",
  "data": null
}
```

**业务规则**：

- 验证用户是否存在
- 检查用户是否有超期图书
- 验证图书状态是否可借阅
- 检查借阅数量限制（根据系统配置）
- 根据用户身份和系统配置计算应还日期

---

### 4.2 执行借阅

**接口地址**：`POST /api/borrow`

**接口描述**：执行借阅操作，创建借阅记录并更新图书状态

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cardNumber | String | 是 | 校园卡号 |
| collectionNumbers | Array | 是 | 馆藏号数组 |
| operator | String | 是 | 操作员账号 |

**请求示例**：

```json
{
  "cardNumber": "C2021001",
  "collectionNumbers": ["TS20260101120000001"],
  "operator": "admin"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 借阅结果 |
| data.records | Array | 借阅记录列表 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "借阅成功",
  "data": {
    "records": [
      {
        "id": "BR20260102172001",
        "cardNumber": "C2021001",
        "userName": "张三",
        "userIdentity": "学生",
        "accountNumber": "2021001",
        "collectionNumber": "TS20260101120000001",
        "bookTitle": "算法导论（原书第3版）",
        "bookAuthor": "Thomas H.Cormen",
        "borrowDate": "2026-01-02T17:20:00.000Z",
        "dueDate": "2026-03-03T17:20:00.000Z",
        "status": "借阅中"
      }
    ]
  }
}
```

**业务规则**：

- 先调用验证接口验证条件
- 创建借阅记录
- 更新图书状态为"已借出"
- 记录操作日志
- 应还日期根据用户身份和系统配置计算

---

### 4.3 查询借阅记录

**接口地址**：`GET /api/borrow/records`

**接口描述**：根据馆藏号查询借阅记录

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| collectionNumber | String | 是 | 馆藏号 |

**请求示例**：

```
GET /api/borrow/records?collectionNumber=TS20260101120000001
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": "BR20260102172001",
    "userName": "张三",
    "userIdentity": "学生",
    "cardNumber": "C2021001",
    "accountNumber": "2021001",
    "collectionNumber": "TS20260101120000001",
    "bookTitle": "算法导论（原书第3版）",
    "bookAuthor": "Thomas H.Cormen",
    "borrowDate": "2026-01-02T17:20:00.000Z",
    "dueDate": "2026-03-03T17:20:00.000Z",
    "returnDate": null,
    "status": "借阅中",
    "operator": "admin"
  }
}
```

---

### 4.4 图书归还

**接口地址**：`POST /api/borrow/return`

**接口描述**：归还图书，计算超期天数

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| collectionNumber | String | 是 | 馆藏号 |
| operator | String | 是 | 操作员账号 |

**请求示例**：

```json
{
  "collectionNumber": "TS20260101120000001",
  "operator": "admin"
}
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 归还结果 |
| data.record | Object | 借阅记录 |

**成功响应示例（正常归还）**：

```json
{
  "code": 200,
  "message": "归还成功",
  "data": {
    "record": {
      "id": "BR20260102172001",
      "status": "已归还",
      "returnDate": "2026-02-28T17:25:00.000Z",
      "overdueDays": 0
    }
  }
}
```

**成功响应示例（超期归还）**：

```json
{
  "code": 200,
  "message": "该图书超期5天",
  "data": {
    "record": {
      "id": "BR20260102172001",
      "status": "已归还",
      "returnDate": "2026-03-08T17:25:00.000Z",
      "overdueDays": 5
    }
  }
}
```

**业务规则**：

- 查找借阅中的记录
- 计算超期天数
- 更新借阅记录状态为"已归还"
- 更新图书状态为"可借阅"
- 记录归还时间和操作员

---

### 4.5 检查用户超期图书

**接口地址**：`GET /api/borrow/check-overdue`

**接口描述**：检查用户是否有超期图书

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| cardNumber | String | 是 | 校园卡号 |

**请求示例**：

```
GET /api/borrow/check-overdue?cardNumber=C2021001
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "检查完成",
  "data": {
    "hasOverdue": true,
    "count": 2,
    "records": [
      {
        "collectionNumber": "TS20260101120000001",
        "bookTitle": "算法导论（原书第3版）",
        "dueDate": "2026-02-28T12:00:00.000Z",
        "overdueDays": 5
      }
    ]
  }
}
```

---

## 5. 查询统计模块

### 5.1 个人借阅记录查询

**接口地址**：`GET /api/borrow/my-records`

**接口描述**：查询个人借阅记录，包含统计信息

**权限要求**：普通用户/管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountNumber | String | 是 | 学号/工号 |

**请求示例**：

```
GET /api/borrow/my-records?accountNumber=2021001
```

**响应参数**：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 响应码 |
| message | String | 响应消息 |
| data | Object | 查询结果 |
| data.statistics | Object | 统计信息 |
| data.statistics.currentBorrowing | Integer | 当前在借 |
| data.statistics.totalBorrowed | Integer | 历史借阅 |
| data.statistics.overdueCount | Integer | 超期图书 |
| data.records | Array | 借阅记录列表 |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "statistics": {
      "currentBorrowing": 2,
      "totalBorrowed": 15,
      "overdueCount": 0
    },
    "records": [
      {
        "id": "BR20260102172001",
        "collectionNumber": "TS20260101120000001",
        "bookTitle": "算法导论（原书第3版）",
        "bookAuthor": "Thomas H.Cormen",
        "borrowDate": "2026-01-02T17:20:00.000Z",
        "dueDate": "2026-03-03T17:20:00.000Z",
        "returnDate": null,
        "status": "借阅中",
        "remainingDays": 30,
        "overdueDays": 0
      }
    ]
  }
}
```

---

### 5.2 全量借阅记录查询

**接口地址**：`GET /api/borrow/all-records`

**接口描述**：管理员查询全量借阅记录，支持分页和模糊查询

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| accountNumber | String | 否 | 学号/工号（模糊查询） |
| keyword | String | 否 | 馆藏号/书名（模糊查询） |
| page | Integer | 否 | 页码（默认1） |
| size | Integer | 否 | 每页数量（默认10） |

**请求示例**：

```
GET /api/borrow/all-records?keyword=算法&page=1&size=10
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 500,
    "page": 1,
    "size": 10,
    "list": [
      {
        "id": "BR20260102172001",
        "userName": "张三",
        "accountNumber": "2021001",
        "userIdentity": "学生",
        "cardNumber": "C2021001",
        "collectionNumber": "TS20260101120000001",
        "bookTitle": "算法导论（原书第3版）",
        "bookAuthor": "Thomas H.Cormen",
        "borrowDate": "2026-01-02T17:20:00.000Z",
        "dueDate": "2026-03-03T17:20:00.000Z",
        "returnDate": null,
        "status": "借阅中",
        "overdueDays": 0,
        "operator": "admin"
      }
    ]
  }
}
```

---

### 5.3 借阅TOP5统计

**接口地址**：`GET /api/statistics/top-books`

**接口描述**：统计借阅TOP5图书

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| dimension | String | 否 | 时间维度（week/month，默认week） |

**请求示例**：

```
GET /api/statistics/top-books?dimension=week
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "统计完成",
  "data": {
    "dimension": "week",
    "topBooks": [
      {
        "collectionNumber": "TS20260101120000001",
        "bookTitle": "算法导论（原书第3版）",
        "author": "Thomas H.Cormen",
        "borrowCount": 45,
        "percentage": 27
      },
      {
        "collectionNumber": "TS20260101130000002",
        "bookTitle": "Java核心技术 卷I",
        "author": "Cay S. Horstmann",
        "borrowCount": 38,
        "percentage": 23
      },
      {
        "collectionNumber": "TS20260101140000003",
        "bookTitle": "深入理解计算机系统",
        "author": "Randal E. Bryant",
        "borrowCount": 32,
        "percentage": 19
      },
      {
        "collectionNumber": "TS20260102150000004",
        "bookTitle": "Python编程从入门到实践",
        "author": "Eric Matthes",
        "borrowCount": 28,
        "percentage": 17
      },
      {
        "collectionNumber": "TS20260102160000005",
        "bookTitle": "设计模式：可复用面向对象软件的基础",
        "author": "Erich Gamma",
        "borrowCount": 25,
        "percentage": 15
      }
    ]
  }
}
```

---

## 6. 系统配置模块

### 6.1 获取系统配置

**接口地址**：`GET /api/config`

**接口描述**：获取当前系统配置参数

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "teacherBorrowDays": 90,
    "studentBorrowDays": 60,
    "maxBorrowCount": 5,
    "defaultConfig": {
      "teacherBorrowDays": 90,
      "studentBorrowDays": 60,
      "maxBorrowCount": 5
    }
  }
}
```

---

### 6.2 更新系统配置

**接口地址**：`PUT /api/config`

**接口描述**：更新系统配置参数

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 | 范围 |
|--------|------|------|------|------|
| teacherBorrowDays | Integer | 是 | 教师借阅期限（天） | 30-365 |
| studentBorrowDays | Integer | 是 | 学生借阅期限（天） | 30-365 |
| maxBorrowCount | Integer | 是 | 单次最大借阅数（本） | 1-20 |

**请求示例**：

```json
{
  "teacherBorrowDays": 120,
  "studentBorrowDays": 90,
  "maxBorrowCount": 10
}
```

**成功响应示例**：

```json
{
  "code": 200,
  "message": "参数保存成功",
  "data": {
    "teacherBorrowDays": 120,
    "studentBorrowDays": 90,
    "maxBorrowCount": 10
  }
}
```

**业务规则**：

- 教师借阅期限：30-365天
- 学生借阅期限：30-365天
- 单次最大借阅数：1-20本
- 配置修改立即生效
- 已借阅的图书不受影响

---

### 6.3 恢复默认配置

**接口地址**：`POST /api/config/reset`

**接口描述**：恢复系统配置为默认值

**权限要求**：管理员

**请求头**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Authorization | String | 是 | Bearer {token} |

**成功响应示例**：

```json
{
  "code": 200,
  "message": "已恢复默认参数",
  "data": {
    "teacherBorrowDays": 90,
    "studentBorrowDays": 60,
    "maxBorrowCount": 5
  }
}
```

---

## 7. 通用响应格式

### 7.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 响应数据
  }
}
```

### 7.2 失败响应

```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 7.3 认证失败

```json
{
  "code": 401,
  "message": "未授权，请先登录",
  "data": null
}
```

### 7.4 权限不足

```json
{
  "code": 403,
  "message": "权限不足",
  "data": null
}
```

---

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或过期） |
| 403 | 权限不足或账号锁定 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务错误码（自定义）

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1003 | 账号已锁定 |
| 2001 | 图书不存在 |
| 2002 | 图书状态不可借阅 |
| 2003 | ISBN格式错误 |
| 3001 | 用户存在超期图书 |
| 3002 | 超过最大借阅数量 |
| 3003 | 借阅记录不存在 |
| 4001 | 校园卡号已存在 |
| 4002 | 学号/工号已存在 |
| 5001 | 配置参数超出范围 |

---

## 9. 附录

### 9.1 数据字典

#### 图书状态枚举
- `可借阅`：图书可以被借阅
- `已借出`：图书已被借出
- `遗失`：图书遗失
- `损坏`：图书损坏
- `维护中`：图书维护中

#### 用户身份枚举
- `教师`：教职工
- `学生`：学生

#### 用户角色枚举
- `admin`：管理员
- `user`：普通用户

#### 借阅状态枚举
- `借阅中`：图书借阅中
- `已归还`：图书已归还

#### 账号状态枚举
- `正常`：账号正常
- `锁定`：账号锁定
- `停用`：账号停用

### 9.2 馆藏号生成规则

格式：`TS + YYYYMMDDHHMMSS + XXX`

- `TS`：前缀（固定）
- `YYYYMMDDHHMMSS`：年月日时分秒（14位）
- `XXX`：随机3位数（000-999）

示例：`TS20260102170523001`

### 9.3 密码加密规则

1. **前端加密**：用户输入密码 → SHA256加密 → 传输给后端
2. **后端加密**：接收加密密码 → 加盐 → SHA256再次加密 → 存储

初始密码规则：
- 学号/工号长度 ≥ 6位：取后6位
- 学号/工号长度 < 6位：使用全部字符

示例：
- `2021001` → 初始密码：`021001`
- `T001` → 初始密码：`T001`

### 9.4 应还日期计算规则

根据用户身份和系统配置计算：

- **教师**：当前日期 + `teacherBorrowDays` 天
- **学生**：当前日期 + `studentBorrowDays` 天

默认配置：
- 教师：90天
- 学生：60天

### 9.5 超期计算规则

```
超期天数 = 当前日期 - 应还日期
```

- 超期天数 > 0：超期
- 超期天数 ≤ 0：未超期

---

## 10. 版本更新记录

| 版本 | 日期 | 更新内容 |
|------|------|----------|
| v1.0 | 2026-01-02 | 初始版本，包含所有核心接口 |

---

**文档结束**
