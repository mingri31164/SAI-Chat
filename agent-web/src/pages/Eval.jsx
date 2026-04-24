/**
 * 评测系统页面
 *
 * 展示：
 * - 评测用例库（分类浏览）
 * - 一键评测（执行全部/冒烟测试）
 * - 评测报告（通过率、各维度得分、失败用例）
 * - 评测结果可视化
 */
import React, { useState, useEffect } from 'react'
import { evalApi } from '../utils/api'

function CaseCard({ c, onSelect }) {
  const colorMap = { EASY: 'green', MEDIUM: 'orange', HARD: 'red', EXPERT: 'purple' }
  return (
    <div className="case-card" onClick={() => onSelect(c)}>
      <div className="flex justify-between items-center">
        <span className="text-sm font-mono">{c.id}</span>
        <span className={`badge badge-${colorMap[c.difficulty] || 'blue'}`}>{c.difficulty}</span>
      </div>
      <div className="text-sm" style={{ marginTop: 6, fontWeight: 500 }}>{c.name}</div>
      <div className="text-xs text-muted" style={{ marginTop: 4 }}>{c.description}</div>
      {c.tags && c.tags.length > 0 && (
        <div className="flex gap-2" style={{ marginTop: 8 }}>
          {c.tags.map((t, i) => <span key={i} className="badge badge-blue">{t}</span>)}
        </div>
      )}
    </div>
  )
}

