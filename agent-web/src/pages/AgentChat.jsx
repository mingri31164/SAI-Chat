/**
 * Agent 对话页面
 *
 * 展示：
 * - 完整对话历史（用户消息 + Agent 回复）
 * - Agent 执行过程可视化（思考、工具调用、观察）
 * - SSE 流式输出
 * - 执行指标（耗时、迭代次数、Token 消耗、成本）
 */
import React, { useState, useRef, useEffect, useCallback } from 'react'
import { agentApi } from '../utils/api'

// 生成简单 sessionId
function genSessionId() {
  return 'session_' + Math.random().toString(36).substring(2, 10)
}

const STATUS_MAP = {
  IDLE: { label: '空闲', color: 'neutral' },
  THINKING: { label: '思考中', color: 'info' },
  EXECUTING: { label: '执行中', color: 'warning' },
  OBSERVING: { label: '观察中', color: 'info' },
  REFLECTING: { label: '反思中', color: 'purple' },
  COMPLETED: { label: '已完成', color: 'success' },
  FAILED: { label: '失败', color: 'error' },
  EXCEEDED: { label: '超限', color: 'error' },
  WAITING_FOR_USER: { label: '等待输入', color: 'warning' },
}

function ToolCallBadge({ tool }) {
  return (
    <div className="tool-badge">
      <span className="tool-badge-icon">⚙</span>
      <span className="tool-badge-name">{tool.toolName || tool.toolId}</span>
      {tool.success === false && <span className="tool-badge-error">失败</span>}
      {tool.durationMs && (
        <span className="tool-badge-time">{(tool.durationMs / 1000).toFixed(1)}s</span>
      )}
    </div>
  )
}

function ThoughtBlock({ thought, index, isStreaming }) {
  return (
    <div className="thought-block animate-fade-in">
      <div className="thought-header">
        <span className="thought-label">💭 Thought #{index + 1}</span>
        {isStreaming && <span className="streaming-indicator"><span className="spinner" style={{ width: 12, height: 12 }}></span></span>}
      </div>
      <pre className="thought-content">{thought}</pre>
    </div>
  )
}

function AgentPipeline({ pipelineData }) {
  if (!pipelineData || !pipelineData.steps) return null

  const steps = pipelineData.steps
  return (
    <div className="pipeline-visual">
      <div className="pipeline-steps">
        {steps.map((step, i) => (
          <React.Fragment key={i}>
            <div className={`pipeline-step ${step.status || 'pending'}`}>
              <div className="pipeline-step-icon">
                {step.status === 'done' ? '✓' : step.status === 'active' ? '▶' : (i + 1)}
              </div>
              <div className="pipeline-step-label">{step.name}</div>
            </div>
            {i < steps.length - 1 && <div className="pipeline-arrow">→</div>}
          </React.Fragment>
        ))}
      </div>
    </div>
  )
}

