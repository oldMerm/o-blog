export const mockArticles = [
  { id: 'mock-001', articleName: '老鱼人博客 v1.2.7 更新公告', articleDecr: '新增移动端页面支持、深色模式优化、修复若干已知问题，提升整体使用体验', groupName: '站点公告', articleType: 0 },
  { id: 'mock-002', articleName: '关于服务器维护的通知', articleDecr: '将于本周六凌晨 2:00-5:00 进行数据库迁移维护，期间部分功能可能不可用', groupName: '站点公告', articleType: 0 },
  { id: 'mock-003', articleName: '社区规范修订 v3.0', articleDecr: '为了更好地维护社区氛围，我们对社区规范进行了新一轮修订，请各位用户仔细阅读', groupName: '站点公告', articleType: 0 },
  { id: 'mock-101', articleName: 'Vue 3 组合式 API 完全指南', articleDecr: '从 ref/reactive 到 provide/inject，全面掌握 Vue 3 组合式 API 的核心概念与最佳实践', groupName: '前端技术', articleType: 1 },
  { id: 'mock-102', articleName: 'TypeScript 5.x 新特性速览', articleDecr: '装饰器正式稳定、const 类型参数、isolatedDeclarations 等新特性的详细介绍与使用示例', groupName: '前端技术', articleType: 1 },
  { id: 'mock-103', articleName: 'CSS Container Queries 实战', articleDecr: '告别媒体查询的局限性，用容器查询构建真正自适应的组件化页面', groupName: '前端技术', articleType: 1 },
  { id: 'mock-104', articleName: 'Node.js 性能调优经验分享', articleDecr: '从事件循环、内存泄漏、CPU 分析等角度分享 Node.js 服务端性能调优的实战经验', groupName: '后端开发', articleType: 1 },
  { id: 'mock-105', articleName: 'Docker 容器化部署最佳实践', articleDecr: '多阶段构建、健康检查、日志管理、安全配置等 Docker 生产环境部署的核心要点', groupName: '运维部署', articleType: 1 },
  { id: 'mock-106', articleName: '从零搭建 Monorepo 工程', articleDecr: '使用 pnpm workspace + turborepo 搭建前端 Monorepo 工程，涵盖依赖管理、构建缓存、发布流程', groupName: '工程化', articleType: 1 },
  { id: 'mock-201', articleName: '周末徒步记：穿越东西涌', articleDecr: '深圳最美海岸线徒步路线分享，全程 8 公里的礁石海岸，风景绝佳但体力消耗也不小', groupName: '旅行日记', articleType: 2 },
  { id: 'mock-202', articleName: '家庭咖啡角搭建指南', articleDecr: '从意式机到手冲壶，从磨豆机到水质选择，分享我的家庭咖啡角搭建心路历程', groupName: '生活分享', articleType: 2 },
  { id: 'mock-203', articleName: '2024 年度读书总结', articleDecr: '今年读了 47 本书，精选 10 本最值得推荐的书籍，涵盖技术、文学、历史、哲学', groupName: '阅读', articleType: 2 },
  { id: 'mock-204', articleName: '自建 NAS 全记录', articleDecr: '从硬件选型到系统安装，再到 Docker 应用部署，记录我的 TrueNAS Scale 自建之旅', groupName: '数码折腾', articleType: 2 },
  { id: 'mock-205', articleName: '阳台种菜三个月总结', articleDecr: '小番茄、辣椒、生菜、薄荷——在 4 平米的阳台上实现蔬菜自由的可能性探索', groupName: '生活分享', articleType: 2 },
  { id: 'mock-206', articleName: '摄影入门：从手机到相机', articleDecr: '分享从手机摄影转向微单的心得，以及新手最值得投入的镜头和配件推荐', groupName: '兴趣爱好', articleType: 2 },
]

export const mockPageResult = {
  current: 1,
  size: 8,
  total: 15,
  pages: 2,
}

