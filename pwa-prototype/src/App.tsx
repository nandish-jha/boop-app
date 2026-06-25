import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { NavLink, Navigate, Route, Routes } from 'react-router-dom'
import { onAuthStateChanged, signInWithPopup, signOut, type User } from 'firebase/auth'
import {
  addDoc, collection, deleteDoc, doc,
  onSnapshot, orderBy, query, updateDoc,
} from 'firebase/firestore'
import { auth, db, googleProvider } from './firebase'
import VoiceCaptureScreen from './VoiceCaptureScreen'
import type { ParsedCapture } from './parseCapture'

// ─── TYPES ───────────────────────────────────────────────────────────────────

type ItemType = 'TASK' | 'NOTE' | 'JOURNAL' | 'QUOTE'
type Priority = 'low' | 'medium' | 'high'
type TimeOfDay = 'morning' | 'afternoon' | 'evening' | 'anytime'

interface Item {
  id: string
  type: ItemType
  title: string
  body: string
  area: string | null
  priority: Priority | null
  dueEpochMillis: number | null
  completed: boolean
  pinned: boolean
  createdEpochMillis: number
}

interface Routine {
  id: string
  name: string
  timeOfDay: TimeOfDay
  streak: number
  lastCheckedDate: string
  completedToday: boolean
}

interface Ctx {
  items: Item[]
  routines: Routine[]
  addCapture(p: ParsedCapture): Promise<void>
  toggleItem(id: string, completed: boolean): Promise<void>
  pinItem(id: string, pinned: boolean): Promise<void>
  deleteItem(id: string): Promise<void>
  addRoutine(name: string, timeOfDay: TimeOfDay): Promise<void>
  toggleRoutine(r: Routine): Promise<void>
  deleteRoutine(id: string): Promise<void>
}

const AppCtx = createContext<Ctx | null>(null)
const useCtx = () => useContext(AppCtx)!

// ─── UTILITIES ───────────────────────────────────────────────────────────────

const todayStr = () => new Date().toISOString().slice(0, 10)

function greeting() {
  const h = new Date().getHours()
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening'
}

function currentTOD(): TimeOfDay {
  const h = new Date().getHours()
  return h < 12 ? 'morning' : h < 17 ? 'afternoon' : 'evening'
}

const formatDue = (ms: number) =>
  new Date(ms).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })

const formatDate = (ms: number) =>
  new Date(ms).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' })

const TYPE_ICON: Record<ItemType, string> = {
  TASK: 'task_alt', NOTE: 'description', JOURNAL: 'book', QUOTE: 'format_quote',
}
const TYPE_COLOR: Record<ItemType, string> = {
  TASK: '#818cf8', NOTE: '#4ade80', JOURNAL: '#fbbf24', QUOTE: '#f472b6',
}

// ─── APP ─────────────────────────────────────────────────────────────────────

