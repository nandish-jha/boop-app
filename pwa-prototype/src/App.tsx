import { useEffect, useMemo, useState } from 'react'
import { NavLink, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'

type Reminder = { id: string; title: string; meta: string; completed: boolean; context?: string }
type Note = { id: string; title: string; text: string }

const defaultReminders: Reminder[] = [
  { id: '1', title: 'Review architectural drafts', meta: '09:00 AM', completed: false },
  { id: '2', title: 'Synchronize database nodes', meta: '01:30 PM', completed: false },
  { id: '3', title: 'Gallery opening reception', meta: '07:00 PM • URGENT', completed: false },
]

const defaultNotes: Note[] = [
  { id: 'n1', title: 'The Brutalist Agenda: Form follows failure', text: 'Architecture should not apologize for its existence...' },
  { id: 'n2', title: 'Visual Anchors', text: 'Research on verticality and light compression.' },
  { id: 'n3', title: 'Project Zero', text: 'Review monochromatic palette constraints.' },
]

function App() {
  const location = useLocation()
  const [reminders, setReminders] = useState<Reminder[]>(() => readStore('boop-reminders', defaultReminders))
  const [notes, setNotes] = useState<Note[]>(() => readStore('boop-notes', defaultNotes))
  const fullFocus = location.pathname === '/new-note' || location.pathname === '/new-reminder'
  const showHomeFab = location.pathname === '/home'
  const incompleteReminders = useMemo(() => reminders.filter((r) => !r.completed), [reminders])
  const completedReminders = useMemo(() => reminders.filter((r) => r.completed), [reminders])

  useEffect(() => {
    window.localStorage.setItem('boop-reminders', JSON.stringify(reminders))
  }, [reminders])

  useEffect(() => {
    window.localStorage.setItem('boop-notes', JSON.stringify(notes))
  }, [notes])

  const addNote = (title: string, text: string) =>
    setNotes((prev) => [{ id: crypto.randomUUID(), title, text }, ...prev])

  const deleteNote = (id: string) => setNotes((prev) => prev.filter((n) => n.id !== id))

  const addReminder = (title: string, meta: string, context: string) =>
    setReminders((prev) => [{ id: crypto.randomUUID(), title, meta, completed: false, context }, ...prev])

  const toggleReminder = (id: string) =>
    setReminders((prev) => prev.map((r) => (r.id === id ? { ...r, completed: !r.completed } : r)))

  const deleteReminder = (id: string) => setReminders((prev) => prev.filter((r) => r.id !== id))

  return (
    <div className="app-shell">
      <div className="phone-shell">
        {!fullFocus && <TopBar />}
        <main className={fullFocus ? 'content focus' : 'content'}>
          <Routes>
            <Route path="/" element={<Navigate to="/home" replace />} />
            <Route path="/home" element={<HomeScreen reminders={incompleteReminders} notes={notes} />} />
            <Route
              path="/reminders"
              element={
                <RemindersScreen
                  reminders={incompleteReminders}
                  completedReminders={completedReminders}
                  onToggle={toggleReminder}
                  onDelete={deleteReminder}
                />
              }
            />
            <Route path="/notes" element={<NotesScreen notes={notes} onDelete={deleteNote} />} />
            <Route path="/calendar" element={<CalendarScreen reminders={incompleteReminders} />} />
            <Route path="/account" element={<AccountScreen />} />
            <Route path="/profile" element={<ProfileScreen />} />
            <Route path="/create" element={<CreateScreen />} />
            <Route path="/new-note" element={<NewNoteScreen onCreate={addNote} />} />
            <Route path="/new-reminder" element={<NewReminderScreen onCreate={addReminder} />} />
          </Routes>
        </main>
        {showHomeFab && (
          <NavLink to="/create" className="fab" aria-label="Add item">
            <span className="material-symbols-outlined">add</span>
          </NavLink>
        )}
        {!fullFocus && <BottomNav />}
      </div>
    </div>
  )
}

function readStore<T>(key: string, fallback: T): T {
  const raw = window.localStorage.getItem(key)
  if (!raw) return fallback
  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

function TopBar() {
  return (
    <header className="topbar">
      <NavLink to="/profile" className="icon-button avatar" aria-label="Profile">
        <span className="material-symbols-outlined">person</span>
      </NavLink>
      <div className="brand center">BOOP</div>
      <NavLink to="/account" className="icon-button" aria-label="Settings">
        <span className="material-symbols-outlined">settings</span>
      </NavLink>
    </header>
  )
}

function HomeScreen({ reminders, notes }: { reminders: Reminder[]; notes: Note[] }) {
  return (
    <section className="stack">
      <h1 className="hero">Good Morning.</h1>
      <p className="muted">{reminders.length} reminders pending today</p>
      <section className="card">
        <div className="row-between">
          <h3>Urgent Priority</h3>
          <span className="tiny">VIEW ALL</span>
        </div>
        {reminders.slice(0, 3).map((item) => (
          <article key={item.id} className="list-item">
            <div>
              <h4>{item.title}</h4>
              <p className="tiny">{item.meta}</p>
            </div>
          </article>
        ))}
      </section>
      <div className="row-between">
        <h2>Recent Notes</h2>
        <span className="tiny">GRID</span>
      </div>
      {notes.slice(0, 4).map((n) => (
        <article key={n.id} className="note-card">
          <h4>{n.title}</h4>
          <p className="muted">{n.text}</p>
        </article>
      ))}
    </section>
  )
}

function RemindersScreen({
  reminders,
  completedReminders,
  onToggle,
  onDelete,
}: {
  reminders: Reminder[]
  completedReminders: Reminder[]
  onToggle: (id: string) => void
  onDelete: (id: string) => void
}) {
  return (
    <section className="stack">
      <h1 className="title">Reminders</h1>
      <p className="muted">THE MONOLITHIC ARCHIVE / SYSTEM_01</p>
      <h5 className="section-label">TODAY</h5>
      {reminders.length === 0 && <p className="muted">No pending reminders.</p>}
      {reminders.map((item) => (
        <article key={item.id} className="list-item">
          <button className="checkbox btn-ghost" onClick={() => onToggle(item.id)} aria-label="Mark completed" />
          <div>
            <h4>{item.title}</h4>
            <p className="tiny">{item.meta}</p>
          </div>
          <button className="icon-button small ml-auto" aria-label="Delete reminder" onClick={() => onDelete(item.id)}>
            <span className="material-symbols-outlined">delete</span>
          </button>
        </article>
      ))}
      <h5 className="section-label">COMPLETED</h5>
      {completedReminders.length === 0 && <p className="muted">No completed reminders yet.</p>}
      {completedReminders.map((item) => (
        <article key={item.id} className="list-item dimmed">
          <button className="checkbox done btn-ghost" onClick={() => onToggle(item.id)} aria-label="Mark not completed" />
          <div>
            <p>{item.title}</p>
            <p className="tiny">{item.meta}</p>
          </div>
        </article>
      ))}
    </section>
  )
}

function NotesScreen({ notes, onDelete }: { notes: Note[]; onDelete: (id: string) => void }) {
  return (
    <section className="stack">
      <h1 className="title">NOTES</h1>
      <p className="muted">A monolithic archive of observations.</p>
      {notes.length === 0 && <p className="muted">No notes yet. Tap + and create one.</p>}
      {notes.map((n) => (
        <article key={n.id} className="note-card">
          <div className="row-between">
            <h4>{n.title}</h4>
            <button className="icon-button small" aria-label="Delete note" onClick={() => onDelete(n.id)}>
              <span className="material-symbols-outlined">delete</span>
            </button>
          </div>
          <p className="muted">{n.text}</p>
        </article>
      ))}
    </section>
  )
}

function CalendarScreen({ reminders }: { reminders: Reminder[] }) {
  return (
    <section className="stack">
      <h1 className="title">October</h1>
      <div className="calendar-grid">
        {Array.from({ length: 31 }).map((_, i) => (
          <div key={i} className={i + 1 === 12 ? 'day active' : 'day'}>{String(i + 1).padStart(2, '0')}</div>
        ))}
      </div>
      <h3>Today's Focus</h3>
      {reminders.slice(0, 2).map((r) => (
        <article key={r.id} className="list-item">
          <div className="time">{r.meta?.split(' ')[0]}</div>
          <div>
            <h4>{r.title}</h4>
            <p className="muted">Task block</p>
          </div>
        </article>
      ))}
    </section>
  )
}

function AccountScreen() {
  return (
    <section className="stack">
      <p className="section-label">ACCOUNT DASHBOARD</p>
      <h1 className="title">Alex Vanderbilt</h1>
      <section className="card">
        <h3>Calendar Integration</h3>
        <p className="muted">Sync your Google Calendar to manage schedules seamlessly within Boop.</p>
        <button className="btn-primary">SYNC GOOGLE CALENDAR</button>
      </section>
      <section className="card">
        <h3>Alert Preferences</h3>
        <Preference label="Push Notifications" on />
        <Preference label="Email Digests" />
        <Preference label="SMS Reminders" on />
      </section>
      <button className="btn-danger">SIGN OUT</button>
    </section>
  )
}

function ProfileScreen() {
  return (
    <section className="stack">
      <p className="section-label">PROFILE</p>
      <div className="profile-hero">
        <div className="profile-avatar">
          <span className="material-symbols-outlined">person</span>
        </div>
        <h1 className="title">Nandish Jha</h1>
        <p className="muted">boop.user@example.com</p>
      </div>
      <section className="card">
        <h3>Identity</h3>
        <p className="muted">Monochrome workspace owner and archive operator.</p>
      </section>
      <section className="card">
        <h3>Quick Actions</h3>
        <button className="btn-primary">EDIT PROFILE</button>
      </section>
    </section>
  )
}

function Preference({ label, on = false }: { label: string; on?: boolean }) {
  return (
    <div className="row-between pref">
      <span>{label}</span>
      <span className={on ? 'toggle on' : 'toggle'} />
    </div>
  )
}

function CreateScreen() {
  return (
    <section className="stack">
      <h1 className="title">Create New Item</h1>
      <p className="muted">Select a format to capture your thoughts and tasks.</p>
      <NavLink to="/new-note" className="card action-card">
        <h3>New Note</h3>
        <p className="muted">Architectural thoughts and long-form archive.</p>
      </NavLink>
      <NavLink to="/new-reminder" className="card action-card">
        <h3>New Reminder</h3>
        <p className="muted">Set precision alerts and temporal triggers.</p>
      </NavLink>
      <button className="btn-primary">QUICK CAPTURE VOICE</button>
    </section>
  )
}

function NewNoteScreen({ onCreate }: { onCreate: (title: string, text: string) => void }) {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [text, setText] = useState('')

  const save = () => {
    const safeTitle = title.trim() || 'Untitled note'
    const safeText = text.trim() || 'No content'
    onCreate(safeTitle, safeText)
    navigate('/notes')
  }

  return (
    <section className="stack">
      <div className="row-between">
        <NavLink to="/notes" className="icon-link" aria-label="Close editor">
          <span className="material-symbols-outlined">close</span>
          <span className="tiny">CLOSE</span>
        </NavLink>
        <button className="btn-primary small" onClick={save}>DONE</button>
      </div>
      <input className="field title-input" placeholder="Title" value={title} onChange={(e) => setTitle(e.target.value)} />
      <textarea className="field body-input" placeholder="Start your thought here..." value={text} onChange={(e) => setText(e.target.value)} />
      <div className="toolbar" role="toolbar" aria-label="Text formatting">
        <button className="icon-button small" aria-label="Bold"><span className="material-symbols-outlined">format_bold</span></button>
        <button className="icon-button small" aria-label="Italic"><span className="material-symbols-outlined">format_italic</span></button>
        <button className="icon-button small" aria-label="Bulleted list"><span className="material-symbols-outlined">format_list_bulleted</span></button>
        <div className="toolbar-sep" />
        <button className="icon-button small" aria-label="Add image"><span className="material-symbols-outlined">image</span></button>
        <button className="icon-button small" aria-label="Add link"><span className="material-symbols-outlined">link</span></button>
        <button className="icon-button small active" aria-label="Voice note"><span className="material-symbols-outlined">mic</span></button>
      </div>
    </section>
  )
}

function NewReminderScreen({ onCreate }: { onCreate: (title: string, meta: string, context: string) => void }) {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [date, setDate] = useState('')
  const [time, setTime] = useState('')
  const [context, setContext] = useState('')

  const save = () => {
    const safeTitle = title.trim() || 'Untitled reminder'
    const meta = [time || '--:--', date || 'No date'].join(' • ')
    onCreate(safeTitle, meta, context)
    navigate('/reminders')
  }

  return (
    <section className="stack">
      <div className="row-between">
        <NavLink to="/reminders" className="icon-link" aria-label="Close editor">
          <span className="material-symbols-outlined">close</span>
          <span className="tiny">CLOSE</span>
        </NavLink>
        <button className="btn-primary small" onClick={save}>SAVE</button>
      </div>
      <h1 className="title">New Reminder</h1>
      <input className="field" placeholder="What needs attention?" value={title} onChange={(e) => setTitle(e.target.value)} />
      <div className="two">
        <input className="field" placeholder="mm/dd/yyyy" value={date} onChange={(e) => setDate(e.target.value)} />
        <input className="field" placeholder="--:--" value={time} onChange={(e) => setTime(e.target.value)} />
      </div>
      <textarea className="field body-input small" placeholder="Additional context..." value={context} onChange={(e) => setContext(e.target.value)} />
      <button className="btn-primary" onClick={save}>CREATE REMINDER</button>
    </section>
  )
}

function BottomNav() {
  return (
    <nav className="bottomnav">
      <NavItem to="/home" label="HOME" icon="home" />
      <NavItem to="/reminders" label="REMINDERS" icon="notifications" />
      <NavItem to="/calendar" label="CALENDAR" icon="calendar_today" />
      <NavItem to="/notes" label="NOTES" icon="description" />
    </nav>
  )
}

function NavItem({ to, label, icon }: { to: string; label: string; icon: string }) {
  return (
    <NavLink to={to} className={({ isActive }) => (isActive ? 'navitem active' : 'navitem')}>
      <span className="material-symbols-outlined" aria-hidden="true">
        {icon}
      </span>
      <span className="sr-only">{label}</span>
    </NavLink>
  )
}

export default App
