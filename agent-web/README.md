# SAI Agent 控制台前端

SAI-Chat Agent 模块的 Web 验证界面，用于本地快速验证 Agent 核心功能。

## 功能模块

| 页面 | 路径 | 功能 |
|------|------|------|
| Agent 对话 | `/agent-chat` | ReAct 循环、流式 SSE、工具调用可视化 |
| RAG 流水线 | `/rag-pipeline` | 查询改写、意图分类、多路召回、去重排序、滑动窗口记忆 |
| 观测中心 | `/observe` | 指标快照、追踪列表、延迟分析、工具统计 |
| 评测系统 | `/eval` | 用例库浏览、一键评测、评测报告 |
| Prompt 调优 | `/prompt` | Prompt 版本管理、优化、变体生成 |
| A/B 测试 | `/abtest` | 测试创建、流量分配、显著性分析 |
| 安全中心 | `/security` | 权限验证、内容安全过滤、安全规则 |
| 复盘中心 | `/review` | 成功模式、失败教训、复盘分析 |

## 快速启动

### 1. 启动后端（agent 模块）

确保 Redis 和 MySQL 运行中，然后启动 agent 模块：

```bash
cd chat-agent
# 首次需要先创建 .env 文件
cp ../.env .env
# 编辑 .env 填写实际配置

mvn spring-boot:run
```

后端端口：`8080`

### 2. 启动前端

```bash
cd agent-web
npm install
npm run dev
```

前端端口：`5173`（已配置 Vite 代理转发 `/api`、`/agent`、`/health` 到后端）

### 3. 配置 .env（关键）

agent 模块的 `.env` 文件需要填写以下关键配置：

```bash
# AI 模型（至少配置一个）
BAILIAN_API_KEY=sk-xxxxxxxx          # 阿里云百炼
SILICONFLOW_API_KEY=sk-xxxxxxxx     # SiliconFlow

# 数据库
DB_MASTER_HOST=localhost
DB_MASTER_PORT=3306
DB_MASTER_USERNAME=root
DB_MASTER_PASSWORD=你的密码
DB_NAME=sai_chat

# Redis
REDIS_HOST=127.0.0.1
REDIS_PORT=6379

# Milvus（可选，RAG 检索需要）
MILVUS_URI=http://localhost:19530

# MCP 工具（可选，Agent 工具调用需要）
MCP_ENABLED=false
MCP_SERVER_URL=http://localhost:9099/mcp
```

## 前端技术栈

- **React 18** + **Vite 5**
- **React Router 6**（路由）
- **Axios**（API 调用）
- **原生 CSS**（无 UI 框架，深色主题）
- **原生 SSE**（Server-Sent Events 流式输出）

## 代理配置

`vite.config.js` 中已配置所有后端 API 的代理转发：

```javascript
proxy: {
  '/api':    { target: 'http://localhost:8080', changeOrigin: true },
  '/agent':  { target: 'http://localhost:8080', changeOrigin: true },
  '/health': { target: 'http://localhost:8080', changeOrigin: true },
}
```

## 数据初始化

如果数据库为空，需要执行建表 SQL。表结构在 agent 模块的 MyBatis 实体类中定义（JPA/MyBatis-Plus 注解），可以：

1. 启动后端后访问 Swagger 文档：`http://localhost:8080/doc.html`
2. 或手动执行建表语句

关键表：
- `agent_trace` — 追踪记录
- `agent_metrics` — 指标数据
- `eval_case` — 评测用例
- `eval_report` — 评测报告
- `prompt_version` — Prompt 版本
- `ab_test` — A/B 测试
- `review_pattern` — 成功模式
- `failure_lesson` — 失败教训

## 项目结构

```
agent-web/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.jsx
    ├── App.jsx
    ├── utils/
    │   └── api.js          # API 封装 + SSE 支持
    ├── styles/
    │   ├── global.css       # 全局样式
    │   └── chat.css         # 页面特定样式
    └── pages/
        ├── AgentChat.jsx    # Agent 对话
        ├── RAGPipeline.jsx  # RAG 流水线
        ├── Observe.jsx       # 观测中心
        ├── Eval.jsx          # 评测系统
        ├── PromptTuning.jsx  # Prompt 调优
        ├── ABTest.jsx        # A/B 测试
        ├── Security.jsx      # 安全中心
        └── Review.jsx        # 复盘中心
```