export default function App() {
  const [user, setUser] = useState<User | null>(null)
  const [authLoading, setAuthLoading] = useState(true)
  const [items, setItems] = useState<Item[]>([])
  const [routines, setRoutines] = useState<Routine[]>([])

  useEffect(() => onAuthStateChanged(auth, u => { setUser(u); setAuthLoading(false) }), [])

  useEffect(() => {
    if (!user) return
    const q = query(collection(db, 'users', user.uid, 'items'), orderBy('createdEpochMillis', 'desc'))
    return onSnapshot(q, snap => setItems(snap.docs.map(d => ({ id: d.id, ...d.data() } as Item))))
  }, [user])

  useEffect(() => {
    if (!user) return
    const today = todayStr()
    return onSnapshot(query(collection(db, 'users', user.uid, 'routines'), orderBy('name')), snap =>
      setRoutines(snap.docs.map(d => {
        const data = d.data()
        return { id: d.id, ...data, completedToday: data.lastCheckedDate === today && !!data.completedToday } as Routine
      }))
    )
  }, [user])

  if (authLoading) return <Splash />
  if (!user) return <SignInScreen />

  const itemsCol = () => collection(db, 'users', user.uid, 'items')
  const routinesCol = () => collection(db, 'users', user.uid, 'routines')
  const itemRef = (id: string) => doc(db, 'users', user.uid, 'items', id)
  const routineRef = (id: string) => doc(db, 'users', user.uid, 'routines', id)

  const ctx: Ctx = {
    items,
    routines,

    async addCapture(p) {
      await addDoc(itemsCol(), {
        type: p.type,
        title: p.title,
        body: p.body,
        area: p.area ?? null,
        priority: p.priority ?? null,
        dueEpochMillis: p.dueEpochMillis ?? null,
        completed: false,
        pinned: false,
        createdEpochMillis: Date.now(),
      })
    },

    async toggleItem(id, completed) {
      await updateDoc(itemRef(id), { completed: !completed })
    },

    async pinItem(id, pinned) {
      await updateDoc(itemRef(id), { pinned: !pinned })
    },

    async deleteItem(id) {
      await deleteDoc(itemRef(id))
    },

    async addRoutine(name, timeOfDay) {
      await addDoc(routinesCol(), { name, timeOfDay, streak: 0, lastCheckedDate: '', completedToday: false })
    },

    async toggleRoutine(r) {
      const today = todayStr()
      const yesterday = new Date(Date.now() - 86400000).toISOString().slice(0, 10)
      if (r.completedToday) {
        await updateDoc(routineRef(r.id), {
          completedToday: false,
          streak: Math.max(0, r.streak - 1),
          lastCheckedDate: yesterday,
        })
      } else {
        const newStreak = r.lastCheckedDate === yesterday ? r.streak + 1 : 1
        await updateDoc(routineRef(r.id), { completedToday: true, streak: newStreak, lastCheckedDate: today })
      }
    },

    async deleteRoutine(id) {
      await deleteDoc(routineRef(id))
    },
  }

  return (
    <AppCtx.Provider value={ctx}>
      <div className="app-shell">
        <div className="phone-shell">
          <Routes>
            <Route path="/" element={<Navigate to="/today" replace />} />
            <Route path="/today" element={<Layout user={user}><TodayScreen /></Layout>} />
            <Route path="/tasks" element={<Layout user={user}><TasksScreen /></Layout>} />
            <Route path="/library" element={<Layout user={user}><LibraryScreen /></Layout>} />
            <Route path="/habits" element={<Layout user={user}><HabitsScreen /></Layout>} />
            <Route path="/account" element={<Layout user={user}><AccountScreen onSignOut={() => signOut(auth)} /></Layout>} />
            <Route path="/capture" element={
              <main className="content focus">
                <VoiceCaptureScreen onSave={ctx.addCapture} />
              </main>
            } />
          </Routes>
        </div>
      </div>
    </AppCtx.Provider>
  )
}

// ─── SCAFFOLDING ─────────────────────────────────────────────────────────────

function Splash() {
  return (
    <div className="app-shell">
      <div className="phone-shell">
        <main className="content"><p className="muted" style={{ marginTop: 40, textAlign: 'center' }}>Loading…</p></main>
      </div>
    </div>
  )
}

function Layout({ user, children }: { user: User; children: React.ReactNode }) {
  return (
    <>
      <TopBar user={user} />
      <main className="content">{children}</main>
      <BottomNav />
    </>
  )
}

function TopBar({ user }: { user: User }) {
  return (
    <header className="topbar">
      <div className="brand">BOOP</div>
      <NavLink to="/account" className="icon-button avatar" aria-label="Account" style={{ background: 'none', border: 'none' }}>
        {user.photoURL
          ? <img src={user.photoURL} alt="" style={{ width: 28, height: 28, borderRadius: '50%', display: 'block' }} />
          : <span className="material-symbols-outlined">person</span>
        }
      </NavLink>
    </header>
  )
}

function BottomNav() {
  return (
    <nav className="bottomnav-5">
      <NavItem to="/today" icon="home" label="Today" />
      <NavItem to="/tasks" icon="task_alt" label="Tasks" />
      <NavLink to="/capture" className={({ isActive }) => isActive ? 'capture-fab active' : 'capture-fab'} aria-label="Quick Capture">
        <span className="material-symbols-outlined">mic</span>
      </NavLink>
      <NavItem to="/library" icon="library_books" label="Library" />
      <NavItem to="/habits" icon="repeat" label="Habits" />
    </nav>
  )
}

