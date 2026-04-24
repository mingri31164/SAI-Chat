/**
 * 观测中心页面
 *
 * 展示：
 * - 全局指标快照（请求量、成功率、延迟百分位、Token消耗）
 * - 工具调用指标（各工具成功率、平均耗时）
 * - 追踪列表（最近追踪、失败追踪）
 * - 延迟分析（LLM时间/工具时间占比）
 */
import React, { useState, useEffect } from 'react'
import { observeApi } from '../utils/api'

function MetricCard({ label, value, unit, change, color }) {
  const colorMap = {
    blue: 'var(--accent-blue)',
    green: 'var(--accent-green)',
    orange: 'var(--accent-orange)',
    red: 'var(--accent-red)',
    cyan: 'var(--accent-cyan)',
    purple: 'var(--accent-purple)',
  }
  return (
    <div className="card" style={{ padding: 16 }}>
      <div className="text-sm text-muted">{label}</div>
      <div className="stat-value" style={{ color: colorMap[color] || 'var(--text-primary)' }}>
        {value ?? '--'}<span className="text-sm" style={{ fontSize: 14, color: 'var(--text-secondary)', marginLeft: 4 }}>{unit || ''}</span>
      </div>
      {change != null && (
        <div className={`stat-change ${change >= 0 ? 'up' : 'down'}`}>
          {change >= 0 ? '↑' : '↓'} {Math.abs(change).toFixed(1)}%
        </div>
      )}
    </div>
  )
}

