/**
 * 安全中心页面
 */
import React, { useState } from 'react'
import { securityApi } from '../utils/api'

export default function Security() {
  const [activeTab, setActiveTab] = useState('permission')
  const [userId, setUserId] = useState('')
  const [permission, setPermission] = useState('')
  const [permResult, setPermResult] = useState(null)
  const [content, setContent] = useState('')
  const [filterResult, setFilterResult] = useState(null)
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(false)
  const [permList, setPermList] = useState([])

  const handleCheckPerm = async () => {
    if (!userId.trim() || !permission.trim()) return
    setLoading(true)
    try {
      const res = await securityApi.checkPermission(userId, permission)
      setPermResult(res.data)
    } catch (e) { setPermResult(false) }
    finally { setLoading(false) }
  }

  const handleGetPerms = async () => {
    if (!userId.trim()) return
    setLoading(true)
    try {
      const res = await securityApi.getUserPermissions(userId)
      setPermList(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleFilter = async (type) => {
    if (!content.trim()) return
    setLoading(true)
    try {
      const api = type === 'input' ? securityApi.filterInput : securityApi.filterOutput
      const res = await api(content, userId)
      setFilterResult(res.data)
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  const handleGetRules = async () => {
    setLoading(true)
    try {
      const res = await securityApi.getRules()
      setRules(res.data || [])
    } catch (e) { console.error(e) }
    finally { setLoading(false) }
  }

  return (
    <div className="security-page">
      <div className="page-header">
        <div>
          <div className="page-title">安全中心</div>
          <div className="page-subtitle">RBAC 权限 / 内容安全过滤 / 敏感词检测</div>
        </div>
      </div>

      <div className="tabs">
        {[
          { key: 'permission', label: '🔐 权限管理' },
          { key: 'content', label: '🛡️ 内容安全' },
          { key: 'rules', label: '📋 安全规则' },
        ].map(t => (
          <div key={t.key} className={`tab ${activeTab === t.key ? 'active' : ''}`} onClick={() => setActiveTab(t.key)}>
            {t.label}
          </div>
        ))}
      </div>

      {activeTab === 'permission' && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>🔐 权限检查</div>
            <div className="input-group">
              <label className="input-label">用户 ID</label>
              <input className="input" value={userId} onChange={e => setUserId(e.target.value)} placeholder="user_001" />
            </div>
            <div className="input-group">
              <label className="input-label">权限标识</label>
              <input className="input" value={permission} onChange={e => setPermission(e.target.value)} placeholder="tool:calculator" />
            </div>
            <div className="flex gap-2">
              <button className="btn btn-primary" onClick={handleCheckPerm} disabled={loading}>检查</button>
              <button className="btn btn-secondary" onClick={handleGetPerms} disabled={loading}>获取权限列表</button>
            </div>
            {permResult !== null && (
              <div className="card animate-fade-in" style={{ marginTop: 12, borderColor: permResult ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                <span className={permResult ? 'text-green' : 'text-red'}>
                  {permResult ? '✅ 权限通过' : '❌ 权限拒绝'}
                </span>
              </div>
            )}
            {permList.length > 0 && (
              <div style={{ marginTop: 16 }}>
                <div className="text-sm text-muted" style={{ marginBottom: 8 }}>用户权限列表:</div>
                <div className="flex gap-2" style={{ flexWrap: 'wrap' }}>
                  {permList.map((p, i) => <span key={i} className="badge badge-green">{p}</span>)}
                </div>
              </div>
            )}
          </div>

          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>📖 权限模型说明</div>
            <div className="table-wrapper">
              <table className="table">
                <thead><tr><th>角色</th><th>权限</th><th>说明</th></tr></thead>
                <tbody>
                  <tr><td><span className="badge badge-purple">ADMIN</span></td><td>所有权限</td><td>完全控制</td></tr>
                  <tr><td><span className="badge badge-blue">USER</span></td><td>基础操作</td><td>常规使用</td></tr>
                  <tr><td><span className="badge badge-orange">VIEWER</span></td><td>只读权限</td><td>仅查看</td></tr>
                  <tr><td><span className="badge badge-neutral">GUEST</span></td><td>受限权限</td><td>访客访问</td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'content' && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>🛡️ 内容安全过滤</div>
            <div className="input-group">
              <label className="input-label">用户 ID</label>
              <input className="input" value={userId} onChange={e => setUserId(e.target.value)} placeholder="留空则不区分用户" />
            </div>
            <div className="input-group">
              <label className="input-label">待检测内容</label>
              <textarea className="input" rows={4} value={content} onChange={e => setContent(e.target.value)} placeholder="输入待检测的文本内容..." />
            </div>
            <div className="flex gap-2">
              <button className="btn btn-primary" onClick={() => handleFilter('input')} disabled={loading}>过滤输入</button>
              <button className="btn btn-secondary" onClick={() => handleFilter('output')} disabled={loading}>过滤输出</button>
            </div>

            {filterResult && (
              <div className="card animate-fade-in" style={{ marginTop: 12, borderColor: filterResult.allowed !== false ? 'var(--accent-green)' : 'var(--accent-red)' }}>
                <div className="flex items-center gap-2" style={{ marginBottom: 8 }}>
                  <span className={`badge ${filterResult.allowed !== false ? 'badge-green' : 'badge-red'}`}>
                    {filterResult.allowed !== false ? '✅ 通过' : '❌ 拦截'}
                  </span>
                  <span className="text-sm text-muted">风险分数: {((filterResult.riskScore || 0) * 100).toFixed(1)}%</span>
                </div>
                {filterResult.violations && filterResult.violations.length > 0 && (
                  <div>
                    <div className="text-sm text-muted" style={{ marginBottom: 6 }}>违规项:</div>
                    {filterResult.violations.map((v, i) => (
                      <div key={i} className="text-sm" style={{ padding: '4px 8px', background: 'var(--bg-tertiary)', borderRadius: 4, marginBottom: 4 }}>
                        <span className="badge badge-red">{v.rule || v.type}</span>
                        <span style={{ marginLeft: 8 }}>{v.message || v.reason}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="card">
            <div className="card-title" style={{ marginBottom: 16 }}>🛡️ 安全规则说明</div>
            <div className="table-wrapper">
              <table className="table">
                <thead><tr><th>规则</th><th>风险分数</th><th>说明</th></tr></thead>
                <tbody>
                  <tr><td><span className="badge badge-red">prompt-injection</span></td><td className="text-red">100%</td><td>Prompt 注入攻击</td></tr>
                  <tr><td><span className="badge badge-orange">sensitive-words</span></td><td className="text-orange">90%</td><td>敏感词检测</td></tr>
                  <tr><td><span className="badge badge-orange">code-injection</span></td><td className="text-orange">90%</td><td>代码注入检测</td></tr>
                  <tr><td><span className="badge badge-blue">pii-detection</span></td><td className="text-blue">60%</td><td>个人信息检测</td></tr>
                </tbody>
              </table>
            </div>
            <div className="text-sm text-muted" style={{ marginTop: 12 }}>
              风险分数 &gt;= 70% 时内容被拦截
            </div>
          </div>
        </div>
      )}

      {activeTab === 'rules' && (
        <div className="card">
          <div className="card-header">
            <div className="card-title">📋 内容安全规则</div>
            <button className="btn btn-secondary btn-sm" onClick={handleGetRules}>刷新</button>
          </div>
          {loading ? (
            <div className="flex items-center justify-center" style={{ padding: 40 }}><div className="spinner"></div></div>
          ) : rules.length > 0 ? (
            <div className="table-wrapper">
              <table className="table">
                <thead><tr><th>规则名称</th><th>类型</th><th>严重程度</th><th>状态</th></tr></thead>
                <tbody>
                  {rules.map((r, i) => (
                    <tr key={i}>
                      <td><span className="font-mono text-sm">{r.name || r.id}</span></td>
                      <td>{r.type || r.category || '-'}</td>
                      <td><span className={`badge ${{ CRITICAL: 'badge-red', HIGH: 'badge-orange', MEDIUM: 'badge-blue', LOW: 'badge-green' }[r.severity] || 'badge-blue'}`}>{r.severity || '-'}</span></td>
                      <td><span className={`badge ${r.enabled ? 'badge-green' : 'badge-neutral'}`}>{r.enabled ? '启用' : '禁用'}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state"><div className="empty-state-text">点击刷新加载安全规则</div></div>
          )}
        </div>
      )}
    </div>
  )
}
