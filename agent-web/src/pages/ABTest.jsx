/**
 * A/B 测试页面
 */
import React, { useState, useEffect } from 'react'
import { abtestApi } from '../utils/api'

function TestCard({ test, onAction }) {
  const statusColor = {
    DRAFT: 'blue', RUNNING: 'green', PAUSED: 'orange',
    COMPLETED: 'purple', CANCELLED: 'red'
  }
  return (
    <div className="card animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <div className="text-sm font-weight:600">{test.name || test.testId}</div>
          <div className="text-xs text-muted">{test.description}</div>
        </div>
        <span className={`badge badge-${statusColor[test.status] || 'blue'}`}>{test.status}</span>
      </div>
      <div className="grid-3" style={{ marginTop: 12 }}>
        <div className="text-xs text-muted">变体数: <strong className="text-primary">{test.variantCount || 2}</strong></div>
        <div className="text-xs text-muted">流量分配: <strong className="text-primary">{Array.isArray(test.variantWeights) ? test.variantWeights.map(w => `${(w * 100).toFixed(0)}%`).join(' vs ') : '-'}</strong></div>
        <div className="text-xs text-muted">最小样本: <strong className="text-primary">{test.minSampleSize || 1000}</strong></div>
      </div>
      <div className="flex gap-2" style={{ marginTop: 12 }}>
        {test.status === 'DRAFT' && (
          <button className="btn btn-sm btn-success" onClick={() => onAction('start', test.testId)}>▶ 启动</button>
        )}
        {test.status === 'RUNNING' && (
          <button className="btn btn-sm btn-warning" onClick={() => onAction('pause', test.testId)}>⏸ 暂停</button>
        )}
        {test.status === 'PAUSED' && (
          <button className="btn btn-sm btn-success" onClick={() => onAction('start', test.testId)}>▶ 继续</button>
        )}
        {(test.status === 'RUNNING' || test.status === 'PAUSED') && (
          <button className="btn btn-sm btn-danger" onClick={() => onAction('end', test.testId)}>■ 结束</button>
        )}
        <button className="btn btn-sm btn-secondary" onClick={() => onAction('result', test.testId)}>📊 结果</button>
      </div>
    </div>
  )
}