export const mockMarkdown = `
# 老鱼人博客移动端上线

::: tip 提示
这是用于测试移动端 Markdown 渲染的示例文章，包含各种 Markdown 元素的渲染效果。
:::

## 概述

本次移动端更新为博客带来了全新的移动端适配体验。以下是本次更新的**核心特性**：

1. **响应式布局** — 全面适配手机与平板设备
2. **触摸优化** — 支持滑动切换、手势操作
3. **性能提升** — 首屏加载速度提升约 40%

> 移动优先设计不仅仅是将桌面端缩小，而是重新思考用户在手机上的阅读习惯和交互方式。

## 技术架构

### 前端框架

本次移动端采用 \`Vue 3\` + \`TypeScript\` 构建，核心依赖如下：

\`\`\`json
{
  "vue": "^3.5.25",
  "vue-router": "^4.6.3",
  "pinia": "^3.0.4",
  "markdown-it": "^14.1.0",
  "mermaid": "^11.14.0",
  "highlight.js": "^11.11.1"
}
\`\`\`

### 组件结构

组件树结构如下：

\`\`\`
src/views/mobile/
├── index.vue              # 移动端首页
├── components/
│   ├── Topbar.vue         # 顶部导航栏
│   ├── ArticleSection.vue # 文章列表区
│   ├── ArticleItem.vue    # 文章卡片
│   └── FooterBar.vue      # 底部信息栏
\`\`\`

### 核心实现

以下是 \`MobileMarkdownViewer.vue\` 中的滚动进度计算逻辑：

\`\`\`typescript
const handleScroll = () => {
  const scrollTop = window.scrollY
  const docHeight =
    document.documentElement.scrollHeight - window.innerHeight
  if (docHeight <= 0) {
    scrollProgress.value = 0
    return
  }
  const progress = Math.min(
    Math.max(scrollTop / docHeight, 0), 1
  )
  scrollProgress.value = progress
}
\`\`\`

### API 接口

文章数据通过 **RESTful API** 获取，接口格式如下：

| 接口路径 | 方法 | 参数 | 说明 |
|---------|------|------|------|
| \`/article/public/{id}\` | GET | id: 文章ID | 获取文章元信息 |
| \`/article/public/page\` | GET | current, size, type | 分页获取文章列表 |
| \`/article/group/public/{id}\` | GET | id: 分组ID | 获取分组文章 |

## 样式系统

### 配色方案

本次设计采用**浅蓝白色调**，以下是核心颜色变量：

\`\`\`css
:root {
  --brand: #3b82f6;       /* 品牌蓝 */
  --bg-start: #e8eef5;    /* 背景渐变色1 */
  --bg-end: #dce4ee;      /* 背景渐变色2 */
  --text-primary: #1e293b; /* 主文字色 */
  --text-secondary: #64748b;/* 次要文字 */
}
\`\`\`

### 排版

移动端字体使用系统字体栈，确保最佳渲染效果：

\`\`\`css
font-family:
  -apple-system, BlinkMacSystemFont,
  'PingFang SC', 'Hiragino Sans GB',
  'Microsoft YaHei', sans-serif;
\`\`\`

## 文章内容演示

### 文本格式化

这是一段包含 **粗体**、*斜体*、~~删除线~~ 以及 \`行内代码\` 的混合文本。你也可以看到 ~~被删除的内容~~ 和 **重要的强调内容**。

### 引用

> 单行引用示例

> 多行引用示例：
> 这是第二行
> 这是第三行
>
> 中间可以有空行

### 无序列表

- 前端技术栈
  - Vue 3
  - TypeScript
  - Vite
- 后端技术栈
  - Spring Boot
  - MyBatis Plus
  - MySQL
- DevOps
  - Docker
  - Nginx

### 有序列表

1. 打开项目
2. 安装依赖
   \`\`\`bash
   npm install
   \`\`\`
3. 启动开发服务器
   \`\`\`bash
   npm run dev
   \`\`\`
4. 在浏览器中打开 http://localhost:5173

### 代码块

**JavaScript 示例：**

\`\`\`javascript
function fibonacci(n) {
  if (n <= 1) return n
  const dp = [0, 1]
  for (let i = 2; i <= n; i++) {
    dp[i] = dp[i - 1] + dp[i - 2]
  }
  return dp[n]
}

console.log(fibonacci(10)) // 55
\`\`\`

**Python 示例：**

\`\`\`python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def inorder_traversal(root: TreeNode) -> list[int]:
    result = []
    def dfs(node):
        if not node:
            return
        dfs(node.left)
        result.append(node.val)
        dfs(node.right)
    dfs(root)
    return result
\`\`\`

**HTML 示例：**

\`\`\`html
<div class="card">
  <img src="avatar.jpg" alt="头像" />
  <h3>用户名</h3>
  <p>这是一段简介文字</p>
  <button @click="handleClick">
    点击关注
  </button>
</div>
\`\`\`

### 图片

![示例图片](https://picsum.photos/seed/oblog/800/400)

### 表格

| 特性 | 桌面版 | 移动版 |
|------|--------|--------|
| 左侧栏 | ✅ 280px 侧边栏 | ❌ 隐藏 |
| 右侧栏 | ✅ 240px 目录 | ❌ 隐藏 |
| 滚动按钮 | ❌ | ✅ 进度圆环 |
| 触摸滑动 | ❌ | ✅ 支持 |
| 顶部导航 | 完整导航 | 简化导航 |

### Mermaid 流程图

\`\`\`mermaid
graph TD
    A[用户打开页面] --> B{API 请求}
    B -->|成功| C[渲染文章内容]
    B -->|失败| D[显示错误提示]
    C --> E[触发 Mermaid]
    E --> F[监听滚动事件]
    F --> G[更新进度环]
\`\`\`

### Mermaid 时序图

\`\`\`mermaid
sequenceDiagram
    participant 用户
    participant 移动端
    participant 后端
    用户->>移动端: 点击文章
    移动端->>后端: GET /article/public/{id}
    后端-->>移动端: 返回文章元信息
    移动端->>后端: GET 文章内容 URL
    后端-->>移动端: 返回 Markdown 文本
    移动端->>移动端: 渲染为 HTML
\`\`\`

## 容器块演示

::: tip 提示
这是一个 **提示** 容器块，用于展示需要注意的信息。
:::

::: warning 警告
这是一个 **警告** 容器块，用于展示需要谨慎处理的内容。
:::

::: danger 危险
这是一个 **危险** 容器块，用于展示需要特别留意的内容。
:::

## 分割线

---

## 结语

以上就是本次移动端更新的全部核心功能演示。如果你在使用过程中遇到任何问题，欢迎通过 **反馈渠道** 告诉我们。

> 保持学习，保持分享。
> —— oldmerman

最后，附上一段彩蛋代码：

\`\`\`python
import this
\`\`\`

Happy Coding! 🎉
`
