# SAI-Chat UI

RAG 智能问答平台的前端项目，为 [SAI-Chat](https://github.com/your-org/SAI-Chat) 后端提供完整的 UI 适配。

## 项目介绍

一个现代化的 React 前端项目，用于展示和操作 chat-agent 后端的全部核心功能：

- **RAG 智能问答**：多路检索引擎、意图识别、问题改写、会话记忆
- **ReAct Agent**：思考-执行-观察循环、9 状态机、MCP 工具调用
- **三层记忆管理**：滑动窗口、LLM 摘要、自学习闭环
- **A/B 测试体系**：流量分配、统计显著性检验
- **Prompt 自动调优**：版本管理、5 种优化策略、变体测试
- **可观测性面板**：实时指标仪表盘、调用链路追踪、评测中心

## 技术栈

| 技术 | 用途 |
|------|------|
| React 18 + Vite 6 | UI 框架 & 构建工具 |
| TypeScript 5 | 类型安全 |
| Ant Design 5 | 企业级 UI 组件 |
| Tailwind CSS 3 | 快速布局 |
| Zustand | 轻量状态管理 |
| TanStack Query | 服务端状态 |
| Axios | HTTP 客户端 |
| React Markdown | Markdown 渲染 |

## 快速开始

### 前提条件

- Node.js >= 18
- chat-agent 后端运行于 `http://localhost:8080`（可选，UI 有 Mock 数据演示模式）

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
# 访问 http://localhost:3000
```

### 构建生产版本

```bash
npm run build
```

## 功能模块

### 智能问答 (`/`)

- **RAG 模式**：意图识别 → 问题改写 → 多路检索 → LLM 生成
- **Agent 模式**：ReAct 三阶段循环、工具调用可视化
- SSE 流式输出，实时展示各阶段状态
- 9 种 Agent 状态机颜色标识
- 深思考开关、迭代次数控制

### 知识库管理 (`/knowledge`)

- 拖拽上传文档（PDF/Word/PPT/Excel/TXT/HTML）
- 上传进度可视化（解析 → 分块 → 向量化 → 入库）
- 标签管理与分块预览
- Git 仓库导入

### 意图树编辑器 (`/intent-tree`)

- 三层树形结构：Domain → Category → Topic
- 节点类型标识：KB（知识库）/ MCP（工具调用）/ SYSTEM（系统交互）
- 节点详情查看与编辑

### 管理后台

| 页面 | 路径 | 功能 |
|------|------|------|
| A/B 测试 | `/admin/abtest` | 创建/启停测试、流量分配、统计分析 |
| Prompt 调优 | `/admin/prompt` | 版本管理、自动优化、变体测试 |
| 可观测性 | `/admin/observe` | 指标仪表盘、调用链路、评测中心 |
| 复盘分析 | `/admin/review` | 成功模式、失败教训库 |
| 实验管理 | `/admin/experiment` | 优化实验、灰度发布 |

## Mock 登录

无需后端即可体验全部功能：

- 输入任意用户名即可登录
- 预设 3 个快速入口：管理员 / 普通用户 / 访客
- 所有管理功能使用预设的 Mock 数据展示

## 页面截图说明

### 智能问答页面
- 左侧：消息列表，支持 Markdown 渲染
- 中间：RAG/Agent 流水线阶段展示
- 右侧：意图分类、检索结果、工具调用、记忆状态

### A/B 测试页面
- 测试列表（状态筛选）
- 流量权重可视化（环形图/柱状图）
- 各变体转化率对比
- 统计显著性指标

### Prompt 调优页面
- 版本历史列表
- 优化迭代过程（变体对比）
- 评测分数趋势

## API 适配

后端 API 路径（已在 Vite proxy 中配置）：

| 后端路径 | 说明 |
|---------|------|
| `/agent/chat` | 同步 Agent 对话 |
| `/agent/chat/stream` | SSE Agent 流式对话 |
| `/api/rag/chat/sse` | SSE RAG 流式对话 |
| `/api/rag/intent/classify` | 意图分类 |
| `/api/rag/query/rewrite` | 问题改写 |
| `/api/abtest/**` | A/B 测试管理 |
| `/api/prompt/**` | Prompt 管理 |
| `/api/observe/**` | 可观测性 |
| `/api/eval/**` | 评测中心 |
| `/api/review/**` | 复盘分析 |
| `/api/optimize/**` | 实验管理 |
| `/api/v1/rag/file/upload` | 文档上传 |
| `/api/security/**` | 权限与安全 |

## 目录结构

```
sai-chat-ui/
├── src/
│   ├── api/          # API 调用封装
│   ├── components/    # 公共组件
│   ├── hooks/        # React hooks (useSSE, useStream, useMetrics)
│   ├── mock/         # Mock 数据
│   ├── pages/        # 页面组件
│   │   ├── Admin/    # 管理后台页面
│   │   ├── Chat/    # 聊天页面及子组件
│   │   ├── IntentTree/
│   │   ├── KnowledgeBase/
│   │   └── ...
│   ├── stores/       # Zustand 状态管理
│   ├── types/        # TypeScript 类型定义
│   └── utils/        # 工具函数
├── package.json
├── vite.config.ts
└── tailwind.config.js
```

## 演示建议

1. **启动后端**：确保 chat-agent 运行于 8080 端口
2. **打开前端**：`npm run dev` 访问 3000 端口
3. **Mock 登录**：输入用户名直接进入
4. **功能演示**：
   - 发送问题 → 观察 SSE 流式响应各阶段
   - 切换 Agent 模式 → 观察工具调用过程
   - 访问管理后台 → 展示 A/B 测试、Prompt 调优等完整功能

## License

MIT
