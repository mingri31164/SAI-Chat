/**
 * 复盘中心页面
 */
import React, { useState, useEffect } from 'react'
import { reviewApi } from '../utils/api'

export default function Review() {
  const [activeTab, setActiveTab] = useState('patterns')
  const [patterns, setPatterns] = useState([])
  const [lessons, setLessons] = useState([])
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(false)
  const [traceId, setTraceId] = useState('')
  const [reviewResult, setReviewResult] = useState(null)

  const loadPatterns = async () => {
    setLoading(true)
    try {
      const res = await reviewApi.getAllPatterns()
      setPatterns(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const loadLessons = async () => {
    setLoading(true)
    try {
      const res = await reviewApi.getAllLessons()
      setLessons(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const loadStats = async () => {
    try {
      const res = await reviewApi.getStats()
      setStats(res.data)
    } catch (e) { console.error(e) }
  }

  useEffect(() => {
    loadPatterns()
    loadLessons()
    loadStats()
  }, [])

  const handleAnalyze = async () => {
    if (!traceId.trim()) return
    setLoading(true)
    try {
      const res = await reviewApi.analyze(traceId, null)
      setReviewResult(res.data)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleSearchPatterns = async (query) => {
    if (!query.trim()) { loadPatterns(); return }
    setLoading(true)
    try {
      const res = await reviewApi.searchPatterns(query)
      setPatterns(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  return (
    <div className="review-page">
      <div className="page-header">
        <div>
          <div className="page-title">复盘中心</div>
          <div className="page-subtitle">成功模式库 / 失败教训库 / 批量复盘分析</div>
        </div>
      </div>

      {/* 统计概览 */}
      {stats && (
        <div className="grid-4" style={{ marginBottom: 16 }}>
          <div className="card" style={{ padding: 16 }}>
            <div className="text-sm text-muted">成功模式</div>
            <div className="stat-value" style={{ color: 'var(--accent-green)' }}>{stats.patternCount ?? patterns.length}</div>
          </div>
          <div className="card" style={{ padding: 16 }}>
            <div className="text-sm text-muted">失败教训</div>
            <div className="stat-value" style={{ color: 'var(--accent-red)' }}>{stats.lessonCount ?? lessons.length}</div>
          </div>
          <div className="card" style={{ padding: 16 }}>
            <div className="text-sm text-muted">总复盘次数</div>
            <div className="stat-value" style={{ color: 'var(--accent-blue)' }}>{stats.totalReviews ?? 0}</div>
          </div>
          <div className="card" style={{ padding: 16 }}>
            <div className="text-sm text-muted">成功率</div>
            <div className="stat-value" style={{ color: 'var(--accent-cyan)' }}>
              {stats.successRate != null ? `${(stats.successRate * 100).toFixed(1)}%` : '--'}
            </div>
          </div>
        </div>
      )}

      <div className="tabs">
        {[
          { key: 'patterns', label: `✅ 成功模式 (${patterns.length})` },
          { key: 'lessons', label: `❌ 失败教训 (${lessons.length})` },
          { key: 'analyze', label: '🔍 单次复盘' },
        ].map(t => (
          <div key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
            {t.label}
          </div>
        ))}
      </div>

      {activeTab === 'patterns' && (
        <div>
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="flex gap-2">
              <input
                className="input"
                placeholder="搜索成功模式..."
                style={{ flex: 1 }}
                onKeyDown={e => e.key === 'Enter' && handleSearchPatterns(e.target.value)}
              />
              <button className="btn btn-secondary" onClick={() => {
                const input = document.querySelector('.review-page .input')
                if (input) handleSearchPatterns(input.value)
              }}>搜索</button>
            </div>
          </div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 40 }}><div className="spinner"></div></div>
          ) : patterns.length > 0 ? (
            patterns.map((p, i) => (
              <div key={i} className="card animate-fade-in">
                <div className="flex justify-between items-center">
                  <div>
                    <div className="text-sm font-weight:600">{p.name || p.patternId}</div>
                    <div className="text-xs text-muted">{p.description || p.summary || '-'}</div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="badge badge-green">评分: {p.score || p.value || 0}</span>
                    {p.usageCount != null && <span className="badge badge-blue">使用 {p.usageCount} 次</span>}
                  </div>
                </div>
                {p.context && (
                  <div className="text-sm text-muted" style={{ marginTop: 8 }}>
                    上下文: {p.context}
                  </div>
                )}
                {p.successFactors && p.successFactors.length > 0 && (
                  <div className="flex gap-2" style={{ marginTop: 8, flexWrap: 'wrap' }}>
                    {p.successFactors.map((f, fi) => <span key={fi} className="badge badge-green">{f}</span>)}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="empty-state"><div className="empty-state-text">暂无成功模式</div></div>
          )}
        </div>
      )}

      {activeTab === 'lessons' && (
        <div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 40 }}><div className="spinner"></div></div>
          ) : lessons.length > 0 ? (
            lessons.map((l, i) => (
              <div key={i} className="card animate-fade-in">
                <div className="flex justify-between items-center">
                  <div>
                    <div className="text-sm font-weight:600">{l.name || l.lessonId}</div>
                    <div className="text-xs text-muted">{l.description || l.summary || l.reason || '-'}</div>
                  </div>
                  {l.severity && <span className={`badge ${l.severity === 'HIGH' ? 'badge-red' : l.severity === 'MEDIUM' ? 'badge-orange' : 'badge-blue'}`}>{l.severity}</span>}
                </div>
                {l.failureCauses && l.failureCauses.length > 0 && (
                  <div className="flex gap-2" style={{ marginTop: 8, flexWrap: 'wrap' }}>
                    {l.failureCauses.map((c, ci) => <span key={ci} className="badge badge-red">{c}</span>)}
                  </div>
                )}
              </div>
            ))
          ) : (
            <div className="empty-state"><div className="empty-state-text">暂无失败教训</div></div>
          )}
        </div>
      )}

      {activeTab === 'analyze' && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>🔍 执行复盘分析</div>
            <div className="input-group">
              <label className="input-label">Trace ID</label>
              <input className="input" value={traceId} onChange={e => setTraceId(e.target.value)} placeholder="输入 Trace ID" />
            </div>
            <button className="btn btn-primary" onClick={handleAnalyze} disabled={loading || !traceId.trim()}>
              {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '🔍'} 开始复盘
            </button>
          </div>

          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>📊 复盘结果</div>
            {reviewResult ? (
              <div>
                {reviewResult.summary && (
                  <div className="text-sm" style={{ marginBottom: 12 }}>{reviewResult.summary}</div>
                )}
                {reviewResult.successFactors && reviewResult.successFactors.length > 0 && (
                  <div style={{ marginBottom: 12 }}>
                    <div className="text-sm text-green" style={{ marginBottom: 6 }}>✅ 成功因素:</div>
                    {reviewResult.successFactors.map((f, i) => (
                      <div key={i} className="text-sm text-muted" style={{ paddingLeft: 8 }}>• {f}</div>
                    ))}
                  </div>
                )}
                {reviewResult.failureCauses && reviewResult.failureCauses.length > 0 && (
                  <div style={{ marginBottom: 12 }}>
                    <div className="text-sm text-red" style={{ marginBottom: 6 }}>❌ 失败原因:</div>
                    {reviewResult.failureCauses.map((f, i) => (
                      <div key={i} className="text-sm text-muted" style={{ paddingLeft: 8 }}>• {f}</div>
                    ))}
                  </div>
                )}
                {reviewResult.recommendations && reviewResult.recommendations.length > 0 && (
                  <div>
                    <div className="text-sm text-blue" style={{ marginBottom: 6 }}>💡 改进建议:</div>
                    {reviewResult.recommendations.map((r, i) => (
                      <div key={i} className="text-sm text-muted" style={{ paddingLeft: 8 }}>• {r}</div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="empty-state"><div className="empty-state-text">输入 Trace ID 后开始复盘</div></div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
