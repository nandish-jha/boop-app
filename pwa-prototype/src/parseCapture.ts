export type CaptureType = 'TASK' | 'NOTE' | 'JOURNAL' | 'QUOTE'

export interface ParsedCapture {
  type: CaptureType
  title: string
  body: string
  dueEpochMillis: number | null
  area: string | null
  priority: 'low' | 'medium' | 'high' | null
}

export function parseCapture(raw: string): ParsedCapture {
  const text = raw.trim()
  const lower = text.toLowerCase()
  const type = detectType(lower)
  const dueEpochMillis = extractDueDate(lower)
  const area = extractArea(lower)
  const priority = extractPriority(lower)
  const cleaned = cleanText(text)
  const title = extractTitle(cleaned, type)
  return { type, title, body: cleaned, dueEpochMillis, area, priority }
}

// ---------------------------------------------------------------------------
// Type detection
// ---------------------------------------------------------------------------

const TASK_TRIGGERS = [
  'add task', 'create task', 'new task', 'schedule', 'remind me', 'reminder',
  'todo', 'to-do', 'to do', 'buy', 'pick up', 'call', 'email', 'send',
  'fix', 'check', 'review', 'submit', 'pay', 'book', 'reserve', 'order',
  'clean', 'wash', 'cook', 'prepare', 'finish', 'complete', 'update',
  'cancel', 'change', 'replace', 'install', 'set up', 'sign up', 'register',
  'apply', 'follow up', 'contact', 'meet', 'attend', 'go to',
  'make sure', "don't forget", 'need to', 'have to', 'must',
]

const JOURNAL_TRIGGERS = [
  'journal', 'diary', 'today i ', 'today was', 'this morning i', 'this evening i',
  'spent the day', 'we went to', 'we had a', 'spent time', 'i felt', 'i feel like',
  'yesterday we', 'last night we', 'memorial day', 'family dinner', 'journal entry',
]

const QUOTE_TRIGGERS = [
  'quote from', 'from the book', 'according to', 'he said', 'she said',
  'once said', 'as said by', 'the author says', '— ', 'quote:',
]

function detectType(lower: string): CaptureType {
  if (QUOTE_TRIGGERS.some(t => lower.includes(t))) return 'QUOTE'
  if (JOURNAL_TRIGGERS.some(t => lower.includes(t))) return 'JOURNAL'
  if (TASK_TRIGGERS.some(t => lower.includes(t))) return 'TASK'
  if (extractDueDate(lower) !== null && /\b(do|get|bring|take|drop|run|go)\b/.test(lower)) return 'TASK'
  return 'NOTE'
}

// ---------------------------------------------------------------------------
// Due date extraction
// ---------------------------------------------------------------------------

export function extractDueDate(lower: string): number | null {
  const now = new Date()
  const todayMidnight = new Date(now); todayMidnight.setHours(0, 0, 0, 0)

  const dayMap: Record<string, number> = {
    today: 0, tonight: 0, tomorrow: 1, 'day after tomorrow': 2,
    monday: 0, tuesday: 0, wednesday: 0, thursday: 0,
    friday: 0, saturday: 0, sunday: 0,
    'next monday': 0, 'next tuesday': 0, 'next wednesday': 0,
    'next thursday': 0, 'next friday': 0, 'next saturday': 0, 'next sunday': 0,
    'next week': 7, 'next month': 30,
  }

  let baseDate: Date = new Date()
  const dayNames = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday']

  for (const [word, offset] of Object.entries(dayMap)) {
    if (!lower.includes(word)) continue
    if (word === 'next week') {
      baseDate = new Date(todayMidnight); baseDate.setDate(baseDate.getDate() + 7); break
    }
    if (word === 'next month') {
      baseDate = new Date(todayMidnight); baseDate.setDate(baseDate.getDate() + 30); break
    }
    if (dayNames.includes(word.replace('next ', ''))) {
      const targetDay = dayNames.indexOf(word.replace('next ', ''))
      const currentDay = now.getDay()
      let diff = targetDay - currentDay
      if (word.startsWith('next ') || diff <= 0) diff += 7
      baseDate = new Date(todayMidnight); baseDate.setDate(baseDate.getDate() + diff); break
    }
    baseDate = new Date(todayMidnight); baseDate.setDate(baseDate.getDate() + offset); break
  }

  const timeMatch = lower.match(
    /\bat\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\b|\b(\d{1,2}):(\d{2})\s*(am|pm)?\b/
  )
  if (timeMatch) {
    let hours = parseInt(timeMatch[1] || timeMatch[4] || '9')
    const minutes = parseInt(timeMatch[2] || timeMatch[5] || '0')
    const meridiem = (timeMatch[3] || timeMatch[6] || '').toLowerCase()
    if (meridiem === 'pm' && hours < 12) hours += 12
    if (meridiem === 'am' && hours === 12) hours = 0
    baseDate.setHours(hours, minutes, 0, 0)
    return baseDate.getTime()
  }

  const foundDayKeyword = Object.keys(dayMap).some(k => lower.includes(k))
  if (foundDayKeyword) { baseDate.setHours(9, 0, 0, 0); return baseDate.getTime() }

  const inMatch = lower.match(/\bin\s+(\d+)\s+(minute|hour|day|week)s?\b/)
  if (inMatch) {
    const n = parseInt(inMatch[1])
    const unit = inMatch[2]
    const ms = { minute: 60000, hour: 3600000, day: 86400000, week: 604800000 }[unit] || 0
    return Date.now() + n * ms
  }

  return null
}

