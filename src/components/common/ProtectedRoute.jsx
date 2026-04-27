import { Navigate } from 'react-router-dom'
import useAuthStore  from '../../store/authStore'

/**
 * ProtectedRoute
 * @param {object} props
 * @param {React.ReactNode} props.children
 * @param {'ADMIN'|'USER'|null} [props.requiredRole] - if null, any auth user passes
 */
export default function ProtectedRoute({ children, requiredRole = null }) {
  const { isAuthenticated, role } = useAuthStore()

  // Not logged in → go to login
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  // Wrong role → go to dashboard root
  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}