export default function ABTest() {
  const [tests, setTests] = useState([])
  const [runningTests, setRunningTests] = useState([])
  const [loading, setLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('list')
  const [resultData, setResultData] = useState(null)
  const [form, setForm] = useState({
    name: '',
    description: '',
    variantCount: 2,
    variantWeights: [0.5, 0.5],
    minSignificance: 0.95,
    minSampleSize: 1000,
  })

  useEffect(() => { loadTests() }, [])

  const loadTests = async () => {
    setLoading(true)
    try {
      const [allRes, runningRes] = await Promise.allSettled([
        abtestApi.getAll(),
        abtestApi.getRunning(),
      ])
      if (allRes.status === 'fulfilled') setTests(allRes.value.data || [])
      if (runningRes.status === 'fulfilled') setRunningTests(runningRes.value.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleAction = async (action, testId) => {
    try {
      if (action === 'start') await abtestApi.start(testId)
      else if (action === 'pause') await abtestApi.pause(testId)
      else if (action === 'end') await abtestApi.end(testId)
      else if (action === 'result') {
        const res = await abtestApi.getResult(testId)
        setResultData(res.data)
        setActiveTab('result')
        return
      }
      loadTests()
    } catch (e) { console.error(e) }
  }

  const handleCreate = async () => {
    if (!form.name.trim()) return
    setLoading(true)
    try {
      await abtestApi.create(form)
      loadTests()
      setActiveTab('list')
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  return (
    <div className="abtest-page">
      <div className="page-header">
        <div>
          <div className="page-title">A/B 测试中心</div>
          <div className="page-subtitle">双样本差异显著检验与流量自动分配</div>
        </div>
      </div>

      <div className="tabs">
        {[
          { key: 'list', label: `测试列表 (${tests.length})` },
          { key: 'create', label: '+ 创建测试' },
          { key: 'result', label: '测试结果' },
        ].map(t => (
          <div key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
            {t.label}
          </div>
        ))}
      </div>

      {activeTab === 'list' && (
        <div>
          {runningTests.length > 0 && (
            <div>
              <div className="card-title" style={{ marginBottom: 12, color: 'var(--accent-green)' }}>▶ 运行中 ({runningTests.length})</div>
              <div style={{ marginBottom: 16 }}>
                {runningTests.map((t, i) => <TestCard key={i} test={t} onAction={handleAction} />)}
              </div>
            </div>
          )}
          <div className="card-title" style={{ marginBottom: 12 }}>所有测试 ({tests.length})</div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 40 }}><div className="spinner"></div></div>
          ) : tests.length > 0 ? (
            tests.map((t, i) => <TestCard key={i} test={t} onAction={handleAction} />)
          ) : (
            <div className="empty-state">
              <div className="empty-state-icon">◑</div>
              <div className="empty-state-text">暂无测试，点击"创建测试"开始</div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'create' && (
        <div className="card">
          <div className="card-title" style={{ marginBottom: 16 }}>创建 A/B 测试</div>
          <div className="grid-2">
            <div className="input-group">
              <label className="input-label">测试名称</label>
              <input className="input" value={form.name} onChange={e => setForm(p => ({ ...p, name: e.target.value }))} placeholder="新 Prompt 效果对比" />
            </div>
            <div className="input-group">
              <label className="input-label">描述</label>
              <input className="input" value={form.description} onChange={e => setForm(p => ({ ...p, description: e.target.value }))} placeholder="对比两种 Prompt 策略" />
            </div>
            <div className="input-group">
              <label className="input-label">变体数量</label>
              <input className="input" type="number" min="2" max="5" value={form.variantCount} onChange={e => {
                const n = parseInt(e.target.value) || 2
                const w = 1 / n
                setForm(p => ({ ...p, variantCount: n, variantWeights: Array(n).fill(w) }))
              }} />
            </div>
            <div className="input-group">
              <label className="input-label">最小样本量</label>
              <input className="input" type="number" min="100" value={form.minSampleSize} onChange={e => setForm(p => ({ ...p, minSampleSize: parseInt(e.target.value) || 1000 }))} />
            </div>
            <div className="input-group">
              <label className="input-label">显著性阈值</label>
              <input className="input" type="number" min="0.8" max="0.99" step="0.01" value={form.minSignificance} onChange={e => setForm(p => ({ ...p, minSignificance: parseFloat(e.target.value) }))} />
            </div>
          </div>
          <div style={{ marginTop: 12 }}>
            <div className="text-sm text-muted" style={{ marginBottom: 8 }}>流量分配:</div>
            {form.variantWeights.map((w, i) => (
              <div key={i} className="input-group" style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <span className="text-sm" style={{ minWidth: 60 }}>变体 {i + 1}</span>
                <input
                  className="input"
                  type="range"
                  min="0" max="1" step="0.05"
                  value={w}
                  onChange={e => {
                    const val = parseFloat(e.target.value)
                    const newWeights = [...form.variantWeights]
                    newWeights[i] = val
                    const total = newWeights.reduce((a, b) => a + b, 0)
                    setForm(p => ({ ...p, variantWeights: newWeights.map(x => x / total) }))
                  }}
                  style={{ flex: 1 }}
                />
                <span className="text-sm font-mono" style={{ minWidth: 40 }}>{(w * 100).toFixed(0)}%</span>
              </div>
            ))}
          </div>
          <div style={{ marginTop: 16 }}>
            <button className="btn btn-primary" onClick={handleCreate} disabled={loading || !form.name.trim()}>
              {loading ? <span className="spinner" style={{ width: 14, height: 14 }}></span> : '+'} 创建测试
            </button>
          </div>
        </div>
      )}

      {activeTab === 'result' && (
        <div className="card">
          <div className="card-title" style={{ marginBottom: 16 }}>📊 测试结果</div>
          {resultData ? (
            <div>
              {resultData.variantResults && (
                <div className="grid-2">
                  {Object.entries(resultData.variantResults).map(([key, vr]) => (
                    <div key={key} className="card">
                      <div className="text-sm font-weight:600">变体 {key}</div>
                      <div className="stat-value" style={{ color: 'var(--accent-blue)', marginTop: 8 }}>{vr.sampleSize || 0}</div>
                      <div className="text-xs text-muted">样本量</div>
                      {vr.metrics && Object.entries(vr.metrics).map(([mk, mv]) => (
                        <div key={mk} className="flex justify-between" style={{ marginTop: 8 }}>
                          <span className="text-sm text-muted">{mk}</span>
                          <span className="text-sm">{(mv * 100).toFixed(2)}%</span>
                        </div>
                      ))}
                    </div>
                  ))}
                </div>
              )}
              {resultData.winner && (
                <div className="card" style={{ marginTop: 16, borderColor: 'var(--accent-green)' }}>
                  <div className="text-green text-sm font-weight:600">🏆 获胜变体: {resultData.winner}</div>
                  <div className="text-sm text-muted" style={{ marginTop: 4 }}>
                    置信度: {(resultData.confidence * 100).toFixed(1)}% | 提升: {resultData.improvement != null ? `${(resultData.improvement * 100).toFixed(1)}%` : '-'}
                  </div>
                </div>
              )}
              {resultData.significant != null && !resultData.significant && (
                <div className="card" style={{ marginTop: 16, borderColor: 'var(--accent-orange)' }}>
                  <div className="text-orange text-sm">⚠ 样本量不足以得出统计显著结论，需要更多数据</div>
                </div>
              )}
            </div>
          ) : (
            <div className="empty-state">
              <div className="empty-state-text">选择一个测试查看结果</div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
