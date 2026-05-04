const TIME_ZONE = 'Africa/Casablanca'

const formatterCache = new Map()

const getFormatter = (options) => {
  const key = JSON.stringify(options)
  if (!formatterCache.has(key)) {
    formatterCache.set(key, new Intl.DateTimeFormat('en-GB', options))
  }

  return formatterCache.get(key)
}

const getZonedParts = (date, timeZone) => {
  const parts = getFormatter({
    timeZone,
    hour12: false,
    hourCycle: 'h23',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  }).formatToParts(date)

  const map = Object.fromEntries(parts.map((part) => [part.type, part.value]))

  return {
    year: Number(map.year),
    month: Number(map.month),
    day: Number(map.day),
    hour: Number(map.hour),
    minute: Number(map.minute),
    second: Number(map.second),
    millisecond: date.getUTCMilliseconds(),
  }
}

const parseNaiveTimestampInTimeZone = (year, month, day, hour, minute, second, millisecond, timeZone) => {
  const targetUtcMs = Date.UTC(year, month - 1, day, hour, minute, second, millisecond)
  let utcMs = targetUtcMs

  for (let i = 0; i < 3; i += 1) {
    const parts = getZonedParts(new Date(utcMs), timeZone)
    const currentUtcMs = Date.UTC(
      parts.year,
      parts.month - 1,
      parts.day,
      parts.hour,
      parts.minute,
      parts.second,
      parts.millisecond,
    )

    const delta = targetUtcMs - currentUtcMs
    if (!delta) break
    utcMs += delta
  }

  return utcMs
}

export const parseTimestampMs = (value) => {
  if (value === null || value === undefined || value === '') return NaN

  if (value instanceof Date) {
    return value.getTime()
  }

  if (typeof value === 'number') {
    if (!Number.isFinite(value)) return NaN
    return value < 1e12 ? value * 1000 : value
  }

  if (typeof value === 'string') {
    const s = value.trim()

    const numeric = Number(s)
    if (Number.isFinite(numeric)) {
      return numeric < 1e12 ? numeric * 1000 : numeric
    }

    const m = s.match(/^(\d{4}-\d{2}-\d{2})[ T](\d{2}:\d{2}:\d{2})(\.\d+)?(Z|[+-]\d{2}:?\d{2})?$/)
    if (m) {
      const date = m[1]
      const time = m[2]
      const frac = m[3] || ''
      const tz = m[4]

      if (tz) {
        const iso = `${date}T${time}${frac}${tz}`
        const parsedIso = Date.parse(iso)
        if (Number.isFinite(parsedIso)) return parsedIso
      }

      const [year, month, day] = date.split('-').map(Number)
      const [hour, minute, second] = time.split(':').map(Number)
      const millisecond = frac ? Number((frac.slice(1) + '000').slice(0, 3)) : 0

      return parseNaiveTimestampInTimeZone(year, month, day, hour, minute, second, millisecond, TIME_ZONE)
    }

    const parsed = Date.parse(s)
    return Number.isFinite(parsed) ? parsed : NaN
  }

  const parsed = Date.parse(value)
  return Number.isFinite(parsed) ? parsed : NaN
}

export const formatTimestampInTimeZone = (value, options = {}) => {
  const date = value instanceof Date ? value : new Date(value)
  if (!Number.isFinite(date.getTime())) return ''

  return getFormatter({
    timeZone: TIME_ZONE,
    hour12: false,
    hourCycle: 'h23',
    ...options,
  }).format(date)
}
