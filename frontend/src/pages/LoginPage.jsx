import { useState }         from 'react'
import { Link, useNavigate } from 'react-router-dom'
import toast                 from 'react-hot-toast'
import { login }             from '../api/auth'
import useAuthStore          from '../store/authStore'
import { Eye, EyeOff, Activity } from 'lucide-react'

export default function LoginPage() {
  const navigate = useNavigate()
  const { login: storeLogin } = useAuthStore()

  const [form, setForm] = useState({ email: '', password: '' })
  const [errors, setErrors]     = useState({})
  const [loading, setLoading]   = useState(false)
  const [showPass, setShowPass] = useState(false)

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
    setErrors((prev) => ({ ...prev, [e.target.name]: '' }))
  }

  const validate = () => {
    const errs = {}
    if (!form.email)    errs.email    = 'Email is required'
    if (!form.password) errs.password = 'Password is required'
    return errs
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const errs = validate()
    if (Object.keys(errs).length) { setErrors(errs); return }

    setLoading(true)
    try {
      const data = await login(form)
      storeLogin(data)
      toast.success('Welcome back!')
      navigate('/dashboard', { replace: true })
    } catch (err) {
      const status  = err.response?.status
      const payload = err.response?.data

      if (status === 400 && payload?.validationErrors) {
        const fieldErrors = {}
        payload.validationErrors.forEach(({ field, message }) => {
          fieldErrors[field] = message
        })
        setErrors(fieldErrors)
      } else if (status === 401) {
        setErrors({ email: 'Invalid email or password' })
      } else {
        toast.error(payload?.message || 'Login failed')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(63,81,181,0.14),transparent_35%),radial-gradient(circle_at_bottom_right,rgba(76,175,80,0.10),transparent_30%)]" />
      <div className="absolute inset-0 bg-[linear-gradient(180deg,rgba(11,18,32,0.75),rgba(15,23,36,0.92))]" />

      <div className="w-full max-w-5xl relative z-10 grid lg:grid-cols-[1.05fr_0.95fr] rounded-[2rem] overflow-hidden app-panel">
        <div className="hidden lg:flex flex-col justify-between p-10 bg-[linear-gradient(180deg,rgba(63,81,181,0.25),rgba(15,23,36,0.2))] border-r border-white/5">
          <div className="flex items-center gap-3">
            <div className="p-3 rounded-xl bg-gradient-to-br from-[#3f51b5] to-[#5c6bc0] shadow-lg">
              <Activity className="w-6 h-6 text-white" />
            </div>
            <div>
              <span className="text-2xl font-bold text-[#E6EEF2] tracking-tight block">
                CloudShadow
              </span>
              <span className="text-[11px] uppercase tracking-[0.22em] text-[#9AA6B2]">Infrastructure Monitor</span>
            </div>
          </div>

          <div className="max-w-sm">
            <h1 className="text-4xl font-semibold text-[#E6EEF2] leading-tight mb-4">
              Keep your stack visible.
            </h1>
            <p className="text-sm text-[#9AA6B2] leading-7">
              Track servers, alerts, and live metrics from one clean dashboard.
            </p>
          </div>

          <div className="grid grid-cols-3 gap-3 text-xs text-[#9AA6B2]">
            <div className="rounded-2xl border border-white/8 bg-white/4 p-3">Live alerts</div>
            <div className="rounded-2xl border border-white/8 bg-white/4 p-3">Server health</div>
            <div className="rounded-2xl border border-white/8 bg-white/4 p-3">Realtime metrics</div>
          </div>
        </div>

        <div className="p-8 sm:p-10 lg:p-12">
          {/* Logo */}
          <div className="flex lg:hidden items-center justify-center gap-3 mb-10">
            <div className="p-3 rounded-xl bg-gradient-to-br from-[#3f51b5] to-[#5c6bc0] shadow-lg">
              <Activity className="w-6 h-6 text-white" />
            </div>
            <div>
              <span className="text-2xl font-bold text-[#E6EEF2] tracking-tight block">
                CloudShadow
              </span>
              <span className="text-[11px] uppercase tracking-[0.22em] text-[#9AA6B2]">Infrastructure Monitor</span>
            </div>
          </div>

          {/* Card */}
          <div className="rounded-[1.75rem] p-7 sm:p-8 app-panel-soft">
            <h1 className="text-2xl font-semibold text-[#E6EEF2] mb-2 tracking-tight">
              Welcome back
            </h1>
            <p className="text-sm text-[#9AA6B2] mb-7 leading-7">
              Monitor your infrastructure in real-time.
            </p>

            <form onSubmit={handleSubmit} className="space-y-4.5">

            {/* Email */}
            <div>
              <label className="app-label">
                Email address
              </label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="admin@company.com"
                className={`
                  app-input w-full
                  bg-[#0f1724]/50 border text-[#E6EEF2] placeholder:text-[#4b5563]
                  focus:outline-none focus:ring-2 focus:ring-[#3f51b5]/45 focus:border-[#3f51b5]
                  transition-all text-sm backdrop-blur-sm hover:bg-[#0f1724]/70
                  ${errors.email ? 'border-[#E53935]' : 'border-[#374151]/70'}
                `}
              />
              {errors.email && (
                <p className="text-xs text-[#E53935] mt-1">{errors.email}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label className="app-label">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPass ? 'text' : 'password'}
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="••••••••"
                  className={`
                    app-input w-full pr-10
                    bg-[#0f1724]/50 border text-[#E6EEF2] placeholder:text-[#4b5563]
                    focus:outline-none focus:ring-2 focus:ring-[#3f51b5]/45 focus:border-[#3f51b5]
                    transition-all text-sm backdrop-blur-sm hover:bg-[#0f1724]/70
                    ${errors.password ? 'border-[#E53935]' : 'border-[#374151]/70'}
                  `}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#9AA6B2] hover:text-[#E6EEF2]"
                >
                  {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && (
                <p className="text-xs text-[#E53935] mt-1">{errors.password}</p>
              )}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className={`
                app-button w-full font-semibold text-sm
                bg-gradient-to-r from-[#3f51b5] to-[#5c6bc0] hover:from-[#3949a3] hover:to-[#4a5bb0] text-white
                transition-all focus:outline-none focus:ring-2
                focus:ring-[#3f51b5]/50 focus:ring-offset-2 focus:ring-offset-[#1f2937]
                shadow-lg hover:shadow-[#3f51b5]/30
                ${loading ? 'opacity-70 cursor-not-allowed' : ''}
              `}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Signing in…
                </span>
              ) : (
                'Sign in'
              )}
            </button>
          </form>

            {/* Register link */}
            <p className="text-center text-sm text-[#9AA6B2] mt-6 leading-7">
            Don't have an account?{' '}
            <Link
              to="/register"
              className="text-[#3f51b5] hover:text-[#5c6bc0] font-medium transition-colors"
            >
              Create one
            </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}