function NavItem({ to, icon, label }: { to: string; icon: string; label: string }) {
  return (
    <NavLink to={to} className={({ isActive }) => isActive ? 'navitem5 active' : 'navitem5'}>
      <span className="material-symbols-outlined" aria-hidden="true">{icon}</span>
      <span className="navitem-label">{label}</span>
    </NavLink>
  )
}

// ─── SIGN IN ─────────────────────────────────────────────────────────────────

function SignInScreen() {
  return (
    <div className="app-shell">
      <div className="phone-shell">
        <main className="content" style={{ display: 'flex', alignItems: 'center', minHeight: '100%' }}>
          <section className="stack" style={{ width: '100%' }}>
            <div style={{ textAlign: 'center', padding: '32px 0 24px' }}>
              <div style={{ width: 72, height: 72, borderRadius: '50%', background: 'rgba(129,140,248,0.12)', border: '1px solid rgba(129,140,248,0.3)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px' }}>
                <span className="material-symbols-outlined" style={{ fontSize: 36, color: '#818cf8' }}>mic</span>
              </div>
              <h1 style={{ fontFamily: 'Manrope, Inter, sans-serif', fontSize: 42, fontWeight: 800, letterSpacing: '-0.02em', margin: 0 }}>Boop</h1>
              <p className="muted" style={{ marginTop: 10 }}>Speak it. Capture it. Organise it.</p>
              <p className="muted" style={{ fontSize: 12, marginTop: 6, opacity: 0.6 }}>100% free — no API keys needed</p>
            </div>
            <button className="btn-primary" onClick={() => signInWithPopup(auth, googleProvider)}>
              CONTINUE WITH GOOGLE
            </button>
          </section>
        </main>
      </div>
    </div>
  )
}

// ─── TODAY ────────────────────────────────────────────────────────────────────

