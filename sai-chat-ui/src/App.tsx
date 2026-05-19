import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Spin } from 'antd';
import { useAuthStore } from '@/stores/authStore';
import Login from '@/pages/Login';
import MainLayout from '@/pages/MainLayout';
import Chat from '@/pages/Chat';
import KnowledgeBase from '@/pages/KnowledgeBase';
import IntentTree from '@/pages/IntentTree';
import ABTest from '@/pages/Admin/ABTest';
import PromptTuning from '@/pages/Admin/PromptTuning';
import Observability from '@/pages/Admin/Observability';
import Review from '@/pages/Admin/Review';
import Experiment from '@/pages/Admin/Experiment';
import Dashboard from '@/pages/Dashboard';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function LoadingFallback() {
  return (
    <div className="flex items-center justify-center h-screen">
      <Spin size="large" tip="加载中..." />
    </div>
  );
}

export default function App() {
  return (
    <React.Suspense fallback={<LoadingFallback />}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="/chat" replace />} />
          <Route path="chat" element={<Chat />} />
          <Route path="knowledge" element={<KnowledgeBase />} />
          <Route path="intent-tree" element={<IntentTree />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="admin/abtest" element={<ABTest />} />
          <Route path="admin/prompt" element={<PromptTuning />} />
          <Route path="admin/observe" element={<Observability />} />
          <Route path="admin/review" element={<Review />} />
          <Route path="admin/experiment" element={<Experiment />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </React.Suspense>
  );
}
