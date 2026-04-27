import { Outlet }      from 'react-router-dom'
import { useEffect }   from 'react'
import Sidebar         from '../../components/common/Sidebar'
import Header          from '../../components/common/Header'
import useAlertsStore  from '../../store/alertsStore'
import { getAlerts } from '../../api/alerts'

export default function DashboardLayout() {
  const { setAlerts } = useAlertsStore()

  // Bootstrap alerts data on layout mount
  useEffect(() => {
    const bootstrap = async () => {
      try {
        const alerts = await getAlerts()
        setAlerts(alerts)
      } catch {
        // Silently fail — non-critical
      }
    }
    bootstrap()
  }, [])

  return (
    <div className="min-h-screen flex">
      <div className="fixed inset-0 pointer-events-none bg-[radial-gradient(circle_at_top_left,rgba(63,81,181,0.08),transparent_30%),radial-gradient(circle_at_bottom_right,rgba(76,175,80,0.06),transparent_28%)]" />
      <Sidebar />

      <div className="flex-1 min-w-0 flex flex-col relative z-10 pl-3 md:pl-5 lg:pl-6">
        <Header />

        {/* Main content area */}
        <main className="min-h-[calc(100vh-5rem)]">
          <div className="mx-auto w-full max-w-[1440px] px-4 md:px-6 lg:px-8 pb-10 pt-6 space-y-6">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  )
}