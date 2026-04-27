import { useEffect }          from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster }            from 'react-hot-toast'
import useAuthStore           from './store/authStore'
import { useWebSocket }       from './websocket/useWebSocket'

// Pages
import LoginPage              from './pages/LoginPage'
import RegisterPage           from './pages/RegisterPage'
import DashboardLayout        from './pages/dashboard/DashboardLayout'
import OverviewPage           from './pages/dashboard/OverviewPage'
import ServersPage            from './pages/dashboard/ServersPage'
import MetricsPage            from './pages/dashboard/MetricsPage'
import AlertsPage             from './pages/dashboard/AlertsPage'
import UsersPage              from './pages/dashboard/UsersPage'

// Route Guards
import ProtectedRoute         from './components/common/ProtectedRoute'

// ── WebSocket initializer (runs inside auth context) ──────────────────────────
const WebSocketInitializer = () => {
  useWebSocket()
  return null
}

export default function App() {
  const { loadFromStorage, isAuthenticated } = useAuthStore()

  // Rehydrate auth state from localStorage on first render
  useEffect(() => {
    loadFromStorage()
  }, [])

  return (
    <BrowserRouter>
      {/* WebSocket connects when authenticated */}
      <WebSocketInitializer />

      {/* Toast notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: '#1f2937',
            color:      '#E6EEF2',
            border:     '1px solid #374151',
            fontFamily: 'Inter, sans-serif',
            fontSize:   '14px',
          },
          success: {
            iconTheme: { primary: '#4CAF50', secondary: '#1f2937' },
          },
          error: {
            iconTheme: { primary: '#E53935', secondary: '#1f2937' },
          },
        }}
      />

      <Routes>
        {/* ── Public Routes ──────────────────────────────────────────── */}
        <Route
          path="/login"
          element={
            isAuthenticated
              ? <Navigate to="/dashboard" replace />
              : <LoginPage />
          }
        />
        <Route
          path="/register"
          element={
            isAuthenticated
              ? <Navigate to="/dashboard" replace />
              : <RegisterPage />
          }
        />

        {/* ── Protected Routes ───────────────────────────────────────── */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }
        >
          {/* Overview - default dashboard page */}
          <Route index element={<OverviewPage />} />

          {/* Metrics - ADMIN + USER */}
          <Route path="metrics" element={<MetricsPage />} />

          {/* Alerts - ADMIN + USER */}
          <Route path="alerts" element={<AlertsPage />} />

          {/* Servers - ADMIN only */}
          <Route
            path="servers"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <ServersPage />
              </ProtectedRoute>
            }
          />

          {/* Users - ADMIN only */}
          <Route
            path="users"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <UsersPage />
              </ProtectedRoute>
            }
          />
        </Route>

        {/* ── Fallback ───────────────────────────────────────────────── */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}