function TodayScreen() {
  const { items, routines, toggleItem, pinItem, deleteItem, toggleRoutine } = useCtx()

  const todayEnd = new Date(); todayEnd.setHours(23, 59, 59, 999)
  const pinned = items.filter(i => i.type === 'TASK' && i.pinned && !i.completed).slice(0, 3)
  const todayTasks = items.filter(i =>
    i.type === 'TASK' && !i.completed && !i.pinned &&
    i.dueEpochMillis !== null && i.dueEpochMillis <= todayEnd.getTime()
  )
  const tod = currentTOD()
  const todRoutines = routines.filter(r => r.timeOfDay === tod || r.timeOfDay === 'anytime').slice(0, 4)

  const resurfaced = useMemo(() => {
    const lib = items.filter(i => i.type === 'NOTE' || i.type === 'JOURNAL' || i.type === 'QUOTE')
    return lib.length > 0 ? lib[Math.floor(Math.random() * lib.length)] : null
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []) // intentionally static per visit — shows a different item each time you open the app

  const isEmpty = pinned.length === 0 && todayTasks.length === 0

  return (
    <section className="stack">
      <div>
        <p className="muted" style={{ fontSize: 12 }}>
          {new Date().toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' })}
        </p>
        <h1 className="hero">{greeting()}.</h1>
      </div>

      {pinned.length > 0 && (
        <div className="today-section">
          <p className="section-label" style={{ marginBottom: 2 }}>⭐ Top 3 Focus</p>
          {pinned.map(i => <TaskRow key={i.id} item={i} onToggle={toggleItem} onPin={pinItem} onDelete={deleteItem} />)}
        </div>
      )}

      <div className="today-section">
        <div className="row-between">
          <p className="section-label">Due Today</p>
          {todayTasks.length > 0 && <span className="tiny">{todayTasks.length} open</span>}
        </div>
        {isEmpty
          ? <p className="muted" style={{ fontSize: 13 }}>Nothing due. Tap the 🎤 button to add a task.</p>
          : todayTasks.slice(0, 6).map(i => <TaskRow key={i.id} item={i} onToggle={toggleItem} onPin={pinItem} onDelete={deleteItem} />)
        }
      </div>

      {todRoutines.length > 0 && (
        <div className="today-section">
          <p className="section-label" style={{ marginBottom: 2 }}>{tod} habits</p>
          {todRoutines.map(r => (
            <RoutineRow key={r.id} routine={r} onToggle={toggleRoutine} onDelete={() => {}} compact />
          ))}
        </div>
      )}

      {resurfaced && (
        <div className="today-section">
          <p className="section-label" style={{ marginBottom: 2 }}>Resurfaced</p>
          <div className="card">
            <span style={{ fontSize: 10, fontWeight: 700, letterSpacing: '0.1em', color: TYPE_COLOR[resurfaced.type] }}>
              {resurfaced.type}
            </span>
            <p style={{ marginTop: 8, fontSize: 14, lineHeight: 1.6, color: 'var(--muted)' }}>
              {resurfaced.body.length > 200 ? resurfaced.body.slice(0, 200) + '…' : resurfaced.body}
            </p>
          </div>
        </div>
      )}

      <div style={{ height: 24 }} />
    </section>
  )
}

// ─── TASKS ────────────────────────────────────────────────────────────────────

function TasksScreen() {
  const { items, toggleItem, pinItem, deleteItem } = useCtx()
  const [filter, setFilter] = useState<'open' | 'done' | 'all'>('open')
  const [areaFilter, setAreaFilter] = useState<string | null>(null)

  const areas = useMemo(() => {
    const s = new Set<string>()
    items.filter(i => i.type === 'TASK' && i.area).forEach(i => s.add(i.area!))
    return [...s]
  }, [items])

  const visible = items.filter(i => {
    if (i.type !== 'TASK') return false
    if (filter === 'open' && i.completed) return false
    if (filter === 'done' && !i.completed) return false
    if (areaFilter && i.area !== areaFilter) return false
    return true
  })

  return (
    <section className="stack">
      <h1 className="title">Tasks</h1>

      <div className="tab-row">
        {(['open', 'done', 'all'] as const).map(f => (
          <button key={f} className={`tab-btn${filter === f ? ' active' : ''}`} onClick={() => setFilter(f)}>
            {f.toUpperCase()}
          </button>
        ))}
      </div>

      {areas.length > 0 && (
        <div className="chip-row">
          <button className={`area-filter${!areaFilter ? ' active' : ''}`} onClick={() => setAreaFilter(null)}>All</button>
          {areas.map(a => (
            <button key={a} className={`area-filter${areaFilter === a ? ' active' : ''}`} onClick={() => setAreaFilter(areaFilter === a ? null : a)}>
              {a}
            </button>
          ))}
        </div>
      )}

      {visible.length === 0 && (
        <p className="muted" style={{ fontSize: 13 }}>
          {filter === 'done' ? 'No completed tasks yet.' : 'No tasks here. Tap 🎤 to add one.'}
        </p>
      )}
      {visible.map(i => <TaskRow key={i.id} item={i} onToggle={toggleItem} onPin={pinItem} onDelete={deleteItem} />)}
      <div style={{ height: 24 }} />
    </section>
  )
}

// ─── LIBRARY ─────────────────────────────────────────────────────────────────

function LibraryScreen() {
  const { items, deleteItem } = useCtx()
  const [tab, setTab] = useState<'NOTE' | 'JOURNAL' | 'QUOTE'>('NOTE')
  const visible = items.filter(i => i.type === tab)

  const hints: Record<string, string> = {
    NOTE: 'Say anything — "The sky was unusually clear today" → becomes a note.',
    JOURNAL: 'Start with "journal:" or "today I…" to capture a journal entry.',
    QUOTE: 'Say "quote from…" or "he said…" to save a quote.',
  }

  return (
    <section className="stack">
      <h1 className="title">Library</h1>

      <div className="tab-row">
        {(['NOTE', 'JOURNAL', 'QUOTE'] as const).map(t => (
          <button key={t} className={`tab-btn${tab === t ? ' active' : ''}`} onClick={() => setTab(t)}>
            <span className="material-symbols-outlined" style={{ fontSize: 13, verticalAlign: 'middle', marginRight: 3 }}>{TYPE_ICON[t]}</span>
            {t}
          </button>
        ))}
      </div>

      {visible.length === 0 && (
        <div className="card" style={{ opacity: 0.7 }}>
          <p className="muted" style={{ fontSize: 13 }}>{hints[tab]}</p>
        </div>
      )}

      {visible.map(item => (
        <article key={item.id} className="library-item">
          <div className="row-between">
            <span style={{ fontSize: 10, fontWeight: 700, letterSpacing: '0.08em', color: TYPE_COLOR[item.type] }}>
              {item.type}
            </span>
            <div style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <span className="tiny">{formatDate(item.createdEpochMillis)}</span>
              <button className="icon-button small" onClick={() => deleteItem(item.id)} aria-label="Delete">
                <span className="material-symbols-outlined">delete</span>
              </button>
            </div>
          </div>
          <h4 style={{ margin: '8px 0 4px', fontSize: 15 }}>{item.title}</h4>
          <p className="muted" style={{ fontSize: 13, lineHeight: 1.6, margin: 0 }}>{item.body}</p>
          <div style={{ display: 'flex', gap: 6, marginTop: 8, flexWrap: 'wrap' }}>
            {item.area && <span className="area-chip">{item.area}</span>}
            {item.priority && <span className={`priority-chip priority-${item.priority}`}>{item.priority}</span>}
          </div>
        </article>
      ))}

      <div style={{ height: 24 }} />
    </section>
  )
}

// ─── HABITS ──────────────────────────────────────────────────────────────────

function HabitsScreen() {
  const { routines, addRoutine, toggleRoutine, deleteRoutine } = useCtx()
  const [showAdd, setShowAdd] = useState(false)
  const [newName, setNewName] = useState('')
  const [newTime, setNewTime] = useState<TimeOfDay>('morning')

  const done = routines.filter(r => r.completedToday).length
  const total = routines.length
  const TOD_ORDER: TimeOfDay[] = ['morning', 'afternoon', 'evening', 'anytime']

  const save = async () => {
    if (!newName.trim()) return
    await addRoutine(newName.trim(), newTime)
    setNewName(''); setShowAdd(false)
  }

  return (
    <section className="stack">
      <div className="row-between">
        <h1 className="title">Habits</h1>
        {total > 0 && <span className="tiny muted">{done}/{total} today</span>}
      </div>

      {total > 0 && (
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${total > 0 ? (done / total) * 100 : 0}%` }} />
        </div>
      )}

      {TOD_ORDER.map(tod => {
        const group = routines.filter(r => r.timeOfDay === tod)
        if (!group.length) return null
        return (
          <div key={tod} className="today-section">
            <p className="section-label">{tod}</p>
            {group.map(r => <RoutineRow key={r.id} routine={r} onToggle={toggleRoutine} onDelete={deleteRoutine} />)}
          </div>
        )
      })}

      {total === 0 && !showAdd && (
        <p className="muted" style={{ fontSize: 13 }}>Track daily habits and build streaks. Add your first one below.</p>
      )}

      {showAdd ? (
        <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <h4 style={{ margin: 0 }}>New Habit</h4>
          <input
            className="field"
            style={{ borderRadius: 10, fontSize: 14 }}
            placeholder="e.g. Take vitamins, Read 10 pages, Walk outside"
            value={newName}
            onChange={e => setNewName(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && save()}
            autoFocus
          />
          <div className="tab-row">
            {TOD_ORDER.map(t => (
              <button key={t} className={`tab-btn${newTime === t ? ' active' : ''}`} style={{ fontSize: 10 }} onClick={() => setNewTime(t)}>
                {t}
              </button>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-primary" style={{ flex: 1 }} onClick={save}>SAVE</button>
            <button className="btn-ghost-outline" onClick={() => { setShowAdd(false); setNewName('') }}>CANCEL</button>
          </div>
        </div>
      ) : (
        <button className="btn-ghost-outline" style={{ display: 'flex', alignItems: 'center', gap: 6 }} onClick={() => setShowAdd(true)}>
          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>add</span>
          ADD HABIT
        </button>
      )}

      <div style={{ height: 24 }} />
    </section>
  )
}

// ─── ACCOUNT ─────────────────────────────────────────────────────────────────

function AccountScreen({ onSignOut }: { onSignOut(): void }) {
  const { items, routines } = useCtx()
  const taskCount = items.filter(i => i.type === 'TASK').length
  const noteCount = items.filter(i => i.type === 'NOTE' || i.type === 'JOURNAL' || i.type === 'QUOTE').length

  return (
    <section className="stack">
      <h1 className="title">Account</h1>
      <div className="card" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12, textAlign: 'center' }}>
        <div>
          <p style={{ fontSize: 28, fontWeight: 800, margin: 0 }}>{taskCount}</p>
          <p className="muted" style={{ fontSize: 11, margin: '4px 0 0' }}>Tasks</p>
        </div>
        <div>
          <p style={{ fontSize: 28, fontWeight: 800, margin: 0 }}>{noteCount}</p>
          <p className="muted" style={{ fontSize: 11, margin: '4px 0 0' }}>Library items</p>
        </div>
        <div>
          <p style={{ fontSize: 28, fontWeight: 800, margin: 0 }}>{routines.length}</p>
          <p className="muted" style={{ fontSize: 11, margin: '4px 0 0' }}>Habits</p>
        </div>
      </div>
      <div className="card">
        <p className="muted" style={{ fontSize: 13, lineHeight: 1.6, margin: 0 }}>
          Boop uses your own Firebase account for storage. Voice parsing is 100% free — no AI API keys required. Data stays in your Google account.
        </p>
      </div>
      <button className="btn-danger" onClick={onSignOut}>SIGN OUT</button>
    </section>
  )
}

// ─── SHARED COMPONENTS ───────────────────────────────────────────────────────

function TaskRow({ item, onToggle, onPin, onDelete }: {
  item: Item
  onToggle(id: string, completed: boolean): void
  onPin(id: string, pinned: boolean): void
  onDelete(id: string): void
}) {
  return (
    <article className={`list-item${item.completed ? ' dimmed' : ''}`}>
      <button
        className={`checkbox btn-ghost${item.completed ? ' done' : ''}`}
        onClick={() => onToggle(item.id, item.completed)}
        aria-label="Toggle complete"
      />
      <div style={{ flex: 1, minWidth: 0 }}>
        <p style={{ margin: 0, textDecoration: item.completed ? 'line-through' : 'none', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {item.title}
        </p>
        <div style={{ display: 'flex', gap: 6, marginTop: 3, flexWrap: 'wrap', alignItems: 'center' }}>
          {item.dueEpochMillis && <span className="tiny">{formatDue(item.dueEpochMillis)}</span>}
          {item.area && <span className="area-chip">{item.area}</span>}
          {item.priority && <span className={`priority-chip priority-${item.priority}`}>{item.priority}</span>}
        </div>
      </div>
      <button
        className={`icon-button small${item.pinned ? ' pinned' : ''}`}
        onClick={() => onPin(item.id, item.pinned)}
        aria-label={item.pinned ? 'Unpin' : 'Pin to top 3'}
        style={{ background: 'none', border: 'none', color: item.pinned ? '#fbbf24' : 'var(--soft)' }}
      >
        <span className="material-symbols-outlined" style={{ fontSize: 20 }}>{item.pinned ? 'star' : 'star_border'}</span>
      </button>
      <button
        className="icon-button small"
        onClick={() => onDelete(item.id)}
        aria-label="Delete"
        style={{ background: 'none', border: 'none', color: 'var(--soft)' }}
      >
        <span className="material-symbols-outlined" style={{ fontSize: 18 }}>delete</span>
      </button>
    </article>
  )
}

function RoutineRow({ routine, onToggle, onDelete, compact }: {
  routine: Routine
  onToggle(r: Routine): void
  onDelete(id: string): void
  compact?: boolean
}) {
  return (
    <article className={`list-item${routine.completedToday ? ' dimmed' : ''}`}>
      <button
        className={`checkbox btn-ghost${routine.completedToday ? ' done' : ''}`}
        onClick={() => onToggle(routine)}
        aria-label="Toggle habit"
      />
      <p style={{ flex: 1, margin: 0, textDecoration: routine.completedToday ? 'line-through' : 'none' }}>
        {routine.name}
      </p>
      {routine.streak > 0 && <span className="streak-badge">🔥 {routine.streak}</span>}
      {!compact && (
        <button
          className="icon-button small"
          onClick={() => onDelete(routine.id)}
          aria-label="Delete habit"
          style={{ background: 'none', border: 'none', color: 'var(--soft)' }}
        >
          <span className="material-symbols-outlined" style={{ fontSize: 18 }}>delete</span>
        </button>
      )}
    </article>
  )
}

