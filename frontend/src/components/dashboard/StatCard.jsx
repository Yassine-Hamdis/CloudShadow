import { TrendingUp, TrendingDown, Minus } from 'lucide-react'

/**
 * @param {object} props
 * @param {string}  props.title
 * @param {string|number} props.value
 * @param {string}  props.unit      e.g. "%" or "alerts"
 * @param {React.ReactNode} props.icon
 * @param {'normal'|'warning'|'danger'|'info'} props.status
 * @param {string}  [props.subtitle]
 * @param {boolean} [props.loading]
 */
export default function StatCard({
  title,
  value,
  unit = '%',
  icon,
  status = 'normal',
  subtitle,
  loading = false,
}) {
  const statusColors = {
    normal:  { border: '#374151', accent: '#4CAF50', bg: 'rgba(76,175,80,0.08)'  },
    warning: { border: '#FFC107', accent: '#FFC107', bg: 'rgba(255,193,7,0.08)'  },
    danger:  { border: '#E53935', accent: '#E53935', bg: 'rgba(229,57,53,0.08)'  },
    info:    { border: '#3f51b5', accent: '#3f51b5', bg: 'rgba(63,81,181,0.08)'  },
  }

  const { border, accent, bg } = statusColors[status]

  return (
    <div
      className="rounded-2xl p-5 border transition-all duration-200 hover:-translate-y-0.5 hover:shadow-xl"
      style={{
        backgroundColor: '#1f2937',
        borderColor:     border,
        background:      `linear-gradient(135deg, rgba(31,41,55,0.92) 0%, ${bg} 100%)`,
      }}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-[11px] font-semibold text-[#9AA6B2] uppercase tracking-[0.2em] mb-2">
            {title}
          </p>
          {loading ? (
            <div className="h-7 w-24 bg-[#374151] rounded-lg animate-pulse mt-1" />
          ) : (
            <div className="flex items-end gap-2 leading-none">
              <span className="text-3xl font-bold text-[#E6EEF2] tracking-tight">
                {value ?? '—'}
              </span>
              {unit && (
                <span className="text-sm text-[#9AA6B2] pb-1">{unit}</span>
              )}
            </div>
          )}
          {subtitle && (
            <p className="text-xs text-[#9AA6B2] mt-2 leading-relaxed">{subtitle}</p>
          )}
        </div>

        {/* Icon */}
        {icon && (
          <div
            className="p-2.5 rounded-xl"
            style={{ backgroundColor: bg, color: accent }}
          >
            {icon}
          </div>
        )}
      </div>
    </div>
  )
}