function TraceTable({ traces, loading }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center" style={{ padding: 40 }}>
        <div className="spinner"></div>
      </div>
    )
  }

  if (!traces || traces.length === 0) {
    return <div className="empty-state"><div className="empty-state-text">暂无追踪数据</div></div>
  }

  return (
    <div className="table-wrapper">
      <table className="table">
        <thead>
          <tr>
            <th>Trace ID</th>
            <th>Session</th>
            <th>Status</th>
            <th>Duration</th>
            <th>Spans</th>
            <th>Error</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {traces.map((trace, i) => (
            <tr key={i}>
              <td><span className="font-mono text-xs">{trace.traceId?.substring(0, 16)}...</span></td>
              <td><span className="font-mono text-xs">{trace.sessionId?.substring(0, 12)}...</span></td>
              <td>
                <span className={`badge ${
                  trace.status === 'SUCCESS' ? 'badge-green' :
                  trace.status === 'FAILED' ? 'badge-red' :
                  trace.status === 'TIMEOUT' ? 'badge-orange' :
                  'badge-blue'
                }`}>{trace.status || 'RUNNING'}</span>
              </td>
              <td>{trace.totalDurationMs ? `${(trace.totalDurationMs / 1000).toFixed(2)}s` : '-'}</td>
              <td>{trace.spanList?.length || 0}</td>
              <td>
                {trace.errorMessage ? (
                  <span className="text-red text-xs" title={trace.errorMessage}>
                    {trace.errorMessage.substring(0, 30)}...
                  </span>
                ) : '-'}
              </td>
              <td>
                <button className="btn btn-sm btn-secondary" onClick={() => alert(`Trace: ${trace.traceId}`)}>
                  详情
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function LatencyChart({ analysis }) {
  if (!analysis) return null
  const total = analysis.totalDurationMs || 1
  const llm = ((analysis.llmCallDurationMs || 0) / total * 100).toFixed(1)
  const tool = ((analysis.toolCallDurationMs || 0) / total * 100).toFixed(1)
  const other = (100 - llm - tool).toFixed(1)

  return (
    <div className="card">
      <div className="card-title" style={{ marginBottom: 16 }}>⏱ 延迟分布</div>
      <div style={{ marginBottom: 16 }}>
        <div className="flex justify-between text-sm" style={{ marginBottom: 8 }}>
          <span>总耗时: <strong>{(total / 1000).toFixed(2)}s</strong></span>
          <span>Span数: <strong>{analysis.spanCount}</strong></span>
        </div>
        <div className="progress-bar" style={{ height: 24, borderRadius: 4 }}>
          <div className="progress-fill" style={{ width: `${llm}%`, background: 'var(--accent-blue)' }} />
          <div className="progress-fill" style={{ width: `${tool}%`, background: 'var(--accent-orange)' }} />
          <div className="progress-fill" style={{ width: `${other}%`, background: 'var(--bg-tertiary)' }} />
        </div>
      </div>
      <div className="flex justify-between">
        <div className="flex items-center gap-2">
          <div style={{ width: 12, height: 12, borderRadius: 2, background: 'var(--accent-blue)' }} />
          <span className="text-sm">LLM: {llm}%</span>
        </div>
        <div className="flex items-center gap-2">
          <div style={{ width: 12, height: 12, borderRadius: 2, background: 'var(--accent-orange)' }} />
          <span className="text-sm">工具: {tool}%</span>
        </div>
        <div className="flex items-center gap-2">
          <div style={{ width: 12, height: 12, borderRadius: 2, background: 'var(--bg-tertiary)', border: '1px solid var(--border)' }} />
          <span className="text-sm">其他: {other}%</span>
        </div>
      </div>
    </div>
  )
}

export default function Observe() {
  const [activeTab, setActiveTab] = useState('metrics')
  const [metrics, setMetrics] = useState(null)
  const [toolMetrics, setToolMetrics] = useState([])
  const [recentTraces, setRecentTraces] = useState([])
  const [failedTraces, setFailedTraces] = useState([])
  const [traceStats, setTraceStats] = useState(null)
  const [loading, setLoading] = useState(false)
  const [refreshing, setRefreshing] = useState(false)

  const loadData = async (showLoading = true) => {
    if (showLoading) setLoading(true)
    try {
      const [metricsRes, toolsRes, recentRes, failedRes, statsRes] = await Promise.allSettled([
        observeApi.getMetricsSnapshot(),
        observeApi.getToolMetrics(),
        observeApi.getRecentTraces(20),
        observeApi.getFailedTraces(),
        observeApi.getTraceStatistics(),
      ])

      if (metricsRes.status === 'fulfilled') setMetrics(metricsRes.value.data)
      if (toolsRes.status === 'fulfilled') setToolMetrics(toolsRes.value.data || [])
      if (recentRes.status === 'fulfilled') setRecentTraces(recentRes.value.data || [])
      if (failedRes.status === 'fulfilled') setFailedTraces(failedRes.value.data || [])
      if (statsRes.status === 'fulfilled') setTraceStats(statsRes.value.data)
    } catch (err) {
      console.error('加载观测数据失败', err)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => { loadData() }, [])

  const handleRefresh = () => {
    setRefreshing(true)
    loadData()
  }

  const handleResetMetrics = async () => {
    await observeApi.resetMetrics()
    loadData()
  }

  const handleReport = async () => {
    await observeApi.triggerMetricsReport()
  }

  return (
    <div className="observe-page">
      <div className="page-header">
        <div>
          <div className="page-title">观测中心</div>
          <div className="page-subtitle">Trace / Metrics / Latency — 全链路可观测性</div>
        </div>
        <div className="flex gap-2">
          <button className="btn btn-secondary btn-sm" onClick={handleRefresh} disabled={refreshing}>
            {refreshing ? <span className="spinner" style={{ width: 12, height: 12 }}></span> : '↻'}
            刷新
          </button>
          <button className="btn btn-secondary btn-sm" onClick={handleReport}>📊 生成报告</button>
          <button className="btn btn-danger btn-sm" onClick={handleResetMetrics}>🗑 重置指标</button>
        </div>
      </div>

      <div className="tabs">
        {['metrics', 'traces', 'tools'].map(tab => (
          <div
            key={tab}
            className={`tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'metrics' ? '📊 指标' : tab === 'traces' ? '🔗 追踪' : '⚙ 工具'}
          </div>
        ))}
      </div>

      {activeTab === 'metrics' && (
        <div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 60 }}><div className="spinner" style={{ width: 32, height: 32 }}></div></div>
          ) : (
            <>
              {/* 核心指标卡片 */}
              <div className="grid-4" style={{ marginBottom: 16 }}>
                <MetricCard
                  label="总请求数"
                  value={metrics?.totalRequests ?? 0}
                  color="blue"
                />
                <MetricCard
                  label="成功率"
                  value={metrics?.successRate != null ? (metrics.successRate * 100).toFixed(1) : null}
                  unit="%"
                  color="green"
                />
                <MetricCard
                  label="平均延迟"
                  value={metrics?.averageDurationMs != null ? (metrics.averageDurationMs / 1000).toFixed(2) : null}
                  unit="s"
                  color="orange"
                />
                <MetricCard
                  label="Token消耗"
                  value={metrics?.totalTokens != null ? (metrics.totalTokens / 1000).toFixed(1) : null}
                  unit="K"
                  color="cyan"
                />
              </div>

              {/* 百分位延迟 */}
              <div className="grid-3" style={{ marginBottom: 16 }}>
                <div className="card">
                  <div className="text-sm text-muted">P50 延迟</div>
                  <div className="stat-value" style={{ color: 'var(--accent-green)' }}>
                    {metrics?.p50Latency != null ? (metrics.p50Latency / 1000).toFixed(2) : '--'}
                    <span className="text-sm" style={{ fontSize: 14, color: 'var(--text-secondary)', marginLeft: 4 }}>s</span>
                  </div>
                  <div className="progress-bar" style={{ marginTop: 8 }}>
                    <div
                      className="progress-fill success"
                      style={{ width: `${Math.min(100, (metrics?.p50Latency || 0) / 5)}%` }}
                    />
                  </div>
                </div>
                <div className="card">
                  <div className="text-sm text-muted">P95 延迟</div>
                  <div className="stat-value" style={{ color: 'var(--accent-orange)' }}>
                    {metrics?.p95Latency != null ? (metrics.p95Latency / 1000).toFixed(2) : '--'}
                    <span className="text-sm" style={{ fontSize: 14, color: 'var(--text-secondary)', marginLeft: 4 }}>s</span>
                  </div>
                  <div className="progress-bar" style={{ marginTop: 8 }}>
                    <div
                      className="progress-fill warning"
                      style={{ width: `${Math.min(100, (metrics?.p95Latency || 0) / 15)}%` }}
                    />
                  </div>
                </div>
                <div className="card">
                  <div className="text-sm text-muted">P99 延迟</div>
                  <div className="stat-value" style={{ color: 'var(--accent-red)' }}>
                    {metrics?.p99Latency != null ? (metrics.p99Latency / 1000).toFixed(2) : '--'}
                    <span className="text-sm" style={{ fontSize: 14, color: 'var(--text-secondary)', marginLeft: 4 }}>s</span>
                  </div>
                  <div className="progress-bar" style={{ marginTop: 8 }}>
                    <div
                      className="progress-fill danger"
                      style={{ width: `${Math.min(100, (metrics?.p99Latency || 0) / 25)}%` }}
                    />
                  </div>
                </div>
              </div>

              {/* 追踪统计 */}
              {traceStats && (
                <div className="grid-4" style={{ marginBottom: 16 }}>
                  <MetricCard label="追踪总数" value={traceStats.totalTraces} color="blue" />
                  <MetricCard label="成功追踪" value={traceStats.successTraces} color="green" />
                  <MetricCard label="失败追踪" value={traceStats.failedTraces} color="red" />
                  <MetricCard
                    label="平均耗时"
                    value={traceStats.averageDurationMs != null ? (traceStats.averageDurationMs / 1000).toFixed(2) : null}
                    unit="s"
                    color="purple"
                  />
                </div>
              )}

              {/* LLM vs 工具时间分布 */}
              <LatencyChart analysis={metrics?.latencyBreakdown} />
            </>
          )}
        </div>
      )}

      {activeTab === 'traces' && (
        <div>
          <div className="tabs" style={{ marginBottom: 12 }}>
            <div className={`tab ${activeTab === 'traces' && !window._traceSubTab ? 'active' : ''}`}
                 onClick={() => { window._traceSubTab = 'recent'; setActiveTab('traces') }}>
              最近追踪
            </div>
          </div>
          <div className="card">
            <div className="card-header">
              <div className="card-title">🔗 最近追踪 ({recentTraces.length})</div>
            </div>
            <TraceTable traces={recentTraces} loading={loading} />
          </div>
          {failedTraces.length > 0 && (
            <div className="card">
              <div className="card-header">
                <div className="card-title" style={{ color: 'var(--accent-red)' }}>⚠ 失败追踪 ({failedTraces.length})</div>
              </div>
              <TraceTable traces={failedTraces} loading={false} />
            </div>
          )}
        </div>
      )}

      {activeTab === 'tools' && (
        <div>
          <div className="card">
            <div className="card-header">
              <div className="card-title">⚙ 工具调用指标</div>
            </div>
            {toolMetrics && Object.keys(toolMetrics).length > 0 ? (
              <div className="table-wrapper">
                <table className="table">
                  <thead>
                    <tr>
                      <th>工具 ID</th>
                      <th>总调用</th>
                      <th>成功</th>
                      <th>失败</th>
                      <th>成功率</th>
                      <th>平均耗时</th>
                      <th>占比</th>
                    </tr>
                  </thead>
                  <tbody>
                    {Object.entries(toolMetrics).map(([toolId, m]) => (
                      <tr key={toolId}>
                        <td><span className="font-mono text-sm">{toolId}</span></td>
                        <td>{m.totalCalls ?? 0}</td>
                        <td className="text-green">{m.successCalls ?? 0}</td>
                        <td className="text-red">{m.failedCalls ?? 0}</td>
                        <td>
                          <span className={`badge ${(m.successRate || 0) > 0.9 ? 'badge-green' : (m.successRate || 0) > 0.7 ? 'badge-orange' : 'badge-red'}`}>
                            {((m.successRate || 0) * 100).toFixed(1)}%
                          </span>
                        </td>
                        <td>{m.avgDurationMs != null ? `${(m.avgDurationMs / 1000).toFixed(2)}s` : '-'}</td>
                        <td>{m.totalCalls != null && metrics?.toolCalls > 0 ? `${((m.totalCalls / metrics.toolCalls) * 100).toFixed(1)}%` : '-'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state"><div className="empty-state-text">暂无工具调用数据</div></div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