// ---------------------------------------------------------------------------
// Area extraction
// ---------------------------------------------------------------------------

const AREA_PATTERNS: Array<[RegExp, string]> = [
  [/\b(home|house|apartment|kitchen|bathroom|garage|garden|yard|bedroom)\b/, 'home'],
  [/\b(work|office|job|client|meeting|project|boss|colleague|team)\b/, 'work'],
  [/\b(health|doctor|gym|exercise|workout|run|jog|medication|dentist)\b/, 'health'],
  [/\b(finance|money|bill|bank|pay|rent|insurance|tax|budget|invoice)\b/, 'finance'],
  [/\b(family|kid|son|daughter|wife|husband|parent|mom|dad|birthday)\b/, 'personal'],
]

function extractArea(lower: string): string | null {
  for (const [pattern, area] of AREA_PATTERNS) {
    if (pattern.test(lower)) return area
  }
  return null
}

// ---------------------------------------------------------------------------
// Priority extraction
// ---------------------------------------------------------------------------

function extractPriority(lower: string): 'low' | 'medium' | 'high' | null {
  if (/\b(urgent|asap|immediately|right now|critical|high priority|emergency)\b/.test(lower)) return 'high'
  if (/\b(important|medium priority|soon|this week)\b/.test(lower)) return 'medium'
  if (/\b(low priority|whenever|eventually|someday|later|not urgent)\b/.test(lower)) return 'low'
  return null
}

// ---------------------------------------------------------------------------
// Clean filler words from voice input
// ---------------------------------------------------------------------------

const FILLER_PATTERNS = [
  /\bum+\b/gi, /\buh+\b/gi, /\berr+\b/gi, /\blike,?\b/gi,
  /\byou know,?\b/gi, /\bso,?\b/gi, /\bbasically,?\b/gi,
  /\bjust\b/gi, /\bkind of\b/gi, /\bsort of\b/gi,
  /\badd (a |an )?(task|reminder|note|journal|quote) (for |to )?(me )?/gi,
  /\bremind me to\b/gi, /\bcreate (a |an )?(new )?(task|reminder|note)\b/gi,
  /\bschedule (a |an )?/gi,
  /\bset (a |an )?(reminder|alert) (for )?(me )?(to )?\b/gi,
  /\bcan you\b/gi, /\bplease\b/gi,
]

function cleanText(text: string): string {
  let cleaned = text
  for (const p of FILLER_PATTERNS) cleaned = cleaned.replace(p, ' ')
  cleaned = cleaned.replace(/\s{2,}/g, ' ').trim()
  if (cleaned.length > 0) cleaned = cleaned[0].toUpperCase() + cleaned.slice(1)
  return cleaned
}

// ---------------------------------------------------------------------------
// Title extraction — first sentence or up to 70 chars
// ---------------------------------------------------------------------------

function extractTitle(cleaned: string, type: CaptureType): string {
  let title = cleaned
    .replace(/\bat\s+\d{1,2}(:\d{2})?\s*(am|pm)?\b/gi, '')
    .replace(/\b(tomorrow|today|tonight|next\s+\w+)\b/gi, '')
    .replace(/\s{2,}/g, ' ')
    .trim()

  const dot = title.search(/[.!?]/)
  if (dot > 10 && dot < 80) title = title.slice(0, dot)
  if (title.length > 70) title = title.slice(0, 67) + '…'

  if (!title) {
    if (type === 'TASK') return 'New task'
    if (type === 'JOURNAL') return 'Journal entry'
    if (type === 'QUOTE') return 'Saved quote'
    return 'New note'
  }
  return title
}
