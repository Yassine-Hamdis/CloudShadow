import { formatDistanceToNow } from 'date-fns'
import AlertBadge from './AlertBadge'
import { Cpu, HardDrive, MemoryStick } from 'lucide-react'

const TYPE_ICONS = {
  CPU:    <Cpu    className="w-4 h-4" />,
  Memory: <MemoryStick className="w-4 h-4" />,
  Disk:   <HardDrive  className="w-4 h-4" />,
}

const TYPE_COLORS = {
  CPU:    '#3f51b5',
  Memory: '#FFC107',
  Disk:   '#9AA6B2',
}

export default function AlertCard({ alert }) {
  const { serverName, type, severity, message, timestamp } = alert

  const timeAgo = formatDistanceToNow(new Date(timestamp), { addSuffix: true })

  return (
    <div className="
      bg-[#1f2937] border border-[#374151] rounded-2xl p-4
      flex items-start gap-4 hover:border-[#4b5563] hover:shadow-lg transition-all duration-200
    ">
      {/* Type icon */}
      <div
        className="p-2 rounded-lg mt-0.5 flex-shrink-0"
        style={{
          backgroundColor: `${TYPE_COLORS[type]}20`,
          color:           TYPE_COLORS[type],
        }}
      >
        {TYPE_ICONS[type]}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap mb-1">
          <AlertBadge severity={severity} />
          <span className="text-xs text-[#9AA6B2] font-mono">
            {serverName}
          </span>
          <span className="text-xs font-medium text-[#9AA6B2]">
            · {type}
          </span>
        </div>
        <p className="text-sm text-[#E6EEF2] leading-6">{message}</p>
        <p className="text-xs text-[#9AA6B2] mt-2">{timeAgo}</p>
      </div>
    </div>
  )
}