import { formatDistanceToNow } from 'date-fns'
import AlertBadge from './AlertBadge'
import { Cpu, HardDrive, MemoryStick, Sparkles } from 'lucide-react'

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
  const { serverName, type, severity, message, timestamp, isAiGenerated, confidenceScore, predictionWindow, recommendedAction } = alert

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
          {isAiGenerated && (
            <div className="flex items-center gap-1 px-2 py-0.5 rounded-full bg-[#6366F1]/20 border border-[#6366F1]/30">
              <Sparkles className="w-3 h-3" style={{ color: '#6366F1' }} />
              <span className="text-xs font-semibold text-[#6366F1]">AI</span>
            </div>
          )}
          <AlertBadge severity={severity} />
          <span className="text-xs text-[#9AA6B2] font-mono">
            {serverName}
          </span>
          <span className="text-xs font-medium text-[#9AA6B2]">
            · {type}
          </span>
        </div>
        <p className="text-sm text-[#E6EEF2] leading-6">{message}</p>
        
        {/* AI Fields */}
        {isAiGenerated && (
          <div className="mt-3 pt-3 border-t border-[#374151] space-y-2">
            {confidenceScore && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-[#9AA6B2]">Confidence:</span>
                <div className="flex items-center gap-2 flex-1">
                  <div className="flex-1 h-1.5 bg-[#374151] rounded-full overflow-hidden">
                    <div
                      className="h-full bg-[#6366F1]"
                      style={{ width: `${Math.min(100, confidenceScore * 100)}%` }}
                    />
                  </div>
                  <span className="text-xs font-semibold text-[#6366F1]">
                    {Math.round(confidenceScore * 100)}%
                  </span>
                </div>
              </div>
            )}
            
            {predictionWindow && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-[#9AA6B2]">Window:</span>
                <span className="text-xs font-medium text-[#E6EEF2]">{predictionWindow}</span>
              </div>
            )}
            
            {recommendedAction && (
              <div className="flex items-start gap-2">
                <span className="text-xs text-[#9AA6B2] flex-shrink-0 pt-0.5">Action:</span>
                <p className="text-xs text-[#E6EEF2] bg-[#0F172A]/50 rounded px-2 py-1.5 flex-1">
                  {recommendedAction}
                </p>
              </div>
            )}
          </div>
        )}
        
        <p className="text-xs text-[#9AA6B2] mt-2">{timeAgo}</p>
      </div>
    </div>
  )
}