export default function AgentChat() {
  const [sessionId] = useState(genSessionId)
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [currentThought, setCurrentThought] = useState(null)
  const [currentToolCalls, setCurrentToolCalls] = useState([])
  const [streamContent, setStreamContent] = useState('')
  const [doneInfo, setDoneInfo] = useState(null)
  const [error, setError] = useState(null)
  const [options, setOptions] = useState({
    deepThinking: false,
    maxIterations: 10,
    maxTokens: 8000,
    maxBudget: 1.0,
  })
  const [showOptions, setShowOptions] = useState(false)
  const messagesEndRef = useRef(null)
  const sseRef = useRef(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages, streamContent])

  const handleStop = () => {
    sseRef.current?.close()
    setLoading(false)
    setStreamContent('')
    setCurrentThought(null)
    setCurrentToolCalls([])
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!input.trim() || loading) return

    const userMsg = { role: 'user', content: input.trim() }
    setMessages(prev => [...prev, userMsg])
    setInput('')
    setLoading(true)
    setError(null)
    setDoneInfo(null)
    setCurrentThought(null)
    setCurrentToolCalls([])
    setStreamContent('')

    const allMessages = [...messages, userMsg]

    // 使用 SSE 流式
    const { close } = agentApi.chatStream(
      {
        question: userMsg.content,
        sessionId,
        deepThinking: options.deepThinking,
        maxIterations: options.maxIterations,
        maxTokens: options.maxTokens,
        maxBudget: options.maxBudget,
      },
      {
        onIntent: (data) => {
          // 状态变更
        },
        onThinking: (data) => {
          setCurrentThought(data)
        },
        onToolStart: (data) => {
          setCurrentToolCalls(prev => [
            ...prev,
            { ...data, status: 'running' }
          ])
        },
        onToolEnd: (data) => {
          setCurrentToolCalls(prev =>
            prev.map(t =>
              t.step === data.step && t.toolId === data.toolId
                ? { ...data, status: 'done' }
                : t
            )
          )
        },
        onAnswer: (data) => {
          setStreamContent(prev => prev + (data.content || ''))
        },
        onError: (data) => {
          setError(data.error || data.message || '发生错误')
        },
        onDone: (data) => {
          setDoneInfo(data)
          setLoading(false)

          // 添加 Agent 回复
          const agentMsg = {
            role: 'agent',
            content: data.answer || streamContent,
            metadata: {
              totalSteps: data.totalSteps,
              totalTokens: data.totalTokens,
              totalCost: data.totalCost,
              durationMs: data.durationMs,
            }
          }
          setMessages(prev => [...prev, agentMsg])
          setStreamContent('')
          setCurrentThought(null)
          setCurrentToolCalls([])
        }
      }
    )

    sseRef.current = { close }

    // SSE 超时保护
    setTimeout(() => {
      if (loading) {
        close()
        setLoading(false)
        if (streamContent) {
          setMessages(prev => [...prev, {
            role: 'agent',
            content: streamContent,
            metadata: { error: 'SSE 超时' }
          }])
        }
      }
    }, 120000)
  }

  return (
    <div className="agent-chat-page">
      <div className="page-header">
        <div>
          <div className="page-title">Agent 对话</div>
          <div className="page-subtitle">基于 ReAct 模式的核心执行循环，支持多轮对话与工具调用</div>
        </div>
        <div className="flex gap-2">
          <span className="text-sm text-muted">Session: {sessionId.substring(0, 16)}</span>
          <button className="btn btn-sm btn-secondary" onClick={() => setShowOptions(!showOptions)}>
            ⚙ {showOptions ? '隐藏' : '选项'}
          </button>
        </div>
      </div>

      {/* 选项面板 */}
      {showOptions && (
        <div className="card animate-fade-in" style={{ marginBottom: 16 }}>
          <div className="grid-4">
            <div className="input-group">
              <label className="input-label">最大迭代次数</label>
              <input
                className="input"
                type="number"
                min="1" max="50"
                value={options.maxIterations}
                onChange={e => setOptions(p => ({ ...p, maxIterations: parseInt(e.target.value) }))}
              />
            </div>
            <div className="input-group">
              <label className="input-label">最大 Token</label>
              <input
                className="input"
                type="number"
                min="1000" max="128000"
                step="1000"
                value={options.maxTokens}
                onChange={e => setOptions(p => ({ ...p, maxTokens: parseInt(e.target.value) }))}
              />
            </div>
            <div className="input-group">
              <label className="input-label">最大成本 (元)</label>
              <input
                className="input"
                type="number"
                min="0.01" max="100"
                step="0.1"
                value={options.maxBudget}
                onChange={e => setOptions(p => ({ ...p, maxBudget: parseFloat(e.target.value) }))}
              />
            </div>
            <div className="input-group">
              <label className="input-label">&nbsp;</label>
              <label className="flex items-center gap-2" style={{ cursor: 'pointer', paddingTop: 8 }}>
                <input
                  type="checkbox"
                  checked={options.deepThinking}
                  onChange={e => setOptions(p => ({ ...p, deepThinking: e.target.checked }))}
                />
                <span className="text-sm">深度思考模式</span>
              </label>
            </div>
          </div>
        </div>
      )}

      {/* 消息区域 */}
      <div className="chat-messages">
        {messages.length === 0 && !loading && (
          <div className="empty-state">
            <div className="empty-state-icon">⬡</div>
            <div className="empty-state-text">
              开始对话，体验 Agent 的思考-执行-观察循环<br />
              <span className="text-xs text-muted" style={{ marginTop: 8, display: 'block' }}>
                示例问题：帮我查一下北京今天的天气，然后告诉我应该穿什么？
              </span>
            </div>
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`message message-${msg.role} animate-fade-in`}>
            <div className="message-avatar">
              {msg.role === 'user' ? '👤' : '🤖'}
            </div>
            <div className="message-body">
              <div className="message-content">{msg.content}</div>
              {msg.metadata && (
                <div className="message-meta">
                  {msg.metadata.totalSteps && <span>⚙ {msg.metadata.totalSteps} 步</span>}
                  {msg.metadata.totalTokens && <span>📊 {msg.metadata.totalTokens} tokens</span>}
                  {msg.metadata.totalCost != null && <span>💰 ¥{msg.metadata.totalCost.toFixed(4)}</span>}
                  {msg.metadata.durationMs && <span>⏱ {(msg.metadata.durationMs / 1000).toFixed(1)}s</span>}
                </div>
              )}
            </div>
          </div>
        ))}

        {/* 实时思考过程 */}
        {loading && currentThought && (
          <div className="message message-agent">
            <div className="message-avatar">🤖</div>
            <div className="message-body">
              <div className="message-header">
                <span className="badge badge-blue animate-pulse">思考中</span>
              </div>
              <ThoughtBlock thought={currentThought.thought || currentThought.message || ''} index={0} isStreaming />
            </div>
          </div>
        )}

        {/* 实时工具调用 */}
        {loading && currentToolCalls.length > 0 && (
          <div className="card animate-fade-in" style={{ marginLeft: 48 }}>
            <div className="card-title" style={{ fontSize: 12, color: 'var(--accent-orange)' }}>
              ⚡ 工具调用
            </div>
            {currentToolCalls.map((tc, i) => (
              <ToolCallBadge key={i} tool={tc} />
            ))}
          </div>
        )}

        {/* 实时流式回答 */}
        {loading && streamContent && (
          <div className="message message-agent">
            <div className="message-avatar">🤖</div>
            <div className="message-body">
              <div className="message-header">
                <span className="badge badge-green">生成中</span>
                <span className="spinner" style={{ width: 14, height: 14 }}></span>
              </div>
              <div className="message-content stream-content">{streamContent}</div>
            </div>
          </div>
        )}

        {/* 错误 */}
        {error && (
          <div className="card animate-fade-in" style={{ borderColor: 'var(--accent-red)', background: 'rgba(248,81,73,0.05)' }}>
            <span className="text-red">❌ 错误: {error}</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* 输入区域 */}
      <div className="chat-input-area">
        <form className="chat-input-form" onSubmit={handleSubmit}>
          <textarea
            className="input"
            placeholder="输入问题，Agent 将基于 ReAct 模式执行..."
            value={input}
            onChange={e => setInput(e.target.value)}
            onKeyDown={e => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                handleSubmit(e)
              }
            }}
            rows={2}
            disabled={loading}
          />
          <div className="chat-input-actions">
            <span className="text-xs text-muted">Enter 发送，Shift+Enter 换行</span>
            <div className="flex gap-2">
              {loading && (
                <button type="button" className="btn btn-danger btn-sm" onClick={handleStop}>
                  ⏹ 停止
                </button>
              )}
              <button type="submit" className="btn btn-primary btn-sm" disabled={loading || !input.trim()}>
                {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '▶'}
                发送
              </button>
            </div>
          </div>
        </form>
      </div>
    </div>
  )
}
