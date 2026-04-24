import React from 'react'
import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom'
import AgentChat from './pages/AgentChat'
import RAGPipeline from './pages/RAGPipeline'
import Observe from './pages/Observe'
import Eval from './pages/Eval'
import PromptTuning from './pages/PromptTuning'
import ABTest from './pages/ABTest'
import Security from './pages/Security'
import Review from './pages/Review'
import './styles/global.css'
import './styles/chat.css'

function NavBar() {
  const location = useLocation()
  const navs = [
    { path: '/agent-chat', label: 'Agent 对话', icon: '⬡' },
    { path: '/rag-pipeline', label: 'RAG 流水线', icon: '◈' },
    { path: '/observe', label: '观测中心', icon: '◉' },
    { path: '/eval', label: '评测系统', icon: '◎' },
    { path: '/prompt', label: 'Prompt 调优', icon: '◐' },
    { path: '/abtest', label: 'A/B 测试', icon: '◑' },
    { path: '/security', label: '安全中心', icon: '◒' },
    { path: '/review', label: '复盘中心', icon: '◓' },
  ]

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <span className="brand-icon">◎</span>
        <span className="brand-text">SAI Agent</span>
      </div>
      <div className="navbar-links">
        {navs.map(nav => (
          <Link
            key={nav.path}
            to={nav.path}
            className={`nav-link ${location.pathname === nav.path ? 'active' : ''}`}
          >
            <span className="nav-icon">{nav.icon}</span>
            <span className="nav-label">{nav.label}</span>
          </Link>
        ))}
      </div>
    </nav>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <div className="app-layout">
        <NavBar />
        <main className="app-content">
          <Routes>
            <Route path="/" element={<Navigate to="/agent-chat" replace />} />
            <Route path="/agent-chat" element={<AgentChat />} />
            <Route path="/rag-pipeline" element={<RAGPipeline />} />
            <Route path="/observe" element={<Observe />} />
            <Route path="/eval" element={<Eval />} />
            <Route path="/prompt" element={<PromptTuning />} />
            <Route path="/abtest" element={<ABTest />} />
            <Route path="/security" element={<Security />} />
            <Route path="/review" element={<Review />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
