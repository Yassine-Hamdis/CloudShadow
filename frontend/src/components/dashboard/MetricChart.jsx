import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Legend,
} from 'recharts'
import { format } from 'date-fns'

const toMs = (value) => {
  if (value === null || value === undefined || value === '') return NaN

  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return NaN
    return value < 1e12 ? value * 1000 : value
  }

  if (typeof value === 'string') {
    const numeric = Number(value)
    if (Number.isFinite(numeric)) {
      return numeric < 1e12 ? numeric * 1000 : numeric
    }
  }

  const parsed = new Date(value).getTime()
  return Number.isFinite(parsed) ? parsed : NaN
}

const metricTimestampMs = (metric) =>
  toMs(
    metric?.timestamp ??
    metric?.collectedAt ??
    metric?.createdAt ??
    metric?.time ??
    metric?.ts
  )

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null

  return (
    <div className="bg-[#1f2937] border border-[#374151] rounded-lg p-3 text-xs shadow-xl">
      <p className="text-[#9AA6B2] mb-2">
        {label ? format(new Date(label), 'HH:mm:ss') : ''}
      </p>
      {payload.map((entry) => (
        <div key={entry.dataKey} className="flex items-center gap-2 mb-1">
          <div
            className="w-2 h-2 rounded-full"
            style={{ backgroundColor: entry.color }}
          />
          <span className="text-[#9AA6B2]">{entry.name}:</span>
          <span className="text-[#E6EEF2] font-medium">
            {typeof entry.value === 'number' ? entry.value.toFixed(1) : entry.value}
            {entry.unit || '%'}
          </span>
        </div>
      ))}
    </div>
  )
}

/**
 * @param {object} props
 * @param {string}  props.title
 * @param {Array}   props.data        array of MetricResponse
 * @param {Array}   props.lines       [{key, name, color, unit}]
 * @param {string}  [props.yUnit]     Y axis unit suffix
 * @param {boolean} [props.loading]
 */
export default function MetricChart({
  title,
  data = [],
  lines = [],
  yUnit = '%',
  loading = false,
}) {
  const formatted = [...data]
    .sort((a, b) => metricTimestampMs(a) - metricTimestampMs(b))
    .map((d) => ({
      ...d,
      time: metricTimestampMs(d),
    }))

  return (
    <div className="bg-[#1f2937] border border-[#374151] rounded-2xl p-5 shadow-sm">
      <h3 className="text-sm font-semibold text-[#E6EEF2] mb-5 tracking-wide">{title}</h3>

      {loading ? (
        <div className="h-52 flex items-center justify-center">
          <div className="w-8 h-8 border-2 border-[#374151] border-t-[#3f51b5] rounded-full animate-spin" />
        </div>
      ) : data.length === 0 ? (
        <div className="h-52 flex items-center justify-center">
          <p className="text-sm text-[#9AA6B2]">No data available</p>
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={210}>
          <LineChart data={formatted} margin={{ top: 5, right: 5, bottom: 5, left: -10 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
            <XAxis
              dataKey="time"
              tickFormatter={(t) => format(new Date(t), 'HH:mm')}
              tick={{ fill: '#9AA6B2', fontSize: 11 }}
              axisLine={{ stroke: '#374151' }}
              tickLine={false}
              interval="preserveStartEnd"
            />
            <YAxis
              tick={{ fill: '#9AA6B2', fontSize: 11 }}
              axisLine={false}
              tickLine={false}
              tickFormatter={(v) => `${v}${yUnit}`}
              domain={[0, 100]}
            />
            <Tooltip content={<CustomTooltip />} />
            {lines.length > 1 && (
              <Legend
                wrapperStyle={{ fontSize: '12px', color: '#9AA6B2' }}
              />
            )}
            {lines.map((line) => (
              <Line
                key={line.key}
                type="monotone"
                dataKey={line.key}
                name={line.name}
                stroke={line.color}
                strokeWidth={2}
                dot={false}
                activeDot={{ r: 4, strokeWidth: 0 }}
                unit={line.unit || yUnit}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}