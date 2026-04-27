export default function AlertBadge({ severity }) {
  const isCritical = severity === 'CRITICAL'

  return (
    <span
      className={`
        inline-flex items-center px-2 py-0.5 rounded-full
        text-xs font-semibold tracking-wide
        ${isCritical
          ? 'bg-[#E53935]/20 text-[#E53935] border border-[#E53935]/30'
          : 'bg-[#FFC107]/20 text-[#FFC107] border border-[#FFC107]/30'
        }
      `}
    >
      {severity}
    </span>
  )
}