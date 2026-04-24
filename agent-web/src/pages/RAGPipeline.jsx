/**
 * RAG 流水线页面
 *
 * 展示语义增强流水线的每个阶段：
 * 1. 查询改写（基于 LLM 的问题重写与拆分）
 * 2. 意图分类（IntentNode 树分类 + 置信度）
 * 3. 多路召回（全局向量检索 + 意图定向检索）
 * 4. 去重与重排序
 * 5. 滑动窗口记忆
 */
import React, { useState } from 'react'
import { ragApi } from '../utils/api'

// 流水线阶段定义
const PIPELINE_STAGES = [
  { id: 'input', label: '输入解析', icon: '📥' },
  { id: 'rewrite', label: '查询改写', icon: '🔄' },
  { id: 'intent', label: '意图分类', icon: '🎯' },
  { id: 'retrieval', label: '多路召回', icon: '🔍' },
  { id: 'rerank', label: '去重排序', icon: '📊' },
  { id: 'memory', label: '记忆管理', icon: '🧠' },
  { id: 'answer', label: '答案生成', icon: '💬' },
]

function StageCard({ stage, data, active }) {
  const statusColors = {
    pending: 'var(--text-muted)',
    running: 'var(--accent-blue)',
    done: 'var(--accent-green)',
    error: 'var(--accent-red)',
  }

  return (
    <div className={`stage-card ${active ? 'active' : ''}`}>
      <div className="stage-header">
        <span className="stage-icon">{stage.icon}</span>
        <span className="stage-label">{stage.label}</span>
        {data && (
          <span
            className="status-dot"
            style={{ background: statusColors[data.status || 'done'] }}
          />
        )}
      </div>
      <div className="stage-content">
        {active && !data && (
          <div className="flex items-center gap-2">
            <div className="spinner" style={{ width: 14, height: 14 }}></div>
            <span className="text-sm text-muted">处理中...</span>
          </div>
        )}
        {data && stage.id === 'rewrite' && (
          <div className="stage-data">
            {data.rewrittenQuestion && (
              <div className="text-sm">
                <span className="text-muted">改写后: </span>
                <span className="text-blue">{data.rewrittenQuestion}</span>
              </div>
            )}
            {data.subQuestions && data.subQuestions.length > 0 && (
              <div style={{ marginTop: 6 }}>
                <span className="text-xs text-muted">子问题拆分:</span>
                {data.subQuestions.map((q, i) => (
                  <div key={i} className="sub-question">{q}</div>
                ))}
              </div>
            )}
          </div>
        )}
        {data && stage.id === 'intent' && (
          <div className="stage-data">
            {data.scores && Array.isArray(data.scores) ? (
              data.scores.map((item, i) => (
                <div key={i} className="intent-item">
                  <span className="intent-name">{item.node || item.intent}</span>
                  <div className="intent-bar">
                    <div
                      className="intent-fill"
                      style={{ width: `${(item.score || item.confidence || 0) * 100}%` }}
                    />
                  </div>
                  <span className="intent-score">
                    {((item.score || item.confidence || 0) * 100).toFixed(1)}%
                  </span>
                </div>
              ))
            ) : (
              <div className="text-sm text-muted">
                Top: <span className="text-green">{data.topIntent || data.topScore?.node}</span>
                {' '}({((data.topScore?.score || data.topScore?.confidence || 0) * 100).toFixed(1)}%)
              </div>
            )}
          </div>
        )}
        {data && stage.id === 'retrieval' && (
          <div className="stage-data">
            {data.count != null && (
              <div className="text-sm text-muted">召回: {data.count} 个文档块</div>
            )}
            {data.chunks && data.chunks.slice(0, 3).map((chunk, i) => (
              <div key={i} className="chunk-item">
                <span className="chunk-score">{(chunk.score * 100).toFixed(0)}%</span>
                <span className="chunk-content">{chunk.content?.substring(0, 80)}...</span>
              </div>
            ))}
          </div>
        )}
        {data && stage.id === 'memory' && (
          <div className="stage-data">
            {data.turnsKept != null && (
              <div className="text-sm text-muted">保留最近 {data.turnsKept} 轮对话</div>
            )}
            {data.summary && (
              <div className="text-sm text-muted" style={{ marginTop: 4 }}>
                摘要: {data.summary}
              </div>
            )}
            {data.historyTokens && (
              <div className="text-xs text-muted" style={{ marginTop: 4 }}>
                Token: {data.historyTokens}
              </div>
            )}
          </div>
        )}
        {data && stage.id === 'answer' && (
          <div className="stage-data">
            <div className="text-sm">{data.fullContent || data.answer}</div>
          </div>
        )}
      </div>
    </div>
  )
}

