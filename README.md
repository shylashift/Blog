# Blog 前端项目

这是一个使用 Vue 3 + TypeScript + Vite 开发的博客系统前端项目。

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- TypeScript - JavaScript 的超集，添加了类型系统
- Vite - 下一代前端构建工具
- Vue Router - Vue.js 的官方路由
- Pinia - Vue 的状态管理库
- Ant Design Vue - 基于 Ant Design 的 Vue UI 组件库
- Axios - 基于 Promise 的 HTTP 客户端

## 项目结构

```
blog-front/
├── src/
│   ├── assets/      # 静态资源
│   ├── components/  # 公共组件
│   ├── layouts/     # 布局组件
│   ├── router/      # 路由配置
│   ├── stores/      # Pinia 状态管理
│   ├── types/       # TypeScript 类型定义
│   ├── utils/       # 工具函数
│   └── views/       # 页面组件
├── public/          # 公共资源
└── vite.config.ts   # Vite 配置文件
```

## 功能规划

### 第一阶段：基础功能
- [ ] 用户认证（登录/注册）
- [ ] 文章列表展示
- [ ] 文章详情页
- [ ] 个人中心
- [ ] 基础布局

### 第二阶段：内容管理
- [ ] 文章发布/编辑
- [ ] 文章分类管理
- [ ] 标签管理
- [ ] 评论功能

### 第三阶段：用户交互
- [ ] 文章点赞
- [ ] 文章收藏
- [ ] 用户关注
- [ ] 消息通知

### 第四阶段：高级功能
- [ ] 搜索功能
- [ ] 数据统计
- [ ] 主题切换
- [ ] 移动端适配

## 开发指南

1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

## 代码规范

- 使用 ESLint 进行代码检查
- 使用 Prettier 进行代码格式化
- 遵循 Vue 3 组合式 API 的最佳实践
- 使用 TypeScript 类型注解
- 组件和函数必须添加注释说明

## 性能优化

- 路由懒加载
- 组件按需加载
- 图片懒加载
- 合理使用缓存
- 代码分割
