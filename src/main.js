import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './styles/global.css'

// 创建Vue应用实例
const app = createApp(App)

// 注册全局插件
app.use(createPinia())  // 状态管理
app.use(router)         // 路由
app.use(ElementPlus)    // Element Plus组件库

app.mount('#app')