export default function RAGPipeline() {
  const [question, setQuestion] = useState('')
  const [sessionId] = useState('rag_' + Math.random().toString(36).substring(2, 8))
  const [loading, setLoading] = useState(false)
  const [pipelineData, setPipelineData] = useState({
    input: { status: 'done', data: null },
    rewrite: null,
    intent: null,
    retrieval: null,
    rerank: null,
    memory: null,
    answer: null,
  })
  const [result, setResult] = useState('')
  const [error, setError] = useState(null)
  const [mode, setMode] = useState('sse') // sse | rewrite | intent

  const runPipeline = () => {
    if (!question.trim()) return
    setLoading(true)
    setError(null)
    setResult('')

    // 模拟流水线阶段
    setPipelineData({
      input: { status: 'done', data: { original: question } },
      rewrite: null, intent: null, retrieval: null,
      rerank: null, memory: null, answer: null,
    })

    if (mode === 'rewrite') {
      ragApi.rewriteQuery(question, sessionId)
        .then(res => {
          setPipelineData(p => ({ ...p, rewrite: { status: 'done', data: res.data } }))
          setLoading(false)
        })
        .catch(err => {
          setError(err.message)
          setLoading(false)
        })
      return
    }

    if (mode === 'intent') {
      ragApi.classifyIntent(question)
        .then(res => {
          setPipelineData(p => ({ ...p, intent: { status: 'done', data: res.data } }))
          setLoading(false)
        })
        .catch(err => {
          setError(err.message)
          setLoading(false)
        })
      return
    }

    // SSE 模式：流式展示
    const { close } = ragApi.chatStream(
      { question, sessionId, deepThinking: false },
      {
        onIntent: (data) => {
          setPipelineData(p => ({ ...p, intent: { status: 'done', data } }))
        },
        onThinking: () => {},
        onRewrite: (data) => {
          setPipelineData(p => ({ ...p, rewrite: { status: 'done', data } }))
        },
        onRetrieval: (data) => {
          setPipelineData(p => ({ ...p, retrieval: { status: 'done', data } }))
        },
        onAnswer: (data) => {
          setResult(prev => prev + (data.content || ''))
          setPipelineData(p => ({ ...p, answer: { status: 'done', data: { content: prev + (data.content || '') } } }))
        },
        onError: (data) => {
          setError(data.message)
          setLoading(false)
        },
        onDone: (data) => {
          setResult(data.fullContent || data.answer || result)
          setLoading(false)
        }
      }
    )

    setTimeout(() => {
      if (loading) {
        close()
        setLoading(false)
      }
    }, 60000)
  }

  return (
    <div className="rag-pipeline-page">
      <div className="page-header">
        <div>
          <div className="page-title">RAG 语义增强流水线</div>
          <div className="page-subtitle">
            查询改写 → 意图分类 → 多路召回 → 去重排序 → 滑动窗口记忆 → 答案生成
          </div>
        </div>
      </div>

      {/* 流水线可视化 */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">🔄 执行流水线</div>
          <div className="flex gap-2">
            <button
              className={`btn btn-sm ${mode === 'sse' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setMode('sse')}
            >
              完整流水线
            </button>
            <button
              className={`btn btn-sm ${mode === 'rewrite' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setMode('rewrite')}
            >
              查询改写
            </button>
            <button
              className={`btn btn-sm ${mode === 'intent' ? 'btn-primary' : 'btn-secondary'}`}
              onClick={() => setMode('intent')}
            >
              意图分类
            </button>
          </div>
        </div>

        <div className="pipeline-flow">
          {PIPELINE_STAGES.map(stage => {
            const data = pipelineData[stage.id]
            const isActive = stage.id === 'input' ||
              (stage.id === 'rewrite' && mode === 'rewrite') ||
              (stage.id === 'intent' && mode === 'intent') ||
              (mode === 'sse' && stage.id !== 'input')
            return (
              <React.Fragment key={stage.id}>
                <StageCard stage={stage} data={data} active={isActive && !data} />
                {stage.id !== 'answer' && <div className="pipeline-connector">→</div>}
              </React.Fragment>
            )
          })}
        </div>
      </div>

      {/* 输入与结果 */}
      <div className="grid-2">
        <div className="card">
          <div className="card-header">
            <div className="card-title">📥 输入问题</div>
          </div>
          <div className="input-group">
            <textarea
              className="input"
              rows={3}
              placeholder="输入问题，例如：帮我分析一下量子计算的发展趋势"
              value={question}
              onChange={e => setQuestion(e.target.value)}
              disabled={loading}
            />
          </div>
          <div className="flex justify-between items-center">
            <span className="text-xs text-muted">Session: {sessionId}</span>
            <button
              className="btn btn-primary"
              onClick={runPipeline}
              disabled={loading || !question.trim()}
            >
              {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '▶'}
              执行流水线
            </button>
          </div>

          {error && (
            <div className="card animate-fade-in" style={{ marginTop: 12, borderColor: 'var(--accent-red)', background: 'rgba(248,81,73,0.05)' }}>
              <span className="text-red text-sm">❌ {error}</span>
            </div>
          )}
        </div>

        <div className="card">
          <div className="card-header">
            <div className="card-title">💬 生成结果</div>
          </div>
          {result ? (
            <div className="scroll-area">
              <pre className="text-sm" style={{ whiteSpace: 'pre-wrap', fontFamily: 'inherit', lineHeight: 1.7 }}>
                {result}
              </pre>
            </div>
          ) : (
            <div className="empty-state">
              <div className="empty-state-icon">💬</div>
              <div className="empty-state-text">执行流水线后，结果将显示在这里</div>
            </div>
          )}
        </div>
      </div>

      {/* 流水线说明 */}
      <div className="card">
        <div className="card-header">
          <div className="card-title">📖 流水线各阶段说明</div>
        </div>
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>阶段</th>
                <th>输入</th>
                <th>输出</th>
                <th>解决的问题</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span className="badge badge-blue">查询改写</span></td>
                <td>用户原始问题</td>
                <td>改写后问题 + 子问题列表</td>
                <td>口语化表达、模糊问题</td>
              </tr>
              <tr>
                <td><span className="badge badge-purple">意图分类</span></td>
                <td>改写后问题</td>
                <td>意图类型 + 置信度</td>
                <td>路由到不同知识库</td>
              </tr>
              <tr>
                <td><span className="badge badge-cyan">多路召回</span></td>
                <td>意图 + 问题</td>
                <td>向量检索块 + 意图定向块</td>
                <td>单一检索覆盖率低</td>
              </tr>
              <tr>
                <td><span className="badge badge-orange">去重排序</span></td>
                <td>多路召回结果</td>
                <td>去重 + 重排序块列表</td>
                <td>重复内容、相关性排序</td>
              </tr>
              <tr>
                <td><span className="badge badge-green">滑动窗口记忆</span></td>
                <td>历史对话</td>
                <td>摘要 + 关键信息</td>
                <td>长对话 Token 爆炸</td>
              </tr>
              <tr>
                <td><span className="badge badge-blue">答案生成</span></td>
                <td>检索块 + 记忆 + 问题</td>
                <td>最终回答</td>
                <td>综合生成</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
