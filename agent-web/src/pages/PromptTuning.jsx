/**
 * Prompt 调优页面
 */
import React, { useState } from 'react'
import { promptApi } from '../utils/api'

const STRATEGIES = ['evolution', 'beam_search', 'random', 'hybrid']

export default function PromptTuning() {
  const [promptId, setPromptId] = useState('')
  const [content, setContent] = useState('')
  const [description, setDescription] = useState('')
  const [strategy, setStrategy] = useState('evolution')
  const [testCases, setTestCases] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [variants, setVariants] = useState([])
  const [activeTab, setActiveTab] = useState('optimize')

  const handleCreate = async () => {
    if (!content.trim()) return
    setLoading(true)
    try {
      const res = await promptApi.createVersion({ promptId, content, description })
      setResult(res.data)
      setPromptId(res.data.promptId || promptId)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleOptimize = async () => {
    if (!promptId.trim()) return
    setLoading(true)
    try {
      const res = await promptApi.optimize({
        promptId,
        content,
        strategy,
        testCases: testCases.split('\n').filter(Boolean)
      })
      setResult(res.data)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleGenerateVariants = async () => {
    if (!content.trim()) return
    setLoading(true)
    try {
      const res = await promptApi.generateVariants({ content, strategy, count: 3 })
      setVariants(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handlePublish = async (versionId) => {
    try {
      await promptApi.publish(versionId)
      alert('发布成功')
    } catch (e) { console.error(e) }
  }

  return (
    <div className="prompt-page">
      <div className="page-header">
        <div>
          <div className="page-title">Prompt 调优中心</div>
          <div className="page-subtitle">定时/事件双触发驱动的自动 Prompt 调优</div>
        </div>
      </div>

      <div className="tabs">
        {[
          { key: 'optimize', label: '🔧 Prompt 优化' },
          { key: 'variants', label: '🎲 生成变体' },
          { key: 'history', label: '📜 版本历史' },
        ].map(t => (
          <div key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
            {t.label}
          </div>
        ))}
      </div>

      {activeTab === 'optimize' && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>📝 Prompt 编辑</div>
            <div className="input-group">
              <label className="input-label">Prompt ID</label>
              <input className="input" value={promptId} onChange={e => setPromptId(e.target.value)} placeholder="system_prompt_v1" />
            </div>
            <div className="input-group">
              <label className="input-label">Prompt 内容</label>
              <textarea className="input" rows={8} value={content} onChange={e => setContent(e.target.value)} placeholder="你是一个智能助手..." />
            </div>
            <div className="input-group">
              <label className="input-label">描述</label>
              <input className="input" value={description} onChange={e => setDescription(e.target.value)} placeholder="优化系统提示词" />
            </div>
            <div className="input-group">
              <label className="input-label">优化策略</label>
              <select className="input" value={strategy} onChange={e => setStrategy(e.target.value)}>
                <option value="evolution">Evolution（进化策略）</option>
                <option value="beam_search">Beam Search（束搜索）</option>
                <option value="random">Random（随机变异）</option>
                <option value="hybrid">Hybrid（混合策略）</option>
              </select>
            </div>
            <div className="input-group">
              <label className="input-label">测试用例（每行一个，用 | 分隔问题和期望答案）</label>
              <textarea className="input" rows={4} value={testCases} onChange={e => setTestCases(e.target.value)} placeholder="今天天气怎么样？| 天气晴朗&#10;1+1等于多少？| 2" />
            </div>
            <div className="flex gap-2">
              <button className="btn btn-primary" onClick={handleOptimize} disabled={loading || !promptId.trim()}>
                {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '🚀'} 优化 Prompt
              </button>
              <button className="btn btn-secondary" onClick={handleCreate} disabled={loading || !content.trim()}>
                保存版本
              </button>
            </div>
          </div>

          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>📊 优化结果</div>
            {result ? (
              <div>
                {result.optimizedContent && (
                  <div style={{ marginBottom: 16 }}>
                    <div className="text-sm text-muted" style={{ marginBottom: 8 }}>优化后的 Prompt:</div>
                    <pre className="text-sm" style={{ background: 'var(--bg-tertiary)', padding: 12, borderRadius: 6, whiteSpace: 'pre-wrap', maxHeight: 200, overflow: 'auto' }}>
                      {result.optimizedContent}
                    </pre>
                  </div>
                )}
                {result.improvement && (
                  <div className="text-sm">
                    <span className="text-muted">改进说明: </span>
                    <span>{result.improvement}</span>
                  </div>
                )}
                {result.score != null && (
                  <div style={{ marginTop: 12 }}>
                    <div className="text-sm text-muted">得分: <span className="text-green">{result.score.toFixed(2)}</span></div>
                    <div className="progress-bar" style={{ marginTop: 6 }}>
                      <div className="progress-fill success" style={{ width: `${result.score * 100}%` }} />
                    </div>
                  </div>
                )}
                {result.diffs && result.diffs.length > 0 && (
                  <div style={{ marginTop: 16 }}>
                    <div className="text-sm text-muted" style={{ marginBottom: 8 }}>变更点:</div>
                    {result.diffs.map((d, i) => (
                      <div key={i} className="text-sm" style={{ padding: '4px 8px', background: 'var(--bg-tertiary)', borderRadius: 4, marginBottom: 4 }}>
                        {d}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="empty-state">
                <div className="empty-state-icon">📊</div>
                <div className="empty-state-text">输入 Prompt 后点击优化，结果将显示在这里</div>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'variants' && (
        <div className="card">
          <div className="card-title" style={{ marginBottom: 16 }}>🎲 Prompt 变体生成</div>
          <div className="input-group">
            <label className="input-label">原始 Prompt</label>
            <textarea className="input" rows={6} value={content} onChange={e => setContent(e.target.value)} placeholder="输入原始 Prompt..." />
          </div>
          <div className="input-group">
            <label className="input-label">生成策略</label>
            <select className="input" value={strategy} onChange={e => setStrategy(e.target.value)}>
              {STRATEGIES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <button className="btn btn-primary" onClick={handleGenerateVariants} disabled={loading || !content.trim()}>
            {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '🎲'} 生成 3 个变体
          </button>

          {variants.length > 0 && (
            <div style={{ marginTop: 16 }}>
              {variants.map((v, i) => (
                <div key={i} className="card" style={{ marginBottom: 8 }}>
                  <div className="flex justify-between items-center">
                    <span className="badge badge-purple">变体 {i + 1}</span>
                    {v.versionId && <button className="btn btn-sm btn-success" onClick={() => handlePublish(v.versionId)}>发布</button>}
                  </div>
                  <pre className="text-sm" style={{ marginTop: 8, whiteSpace: 'pre-wrap', maxHeight: 150, overflow: 'auto' }}>
                    {v.content || v.prompt || JSON.stringify(v, null, 2)}
                  </pre>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'history' && (
        <div className="card">
          <div className="card-title" style={{ marginBottom: 16 }}>📜 版本历史</div>
          <div className="input-group">
            <label className="input-label">Prompt ID</label>
            <div className="flex gap-2">
              <input className="input" value={promptId} onChange={e => setPromptId(e.target.value)} placeholder="输入 Prompt ID" style={{ flex: 1 }} />
              <button className="btn btn-secondary" onClick={async () => {
                if (!promptId) return
                try {
                  const res = await promptApi.getHistory(promptId)
                  setResult({ history: res.data })
                } catch (e) { console.error(e) }
              }}>查询</button>
            </div>
          </div>
          {result?.history && (
            <div className="table-wrapper">
              <table className="table">
                <thead>
                  <tr><th>Version ID</th><th>描述</th><th>创建时间</th><th>操作</th></tr>
                </thead>
                <tbody>
                  {result.history.map((v, i) => (
                    <tr key={i}>
                      <td><span className="font-mono text-xs">{v.versionId || v.id}</span></td>
                      <td>{v.description || '-'}</td>
                      <td className="text-muted text-xs">{v.createdAt || v.timestamp || '-'}</td>
                      <td>
                        <div className="flex gap-2">
                          <button className="btn btn-sm btn-success" onClick={() => handlePublish(v.versionId || v.id)}>发布</button>
                          <button className="btn btn-sm btn-secondary" onClick={async () => {
                            try {
                              const r = await promptApi.rollback(v.versionId || v.id)
                              setResult(r.data)
                            } catch (e) { console.error(e) }
                          }}>回滚</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
