/**
 * 统一 API 封装
 * 基于 Axios，封装请求拦截、响应处理、SSE 支持
 */
import axios from 'axios'
import { useState, useEffect, useRef } from 'react'

// 创建 Axios 实例
const api = axios.create({
  baseURL: '',
  timeout: 60000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截器
api.interceptors.request.use(config => {
  const token = localStorage.getItem('agent_token')
  if (token) {
    config.headers['Authorization'] = token
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    const msg = error.response?.data?.message || error.message || '网络错误'
    console.error('[API Error]', msg)
    return Promise.reject(error)
  }
)

export default api

// ============ SSE 工具 ============

/**
 * 创建 SSE 连接
 * @param {string} url - SSE 端点
 * @param {object} params - 查询参数
 * @param {object} callbacks - 事件回调
 * @returns {{ close: function }} 关闭函数
 */
export function createSSE(url, params, callbacks = {}) {
  const query = new URLSearchParams(params).toString()
  const fullUrl = `${url}?${query}`
  const es = new EventSource(fullUrl)

  es.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      callbacks.onMessage?.(data)
    } catch {
      callbacks.onMessage?.(e.data)
    }
  }

  es.addEventListener('intent', (e) => callbacks.onIntent?.(JSON.parse(e.data)))
  es.addEventListener('thinking', (e) => callbacks.onThinking?.(JSON.parse(e.data)))
  es.addEventListener('tool_start', (e) => callbacks.onToolStart?.(JSON.parse(e.data)))
  es.addEventListener('tool_end', (e) => callbacks.onToolEnd?.(JSON.parse(e.data)))
  es.addEventListener('answer', (e) => callbacks.onAnswer?.(JSON.parse(e.data)))
  es.addEventListener('error', (e) => callbacks.onError?.(JSON.parse(e.data)))
  es.addEventListener('done', (e) => callbacks.onDone?.(JSON.parse(e.data)))

  es.onerror = (e) => {
    callbacks.onError?.({ message: 'SSE 连接错误' })
    setTimeout(() => es.close(), 2000)
  }

  return { close: () => es.close() }
}

// ============ Agent API ============

export const agentApi = {
  // 同步对话
  chat: (params) => api.post('/agent/chat', params),

  // SSE 流式对话
  chatStream: (params, callbacks) => createSSE('/agent/chat/stream', params, callbacks),

  // 获取 Agent 列表
  getAgents: () => api.get('/agent/agents'),

  // 获取 Agent 状态
  getStatus: (sessionId) => api.get(`/agent/status/${sessionId}`)
}

// ============ RAG API ============

export const ragApi = {
  // 意图分类
  classifyIntent: (question, topN = 3, minScore = 0.35) =>
    api.post(`/api/rag/intent/classify?question=${encodeURIComponent(question)}&topN=${topN}&minScore=${minScore}`),

  // 查询改写
  rewriteQuery: (question, sessionId) =>
    api.post(`/api/rag/query/rewrite?question=${encodeURIComponent(question)}${sessionId ? `&sessionId=${sessionId}` : ''}`),

  // 规则级查询改写
  rewriteQueryRule: (question) =>
    api.post(`/api/rag/query/rewrite/rule?question=${encodeURIComponent(question)}`),

  // SSE 流式对话
  chatStream: (params, callbacks) => createSSE('/api/rag/chat/sse', params, callbacks)
}

// ============ 观测 API ============

export const observeApi = {
  // 指标
  getMetricsSnapshot: () => api.get('/api/observe/metrics/snapshot'),
  getToolMetrics: () => api.get('/api/observe/metrics/tools'),
  getSessionMetrics: (sessionId) => api.get(`/api/observe/metrics/session/${sessionId}`),
  triggerMetricsReport: () => api.post('/api/observe/metrics/report'),
  resetMetrics: () => api.post('/api/observe/metrics/reset'),

  // 追踪
  getTrace: (traceId) => api.get(`/api/observe/trace/${traceId}`),
  getSessionTraces: (sessionId) => api.get(`/api/observe/trace/session/${sessionId}`),
  getRecentTraces: (limit = 20) => api.get(`/api/observe/trace/recent?limit=${limit}`),
  getFailedTraces: () => api.get('/api/observe/trace/failed'),
  getTraceLatency: (traceId) => api.get(`/api/observe/trace/${traceId}/latency`),
  getTraceStatistics: () => api.get('/api/observe/trace/statistics'),
  cleanupTraces: (ttlHours = 24) => api.post(`/api/observe/trace/cleanup?ttlHours=${ttlHours}`)
}

// ============ 评测 API ============

export const evalApi = {
  runAll: (parallel = false) => api.post(`/api/eval/run?parallel=${parallel}`),
  runCustom: (testCases, parallel = false) => api.post(`/api/eval/run/custom?parallel=${parallel}`, testCases),
  runSmokeTest: () => api.post('/api/eval/smoke-test'),
  getLibraryStats: () => api.get('/api/eval/library/stats'),
  getLibrary: () => api.get('/api/eval/library'),
  getLibrarySimpleQA: () => api.get('/api/eval/library/simple-qa'),
  getLibraryToolCall: () => api.get('/api/eval/library/tool-call'),
  getLibraryReasoning: () => api.get('/api/eval/library/reasoning')
}

// ============ Prompt 调优 API ============

export const promptApi = {
  createVersion: (data) => api.post('/api/prompt/version', data),
  optimize: (data) => api.post('/api/prompt/optimize', data),
  generateVariants: (data) => api.post('/api/prompt/variants', data),
  batchTest: (data) => api.post('/api/prompt/batch-test', data),
  getBest: (promptId) => api.get(`/api/prompt/best/${promptId}`),
  getHistory: (promptId) => api.get(`/api/prompt/history/${promptId}`),
  getTuningHistory: (versionId) => api.get(`/api/prompt/tuning-history/${versionId}`),
  rollback: (versionId) => api.post(`/api/prompt/rollback/${versionId}`),
  publish: (versionId) => api.post(`/api/prompt/publish/${versionId}`)
}

// ============ A/B 测试 API ============

export const abtestApi = {
  create: (data) => api.post('/api/abtest/test', data),
  start: (testId) => api.post(`/api/abtest/test/${testId}/start`),
  pause: (testId) => api.post(`/api/abtest/test/${testId}/pause`),
  end: (testId, markWinner = false) => api.post(`/api/abtest/test/${testId}/end?markWinner=${markWinner}`),
  getResult: (testId) => api.get(`/api/abtest/test/${testId}/result`),
  getTest: (testId) => api.get(`/api/abtest/test/${testId}`),
  getAll: () => api.get('/api/abtest/tests'),
  getRunning: () => api.get('/api/abtest/tests/running'),
  delete: (testId) => api.delete(`/api/abtest/test/${testId}`),
  assign: (testId, userId) => api.get(`/api/abtest/test/${testId}/assign?userId=${userId}`),
  recordMetric: (testId, data) => api.post(`/api/abtest/test/${testId}/metric`, data)
}

// ============ 安全 API ============

export const securityApi = {
  validatePermission: (data) => api.post('/api/security/permission/validate', data),
  batchValidatePermission: (data) => api.post('/api/security/permission/batch-validate', data),
  checkPermission: (userId, permission) =>
    api.get(`/api/security/permission/check?userId=${userId}&permission=${permission}`),
  getUserPermissions: (userId) => api.get(`/api/security/permission/user/${userId}`),
  filterInput: (content, userId) =>
    api.post(`/api/security/content/filter-input?content=${encodeURIComponent(content)}&userId=${userId || ''}`),
  filterOutput: (content, userId) =>
    api.post(`/api/security/content/filter-output?content=${encodeURIComponent(content)}&userId=${userId || ''}`),
  getRules: () => api.get('/api/security/content/rules'),
  getViolationCount: (userId) => api.get(`/api/security/violation/count?userId=${userId}`)
}

// ============ 复盘 API ============

export const reviewApi = {
  analyze: (traceId, sessionId) => api.post('/api/review/analyze', { traceId, sessionId }),
  batchAnalyze: (traceIds) => api.post('/api/review/batch', traceIds),
  aggregate: (reports) => api.post('/api/review/aggregate', reports),
  addPattern: (data) => api.post('/api/review/pattern', data),
  getPattern: (patternId) => api.get(`/api/review/pattern/${patternId}`),
  searchPatterns: (query) => api.get(`/api/review/patterns/search?query=${encodeURIComponent(query)}`),
  getAllPatterns: () => api.get('/api/review/patterns'),
  getTopPatterns: (limit = 10) => api.get(`/api/review/patterns/top?limit=${limit}`),
  getRelatedPatterns: (context) => api.get(`/api/review/patterns/related?context=${encodeURIComponent(context)}`),
  updatePatternScore: (patternId, delta) =>
    api.put(`/api/review/pattern/${patternId}/score?delta=${delta}`),
  deletePattern: (patternId) => api.delete(`/api/review/pattern/${patternId}`),
  addLesson: (data) => api.post('/api/review/lesson', data),
  getLesson: (lessonId) => api.get(`/api/review/lesson/${lessonId}`),
  searchLessons: (query) => api.get(`/api/review/lessons/search?query=${encodeURIComponent(query)}`),
  getAllLessons: () => api.get('/api/review/lessons'),
  deleteLesson: (lessonId) => api.delete(`/api/review/lesson/${lessonId}`),
  export: () => api.get('/api/review/export'),
  getStats: () => api.get('/api/review/stats')
}

// ============ 优化 API ============

export const optimizeApi = {
  analyze: (since) => api.get(`/api/optimize/analyze${since ? `?since=${since}` : ''}`),
  analyzeTrace: (traceId) => api.get(`/api/optimize/trace/${traceId}/analyze`),
  getHealth: () => api.get('/api/optimize/health'),
  createExperiment: (data) => api.post('/api/optimize/experiment', data),
  startExperiment: (experimentId, baseline) =>
    api.post(`/api/optimize/experiment/${experimentId}/start`, baseline),
  updateMetrics: (experimentId, metrics) =>
    api.post(`/api/optimize/experiment/${experimentId}/metrics`, metrics),
  completeExperiment: (experimentId, success) =>
    api.post(`/api/optimize/experiment/${experimentId}/complete?success=${success}`),
  rollbackExperiment: (experimentId) => api.post(`/api/optimize/experiment/${experimentId}/rollback`),
  rolloutExperiment: (experimentId, percentage) =>
    api.post(`/api/optimize/experiment/${experimentId}/rollout?percentage=${percentage}`),
  getExperiment: (experimentId) => api.get(`/api/optimize/experiment/${experimentId}`),
  getAllExperiments: () => api.get('/api/optimize/experiments'),
  getRunningExperiments: () => api.get('/api/optimize/experiments/running'),
  getReport: () => api.get('/api/optimize/report')
}