function ReportOverview({ report }) {
  if (!report) return null
  const passRate = report.passRate != null ? (report.passRate * 100).toFixed(1) : '--'
  const overallScore = report.overallScore?.toFixed(1) || '--'

  return (
    <div>
      <div className="grid-4" style={{ marginBottom: 16 }}>
        <div className="card" style={{ padding: 16 }}>
          <div className="text-sm text-muted">总用例数</div>
          <div className="stat-value" style={{ color: 'var(--accent-blue)' }}>{report.totalCases ?? 0}</div>
        </div>
        <div className="card" style={{ padding: 16 }}>
          <div className="text-sm text-muted">通过</div>
          <div className="stat-value" style={{ color: 'var(--accent-green)' }}>{report.passedCases ?? 0}</div>
        </div>
        <div className="card" style={{ padding: 16 }}>
          <div className="text-sm text-muted">失败</div>
          <div className="stat-value" style={{ color: 'var(--accent-red)' }}>{report.failedCases ?? 0}</div>
        </div>
        <div className="card" style={{ padding: 16 }}>
          <div className="text-sm text-muted">通过率</div>
          <div className="stat-value" style={{ color: passRate > 70 ? 'var(--accent-green)' : 'var(--accent-red)' }}>
            {passRate}%
          </div>
          <div className="progress-bar" style={{ marginTop: 8 }}>
            <div
              className={`progress-fill ${passRate > 70 ? 'success' : 'danger'}`}
              style={{ width: `${passRate}%` }}
            />
          </div>
        </div>
      </div>

      {/* 维度得分 */}
      {report.dimensionScores && Object.keys(report.dimensionScores).length > 0 && (
        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-title" style={{ marginBottom: 12 }}>📊 各维度得分</div>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr><th>维度</th><th>平均得分</th><th>通过率</th></tr>
              </thead>
              <tbody>
                {Object.entries(report.dimensionScores).map(([dim, score]) => (
                  <tr key={dim}>
                    <td>{dim}</td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="progress-bar" style={{ flex: 1 }}>
                          <div className="progress-fill" style={{ width: `${score * 100}%` }} />
                        </div>
                        <span style={{ minWidth: 40, textAlign: 'right' }}>{(score * 100).toFixed(1)}%</span>
                      </div>
                    </td>
                    <td><span className={`badge ${score >= 0.7 ? 'badge-green' : score >= 0.5 ? 'badge-orange' : 'badge-red'}`}>{(score >= 0.7 ? 'PASS' : score >= 0.5 ? 'PARTIAL' : 'FAIL')}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 按标签统计 */}
      {report.tagStatistics && Object.keys(report.tagStatistics).length > 0 && (
        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-title" style={{ marginBottom: 12 }}>🏷 按标签统计</div>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr><th>标签</th><th>用例数</th><th>通过率</th><th>平均耗时</th></tr>
              </thead>
              <tbody>
                {Object.entries(report.tagStatistics).map(([tag, stat]) => (
                  <tr key={tag}>
                    <td><span className="badge badge-blue">{tag}</span></td>
                    <td>{stat.count}</td>
                    <td>{((stat.passRate || 0) * 100).toFixed(1)}%</td>
                    <td>{((stat.avgDurationMs || 0) / 1000).toFixed(2)}s</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 失败用例 */}
      {report.failedResults && report.failedResults.length > 0 && (
        <div className="card">
          <div className="card-title" style={{ marginBottom: 12, color: 'var(--accent-red)' }}>
            ⚠ 失败用例 ({report.failedResults.length})
          </div>
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr><th>用例</th><th>问题</th><th>得分</th><th>原因</th></tr>
              </thead>
              <tbody>
                {report.failedResults.map((r, i) => (
                  <tr key={i}>
                    <td><span className="font-mono text-xs">{r.testCaseId}</span></td>
                    <td style={{ maxWidth: 200 }} className="text-sm">{r.question?.substring(0, 60)}...</td>
                    <td><span className="text-red">{(r.totalScore || 0).toFixed(1)}</span></td>
                    <td className="text-sm text-muted">{r.failureReason || '详见报告'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

export default function Eval() {
  const [activeTab, setActiveTab] = useState('run')
  const [library, setLibrary] = useState([])
  const [stats, setStats] = useState(null)
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(false)
  const [running, setRunning] = useState(false)
  const [parallel, setParallel] = useState(false)
  const [selectedCase, setSelectedCase] = useState(null)

  useEffect(() => {
    loadLibrary()
  }, [])

  const loadLibrary = async () => {
    setLoading(true)
    try {
      const [libRes, statsRes] = await Promise.allSettled([
        evalApi.getLibrary(),
        evalApi.getLibraryStats(),
      ])
      if (libRes.status === 'fulfilled') setLibrary(libRes.value.data || [])
      if (statsRes.status === 'fulfilled') setStats(statsRes.value.data)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleRunAll = async () => {
    setRunning(true)
    setReport(null)
    try {
      const res = await evalApi.runAll(parallel)
      setReport(res.data)
    } catch (e) { console.error(e) }
    finally { setRunning(false) }
  }

  const handleSmokeTest = async () => {
    setRunning(true)
    setReport(null)
    try {
      const res = await evalApi.runSmokeTest()
      setReport(res.data)
    } catch (e) { console.error(e) }
    finally { setRunning(false) }
  }

  const loadCategory = async (category) => {
    setLoading(true)
    try {
      let res
      if (category === 'simple-qa') res = await evalApi.getLibrarySimpleQA()
      else if (category === 'tool-call') res = await evalApi.getLibraryToolCall()
      else if (category === 'reasoning') res = await evalApi.getLibraryReasoning()
      else res = await evalApi.getLibrary()
      setLibrary(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  return (
    <div className="eval-page">
      <div className="page-header">
        <div>
          <div className="page-title">评测系统</div>
          <div className="page-subtitle">Keyword / Pattern / Reject / ToolCall / Performance — 5 维度自动评测</div>
        </div>
      </div>

      <div className="tabs">
        {[
          { key: 'run', label: '执行评测' },
          { key: 'library', label: `用例库 (${library.length})` },
          { key: 'report', label: '评测报告' },
        ].map(t => (
          <div key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
            {t.label}
          </div>
        ))}
      </div>

      {activeTab === 'run' && (
        <div>
          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>🚀 执行评测</div>
            <div className="flex items-center gap-4" style={{ marginBottom: 16 }}>
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={parallel} onChange={e => setParallel(e.target.checked)} />
                <span className="text-sm">并行执行（更快，适合大规模用例）</span>
              </label>
            </div>
            <div className="flex gap-4">
              <button className="btn btn-primary btn-lg" onClick={handleRunAll} disabled={running}>
                {running ? <span className="spinner" style={{ width: 16, height: 16 }}></span> : '▶'}
                执行全部评测 ({library.length} 个用例)
              </button>
              <button className="btn btn-success btn-lg" onClick={handleSmokeTest} disabled={running}>
                冒烟测试 (2 个用例)
              </button>
            </div>
          </div>

          {running && (
            <div className="card animate-fade-in" style={{ borderColor: 'var(--accent-blue)' }}>
              <div className="flex items-center gap-2">
                <div className="spinner"></div>
                <span className="text-sm">评测执行中...</span>
              </div>
            </div>
          )}

          {report && (
            <div className="animate-fade-in">
              <ReportOverview report={report} />
            </div>
          )}
        </div>
      )}

      {activeTab === 'library' && (
        <div>
          <div className="card" style={{ marginBottom: 16 }}>
            <div className="flex gap-2">
              <button className="btn btn-secondary btn-sm" onClick={() => loadLibrary()}>全部</button>
              <button className="btn btn-secondary btn-sm" onClick={() => loadCategory('simple-qa')}>简单问答</button>
              <button className="btn btn-secondary btn-sm" onClick={() => loadCategory('tool-call')}>工具调用</button>
              <button className="btn btn-secondary btn-sm" onClick={() => loadCategory('reasoning')}>推理</button>
            </div>
          </div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 40 }}><div className="spinner"></div></div>
          ) : (
            <div className="card-grid">
              {library.map((c, i) => <CaseCard key={i} c={c} onSelect={setSelectedCase} />)}
            </div>
          )}
          {selectedCase && (
            <div className="card animate-fade-in" style={{ marginTop: 16 }}>
              <div className="card-header">
                <div className="card-title">用例详情: {selectedCase.id}</div>
                <button className="btn btn-sm btn-secondary" onClick={() => setSelectedCase(null)}>关闭</button>
              </div>
              <div className="text-sm"><strong>名称:</strong> {selectedCase.name}</div>
              <div className="text-sm" style={{ marginTop: 8 }}><strong>描述:</strong> {selectedCase.description}</div>
              <div className="text-sm" style={{ marginTop: 8 }}><strong>输入:</strong> {selectedCase.input}</div>
              {selectedCase.expectedKeywords?.length > 0 && (
                <div className="text-sm" style={{ marginTop: 8 }}>
                  <strong>期望关键词:</strong>
                  <div className="flex gap-2" style={{ marginTop: 4 }}>
                    {selectedCase.expectedKeywords.map((k, i) => <span key={i} className="badge badge-green">{k}</span>)}
                  </div>
                </div>
              )}
              {selectedCase.maxIterations != null && (
                <div className="text-sm" style={{ marginTop: 8 }}>
                  <strong>最大迭代:</strong> {selectedCase.maxIterations}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {activeTab === 'report' && (
        <div>
          {report ? (
            <ReportOverview report={report} />
          ) : (
            <div className="empty-state">
              <div className="empty-state-icon">📊</div>
              <div className="empty-state-text">执行评测后，报告将显示在这里</div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
