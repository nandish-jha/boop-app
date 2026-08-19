import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.14.1/firebase-app.js';
import {
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signOut,
} from 'https://www.gstatic.com/firebasejs/10.14.1/firebase-auth.js';
import {
  doc,
  getDoc,
  getFirestore,
  setDoc,
  serverTimestamp,
} from 'https://www.gstatic.com/firebasejs/10.14.1/firebase-firestore.js';
import {
  TYPE_ICONS,
  TYPE_LABELS,
  WEEKDAY_LABELS,
  typeColors,
  themeTokens,
  greetingForHour,
  fmtMoney,
  stripHtml,
  fmtDue,
} from './unified-theme.js';

const firebaseConfig = {
  apiKey: 'AIzaSyDVgNljprvdSnhj_P7xTNRsky_SQwXzwvA',
  authDomain: 'prodash-reminders.firebaseapp.com',
  projectId: 'prodash-reminders',
  storageBucket: 'prodash-reminders.firebasestorage.app',
  messagingSenderId: '948809119707',
  appId: '1:948809119707:web:spaplnadjur8eogs9sbavv4pqj9l259i',
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);
const provider = new GoogleAuthProvider();

const KEYS = ['tasks', 'notes', 'habits', 'accounts', 'ledgerEntries'];
const LS_PREFIX = 'boop_web_';
const GUEST_UID = 'local-guest';
const PLANNER_SCREENS = new Set(['reminders', 'notes', 'calendar']);

const ui = {
  theme: localStorage.getItem('boop_theme') || 'dark',
  screen: 'home',
  plannerSection: localStorage.getItem('boop_planner') || 'reminders',
  homeFilter: 'all',
  selectedDay: new Date().getDay() === 0 ? 6 : new Date().getDay() - 1,
  calendarCursor: startOfMonth(new Date()),
  walletAccount: 'all',
  sheetOpen: false,
  sheetStep: 'pick',
  createType: null,
  settingsOpen: false,
  editor: null,
  searchQuery: '',
};

const state = {
  user: null,
  guest: localStorage.getItem('boop_guest') === '1',
  syncing: false,
  lastSync: null,
  lastError: null,
  cloudMeta: null,
  data: emptyData(),
};

function sessionUid() {
  return state.user?.uid || (state.guest ? GUEST_UID : null);
}

function startOfMonth(d) {
  return new Date(d.getFullYear(), d.getMonth(), 1);
}

function emptyData() {
  return { tasks: [], notes: [], habits: [], accounts: [], ledgerEntries: [] };
}

function uuid() {
  return crypto.randomUUID();
}

function todayKey(d = new Date()) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}${m}${day}`;
}

function parseDayKeys(raw) {
  return new Set((raw || '').split(',').map((s) => s.trim()).filter((s) => s.length === 8));
}

function serializeDayKeys(set) {
  return [...set].sort().join(',');
}

function parseDayValues(raw) {
  const out = {};
  if (!raw) return out;
  raw.split(',').forEach((part) => {
    const [k, v] = part.split(':');
    if (k?.length === 8 && v != null) out[k.trim()] = Math.max(0, parseInt(v, 10) || 0);
  });
  return out;
}

function serializeDayValues(map) {
  return Object.entries(map)
    .filter(([k, v]) => k.length === 8 && v >= 0)
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([k, v]) => `${k}:${v}`)
    .join(',');
}

function habitProgress(h) {
  if (h.quantityMode) {
    const vals = parseDayValues(h.quantityDayValues);
    const target = Math.max(1, h.quantityDailyTarget || 30);
    return Object.values(vals).filter((v) => v >= target).length;
  }
  return parseDayKeys(h.dayKeys).size;
}

function syncHabitProgress(h) {
  const goal = Math.max(1, h.goal || 1);
  return { ...h, progress: Math.min(habitProgress(h), goal) };
}

function habitWeekDots(habit) {
  const keys = parseDayKeys(habit.dayKeys);
  const vals = parseDayValues(habit.quantityDayValues);
  const target = Math.max(1, habit.quantityDailyTarget || 30);
  const colors = typeColors('habit', ui.theme);
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const key = todayKey(d);
    const on = habit.quantityMode
      ? (vals[key] || 0) >= target
      : keys.has(key);
    return {
      bg: on ? colors.accent : 'transparent',
      border: `1.5px solid ${on ? colors.accent : colors.border}`,
    };
  });
}

function habitTodayDone(h) {
  const key = todayKey();
  if (h.quantityMode) {
    return (parseDayValues(h.quantityDayValues)[key] || 0) >= Math.max(1, h.quantityDailyTarget || 30);
  }
  return parseDayKeys(h.dayKeys).has(key);
}

function parseArray(raw) {
  if (!raw || raw === '[]') return [];
  try {
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
}

function loadLocal(uid) {
  const data = emptyData();
  KEYS.forEach((key) => {
    data[key] = parseArray(localStorage.getItem(`${LS_PREFIX}${uid}_${key}`));
  });
  return data;
}

function saveLocal(uid, data) {
  KEYS.forEach((key) => {
    localStorage.setItem(`${LS_PREFIX}${uid}_${key}`, JSON.stringify(data[key] || []));
  });
}

function mergeTasks(local, remote) {
  const byId = new Map();
  remote.forEach((t) => byId.set(t.id, t));
  local.forEach((t) => {
    const ex = byId.get(t.id);
    if (!ex) { byId.set(t.id, t); return; }
    if (!t.done && ex.done) byId.set(t.id, t);
    else if (t.done && !ex.done) byId.set(t.id, ex);
    else byId.set(t.id, (t.reminderAt || 0) >= (ex.reminderAt || 0) ? t : ex);
  });
  return [...byId.values()].sort((a, b) => (a.reminderAt || 0) - (b.reminderAt || 0));
}

function mergeNotes(local, remote) {
  const byId = new Map();
  remote.forEach((n) => byId.set(n.id, n));
  local.forEach((n) => {
    const ex = byId.get(n.id);
    if (!ex) { byId.set(n.id, n); return; }
    const nu = n.updatedAt || n.updatedAtMillis || 0;
    const eu = ex.updatedAt || ex.updatedAtMillis || 0;
    byId.set(n.id, nu >= eu ? n : ex);
  });
  return [...byId.values()].sort((a, b) => {
    const au = a.updatedAt || a.updatedAtMillis || 0;
    const bu = b.updatedAt || b.updatedAtMillis || 0;
    return bu - au;
  });
}

function mergeById(local, remote) {
  const byId = new Map();
  remote.forEach((x) => byId.set(x.id, x));
  local.forEach((x) => byId.set(x.id, x));
  return [...byId.values()];
}

function mergeLedger(local, remote) {
  const byId = new Map();
  remote.forEach((e) => byId.set(e.id, e));
  local.forEach((e) => {
    const ex = byId.get(e.id);
    if (!ex) { byId.set(e.id, e); return; }
    byId.set(e.id, (e.createdAt || e.createdAtMillis || 0) >= (ex.createdAt || ex.createdAtMillis || 0) ? e : ex);
  });
  return [...byId.values()].sort((a, b) => (b.createdAt || b.createdAtMillis || 0) - (a.createdAt || a.createdAtMillis || 0));
}

function mergeAll(local, remoteFields) {
  return {
    tasks: mergeTasks(local.tasks, parseArray(remoteFields.tasks)),
    notes: mergeNotes(local.notes, parseArray(remoteFields.notes)),
    habits: mergeById(local.habits, parseArray(remoteFields.habits)).map(syncHabitProgress),
    accounts: mergeById(local.accounts, parseArray(remoteFields.accounts)),
    ledgerEntries: mergeLedger(local.ledgerEntries, parseArray(remoteFields.ledgerEntries)),
  };
}

function payloadFromData(data) {
  return {
    tasks: JSON.stringify(data.tasks),
    notes: JSON.stringify(data.notes),
    habits: JSON.stringify(data.habits),
    accounts: JSON.stringify(data.accounts),
    ledgerEntries: JSON.stringify(data.ledgerEntries),
    webSyncedAt: serverTimestamp(),
  };
}

function taskToJson(t) {
  return {
    id: t.id,
    title: t.title || '',
    reminderAt: t.reminderAt || Date.now(),
    done: !!t.done,
    repeatEveryDays: t.repeatEveryDays || 0,
    linkedNoteId: t.linkedNoteId || '',
    archived: !!t.archived,
    details: t.details || '',
    subtasksJson: t.subtasksJson || '',
  };
}

function noteToJson(n) {
  const now = Date.now();
  return {
    id: n.id,
    title: n.title || '',
    body: n.body || '',
    attachmentUri: n.attachmentUri || '',
    audioUri: n.audioUri || '',
    tags: n.tags || n.tagsCsv || '',
    ocrText: n.ocrText || '',
    linkedTaskId: n.linkedTaskId || '',
    archived: !!n.archived,
    createdAt: n.createdAt || n.createdAtMillis || now,
    updatedAt: n.updatedAt || n.updatedAtMillis || now,
  };
}

function habitToJson(h) {
  return {
    id: h.id,
    title: h.title || '',
    dayPeriodCategory: h.dayPeriodCategory || 'morning',
    goal: h.goal || 7,
    progress: h.progress || 0,
    dayKeys: h.dayKeys || '',
    quantityMode: !!h.quantityMode,
    quantityUnit: h.quantityUnit || '',
    quantityDailyTarget: h.quantityDailyTarget || 30,
    quantityDayValues: h.quantityDayValues || '',
    reminderEnabled: !!h.reminderEnabled,
    reminderHour: h.reminderHour ?? 9,
    reminderMinute: h.reminderMinute ?? 0,
  };
}

function accountToJson(a) {
  return {
    id: a.id,
    name: a.name || '',
    openingBalance: a.openingBalance ?? a.starting ?? 0,
    createdAt: a.createdAt || a.createdAtMillis || Date.now(),
  };
}

function ledgerToJson(e) {
  return {
    id: e.id,
    type: e.type || 'expense',
    accountId: e.accountId || '',
    toAccountId: e.toAccountId || '',
    amount: e.amount || 0,
    title: e.title || '',
    category: e.category || '',
    subcategory: e.subcategory || '',
    note: e.note || '',
    dueAt: e.dueAt || e.dueAtMillis || 0,
    createdAt: e.createdAt || e.createdAtMillis || Date.now(),
  };
}

async function pushKey(key) {
  if (!state.user) return;
  const payload = { [key]: JSON.stringify(state.data[key]), webSyncedAt: serverTimestamp() };
  await setDoc(doc(db, 'boopUsers', state.user.uid), payload, { merge: true });
}

async function persistData(keys = KEYS) {
  const uid = sessionUid();
  if (!uid) return;
  saveLocal(uid, state.data);
  if (!state.user) {
    state.lastSync = new Date();
    render();
    return;
  }
  state.syncing = true;
  state.lastError = null;
  render();
  try {
    for (const key of keys) await pushKey(key);
    state.lastSync = new Date();
  } catch (e) {
    state.lastError = e?.message || String(e);
  } finally {
    state.syncing = false;
    render();
  }
}

function mergeDataObjects(a, b) {
  return {
    tasks: mergeTasks(a.tasks || [], b.tasks || []),
    notes: mergeNotes(a.notes || [], b.notes || []),
    habits: mergeById(a.habits || [], b.habits || []).map(syncHabitProgress),
    accounts: mergeById(a.accounts || [], b.accounts || []),
    ledgerEntries: mergeLedger(a.ledgerEntries || [], b.ledgerEntries || []),
  };
}

async function syncNow() {
  if (!state.user) {
    state.lastError = 'Sign in with Google to sync';
    render();
    return;
  }
  if (state.syncing) return;
  state.syncing = true;
  state.lastError = null;
  render();

  const uid = state.user.uid;
  const ref = doc(db, 'boopUsers', uid);

  try {
    const snap = await Promise.race([
      getDoc(ref),
      new Promise((_, reject) => setTimeout(() => reject(new Error('Download timed out (20s)')), 20000)),
    ]);

    // Merge: in-memory + local cache + cloud (same account on phone/web).
    let merged = mergeDataObjects(state.data, loadLocal(uid));
    if (snap.exists()) {
      const remote = snap.data();
      state.cloudMeta = {
        exists: true,
        webSyncedAt: remote.webSyncedAt?.toDate?.()?.toISOString?.() || null,
      };
      merged = mergeAll(merged, remote);
    } else {
      state.cloudMeta = { exists: false };
    }

    state.data = merged;
    saveLocal(uid, merged);

    const payload = payloadFromData(merged);
    const total = KEYS.reduce((n, k) => n + (payload[k]?.length || 0), 0);
    if (total > 900_000) throw new Error(`Data too large (${Math.round(total / 1024)}KB) for Firestore`);

    await Promise.race([
      setDoc(ref, payload, { merge: true }),
      new Promise((_, reject) => setTimeout(() => reject(new Error('Upload timed out (20s)')), 20000)),
    ]);

    state.lastSync = new Date();
    state.lastError = null;
  } catch (e) {
    state.lastError = e?.message || String(e);
  } finally {
    state.syncing = false;
    render();
  }
}

function exportBackup() {
  const blob = new Blob([JSON.stringify({
    version: 1,
    exportedAt: Date.now(),
    tasks: JSON.stringify(state.data.tasks),
    notes: JSON.stringify(state.data.notes),
    habits: JSON.stringify(state.data.habits),
    accounts: JSON.stringify(state.data.accounts),
    ledgerEntries: JSON.stringify(state.data.ledgerEntries),
  }, null, 2)], { type: 'application/json' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `boop-backup-${Date.now()}.json`;
  a.click();
  URL.revokeObjectURL(a.href);
}

async function importBackup(file) {
  const raw = await file.text();
  const root = JSON.parse(raw);
  state.data = {
    tasks: parseArray(root.tasks),
    notes: parseArray(root.notes),
    habits: parseArray(root.habits).map(syncHabitProgress),
    accounts: parseArray(root.accounts),
    ledgerEntries: parseArray(root.ledgerEntries),
  };
  await persistData();
}

function activeTasks() {
  return state.data.tasks.filter((t) => !t.archived && !t.done);
}

function activeNotes() {
  return state.data.notes.filter((n) => !n.archived);
}

function accountBalance(accountId) {
  const acct = state.data.accounts.find((a) => a.id === accountId);
  const starting = acct?.openingBalance ?? acct?.starting ?? 0;
  const delta = state.data.ledgerEntries
    .filter((e) => e.accountId === accountId || e.toAccountId === accountId)
    .reduce((sum, e) => {
      if (e.type === 'transfer') {
        if (e.accountId === accountId) return sum - (e.amount || 0);
        if (e.toAccountId === accountId) return sum + (e.amount || 0);
      }
      if (e.accountId === accountId) {
        if (e.type === 'income') return sum + (e.amount || 0);
        return sum - (e.amount || 0);
      }
      return sum;
    }, 0);
  return starting + delta;
}

function totalBalance() {
  if (!state.data.accounts.length) return 0;
  return state.data.accounts.reduce((sum, a) => sum + accountBalance(a.id), 0);
}

async function saveTask(task) {
  const idx = state.data.tasks.findIndex((t) => t.id === task.id);
  const normalized = taskToJson(task);
  if (idx >= 0) state.data.tasks[idx] = normalized;
  else state.data.tasks.unshift(normalized);
  state.data.tasks.sort((a, b) => (a.reminderAt || 0) - (b.reminderAt || 0));
  await persistData(['tasks']);
}

async function deleteTask(id) {
  state.data.tasks = state.data.tasks.filter((t) => t.id !== id);
  await persistData(['tasks']);
}

async function toggleTaskDone(id) {
  const t = state.data.tasks.find((x) => x.id === id);
  if (!t) return;
  await saveTask({ ...t, done: !t.done });
}

async function saveNote(note) {
  const now = Date.now();
  const normalized = noteToJson({
    ...note,
    createdAt: note.createdAt || note.createdAtMillis || now,
    updatedAt: now,
  });
  state.data.notes = state.data.notes.filter((n) => n.id !== normalized.id);
  state.data.notes.unshift(normalized);
  await persistData(['notes']);
}

async function deleteNote(id) {
  state.data.notes = state.data.notes.filter((n) => n.id !== id);
  await persistData(['notes']);
}

async function saveHabit(habit) {
  const normalized = habitToJson(syncHabitProgress(habit));
  state.data.habits = state.data.habits.filter((h) => h.id !== normalized.id);
  state.data.habits.unshift(normalized);
  await persistData(['habits']);
}

async function deleteHabit(id) {
  state.data.habits = state.data.habits.filter((h) => h.id !== id);
  await persistData(['habits']);
}

async function toggleHabitToday(id) {
  const h = state.data.habits.find((x) => x.id === id);
  if (!h || h.quantityMode) return;
  const keys = parseDayKeys(h.dayKeys);
  const key = todayKey();
  if (keys.has(key)) keys.delete(key);
  else keys.add(key);
  await saveHabit({ ...h, dayKeys: serializeDayKeys(keys) });
}

async function adjustHabitQuantity(id, delta) {
  const h = state.data.habits.find((x) => x.id === id);
  if (!h || !h.quantityMode) return;
  const key = todayKey();
  const vals = parseDayValues(h.quantityDayValues);
  vals[key] = Math.max(0, (vals[key] || 0) + delta);
  await saveHabit({ ...h, quantityDayValues: serializeDayValues(vals) });
}

function parseSubtasks(raw) {
  try {
    const arr = JSON.parse(raw || '[]');
    return Array.isArray(arr) ? arr : [];
  } catch {
    return [];
  }
}

function matchesSearch(text) {
  const q = (ui.searchQuery || '').trim().toLowerCase();
  if (!q) return true;
  return (text || '').toLowerCase().includes(q);
}

function filterCards(cards) {
  if (!ui.searchQuery.trim()) return cards;
  return cards.filter((c) => matchesSearch(`${c.title} ${c.meta || ''} ${c.body || ''} ${c.amountText || ''}`));
}

async function saveAccount(account) {
  const normalized = accountToJson(account);
  state.data.accounts = state.data.accounts.filter((a) => a.id !== normalized.id);
  state.data.accounts.unshift(normalized);
  await persistData(['accounts']);
}

async function deleteAccount(id) {
  state.data.accounts = state.data.accounts.filter((a) => a.id !== id);
  state.data.ledgerEntries = state.data.ledgerEntries.filter(
    (e) => e.accountId !== id && e.toAccountId !== id,
  );
  await persistData(['accounts', 'ledgerEntries']);
}

async function saveLedgerEntry(entry) {
  const normalized = ledgerToJson(entry);
  const idx = state.data.ledgerEntries.findIndex((e) => e.id === normalized.id);
  if (idx >= 0) state.data.ledgerEntries[idx] = normalized;
  else state.data.ledgerEntries.unshift(normalized);
  state.data.ledgerEntries.sort(
    (a, b) => (b.createdAt || b.createdAtMillis || 0) - (a.createdAt || a.createdAtMillis || 0),
  );
  await persistData(['ledgerEntries']);
}

async function deleteLedgerEntry(id) {
  state.data.ledgerEntries = state.data.ledgerEntries.filter((e) => e.id !== id);
  await persistData(['ledgerEntries']);
}

function openEditor(type, item = null) {
  ui.sheetOpen = true;
  ui.sheetStep = 'editor';
  ui.editor = { type, item: item ? { ...item } : null };
  renderSheet();
}

function openCreate(type) {
  ui.sheetOpen = true;
  ui.sheetStep = 'editor';
  const now = Date.now();
  const defaults = {
    task: { id: uuid(), title: '', reminderAt: now + 3600000, done: false, repeatEveryDays: 0, details: '', archived: false, subtasksJson: '[]' },
    note: { id: uuid(), title: '', body: '', tags: '', archived: false },
    habit: { id: uuid(), title: '', goal: 7, progress: 0, dayPeriodCategory: 'morning', dayKeys: '', quantityMode: false, quantityDailyTarget: 30, quantityUnit: '' },
    expense: { id: uuid(), type: 'expense', title: '', amount: 0, accountId: state.data.accounts[0]?.id || '', category: '', createdAt: now },
    income: { id: uuid(), type: 'income', title: '', amount: 0, accountId: state.data.accounts[0]?.id || '', category: '', createdAt: now },
    transfer: { id: uuid(), type: 'transfer', title: '', amount: 0, accountId: state.data.accounts[0]?.id || '', toAccountId: state.data.accounts[1]?.id || '', createdAt: now },
    account: { id: uuid(), name: '', openingBalance: 0, createdAt: now },
  };
  ui.editor = { type, item: defaults[type] || defaults.note };
  renderSheet();
}

function buildCardModel(type, obj, options = {}) {
  const colors = typeColors(type, ui.theme);
  const tt = themeTokens(ui.theme);
  return {
    type,
    id: obj.id,
    title: obj.title || 'Untitled',
    icon: TYPE_ICONS[type],
    typeLabel: TYPE_LABELS[type],
    bg: colors.bg,
    border: colors.border,
    accent: colors.accent,
    meta: obj.meta || '',
    body: obj.body || null,
    amountText: obj.amountText || null,
    amountColor: obj.amountColor || null,
    dots: obj.dots || null,
    linkedLabel: obj.linkedLabel || null,
    showCheckbox: !!obj.showCheckbox,
    checked: !!obj.checked,
    onToggle: obj.onToggle || (() => {}),
    onLinked: obj.onLinked || (() => {}),
    onClick: obj.onClick || (() => {}),
    extra: obj.extra || null,
    qty: obj.qty || null,
    done: !!obj.done,
    chipOverlay: tt.chipOverlay,
  };
}

function reminderCards(tasks, { done = false } = {}) {
  const notes = activeNotes();
  return tasks
    .filter((t) => !!t.done === done)
    .map((t) => {
      const linked = t.linkedNoteId ? notes.find((n) => n.id === t.linkedNoteId) : null;
      return buildCardModel('reminder', {
        id: t.id,
        title: t.title,
        meta: fmtDue(t.reminderAt),
        body: t.details ? t.details.slice(0, 120) : null,
        showCheckbox: !done,
        checked: !!t.done,
        linkedLabel: linked ? `Note · ${linked.title}` : null,
        onLinked: () => setScreen('notes'),
        onToggle: () => toggleTaskDone(t.id),
        onClick: () => openEditor('task', t),
        done: !!t.done,
        extra: parseSubtasks(t.subtasksJson).length
          ? `${parseSubtasks(t.subtasksJson).filter((s) => s.done).length}/${parseSubtasks(t.subtasksJson).length} subtasks`
          : null,
      });
    });
}

function noteCards() {
  const reminders = state.data.tasks;
  return activeNotes().map((n) => {
    const linked = n.linkedTaskId ? reminders.find((r) => r.id === n.linkedTaskId) : null;
    const body = stripHtml(n.body).slice(0, 180) || 'No content';
    return buildCardModel('note', {
      id: n.id,
      title: n.title || 'Untitled note',
      body,
      linkedLabel: linked ? `Reminder · ${linked.title}` : null,
      onLinked: () => setScreen('reminders'),
      onClick: () => openEditor('note', n),
    });
  });
}

function habitCards() {
  return state.data.habits.map((h) => {
    const todayAmt = parseDayValues(h.quantityDayValues)[todayKey()] || 0;
    return buildCardModel('habit', {
      id: h.id,
      title: h.title,
      meta: h.quantityMode
        ? `${todayAmt} / ${h.quantityDailyTarget || 30} ${h.quantityUnit || ''}`.trim()
        : `${h.progress || 0} / ${h.goal || 0}`,
      dots: habitWeekDots(h),
      showCheckbox: !h.quantityMode,
      checked: habitTodayDone(h),
      onToggle: () => toggleHabitToday(h.id),
      onClick: () => openEditor('habit', h),
      qty: h.quantityMode ? { id: h.id, value: todayAmt } : null,
    });
  });
}

function walletCards() {
  const entries = ui.walletAccount === 'all'
    ? state.data.ledgerEntries
    : state.data.ledgerEntries.filter((e) => e.accountId === ui.walletAccount || e.toAccountId === ui.walletAccount);
  return entries.slice(0, 60).map((e) => {
    let amt = e.amount || 0;
    if (e.type === 'expense') amt = -amt;
    else if (e.type === 'transfer') amt = ui.walletAccount === e.toAccountId ? amt : -amt;
    return buildCardModel('wallet', {
      id: e.id,
      title: e.title || e.type || 'Entry',
      meta: `${e.category || e.type} · ${new Date(e.createdAt || e.createdAtMillis || 0).toLocaleDateString()}`,
      amountText: fmtMoney(amt),
      amountColor: amt >= 0 ? themeTokens(ui.theme).positiveColor : themeTokens(ui.theme).negativeColor,
      onClick: () => openEditor('ledger', e),
    });
  });
}

function calendarCards() {
  const dayStart = new Date();
  dayStart.setHours(0, 0, 0, 0);
  dayStart.setDate(dayStart.getDate() - ((dayStart.getDay() + 6) % 7) + ui.selectedDay);
  const dayEnd = new Date(dayStart);
  dayEnd.setDate(dayEnd.getDate() + 1);
  return activeTasks()
    .filter((t) => t.reminderAt >= dayStart.getTime() && t.reminderAt < dayEnd.getTime())
    .map((t) => buildCardModel('calendar', {
      id: t.id,
      title: t.title,
      meta: fmtDue(t.reminderAt),
      onClick: () => openEditor('task', t),
    }));
}

function renderTintCard(card) {
  const el = document.createElement('article');
  el.className = 'tint-card';
  el.style.background = card.bg;
  el.style.border = `1px solid ${card.border}`;
  if (card.done) el.style.opacity = '0.72';
  el.addEventListener('click', () => card.onClick());

  const top = document.createElement('div');
  top.className = 'card-top';

  const chip = document.createElement('div');
  chip.className = 'type-chip';
  chip.style.background = card.chipOverlay;
  chip.innerHTML = `<span class="material-symbols-outlined" style="font-size:13px;color:${card.accent}">${card.icon}</span><span class="label" style="color:${card.accent}">${card.typeLabel}</span>`;
  top.appendChild(chip);

  if (card.showCheckbox) {
    const btn = document.createElement('button');
    btn.className = `checkbox-btn${card.checked ? ' checked' : ''}`;
    btn.style.borderColor = card.accent;
    btn.style.background = card.checked ? card.accent : 'transparent';
    btn.innerHTML = card.checked ? '<span class="material-symbols-outlined" style="font-size:14px;color:#fff">check</span>' : '';
    btn.addEventListener('click', (e) => { e.stopPropagation(); card.onToggle(); });
    top.appendChild(btn);
  }

  el.appendChild(top);

  const title = document.createElement('div');
  title.className = 'title';
  title.textContent = card.title;
  if (card.done) title.style.textDecoration = 'line-through';
  el.appendChild(title);

  if (card.meta) {
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = card.meta;
    el.appendChild(meta);
  }

  if (card.body) {
    const body = document.createElement('div');
    body.className = 'body';
    body.textContent = card.body;
    el.appendChild(body);
  }

  if (card.dots) {
    const dots = document.createElement('div');
    dots.className = 'habit-dots';
    card.dots.forEach((d) => {
      const dot = document.createElement('div');
      dot.className = 'dot';
      dot.style.background = d.bg;
      dot.style.border = d.border;
      dots.appendChild(dot);
    });
    el.appendChild(dots);
  }

  if (card.extra) {
    const extra = document.createElement('div');
    extra.className = 'meta';
    extra.textContent = card.extra;
    el.appendChild(extra);
  }

  if (card.qty) {
    const row = document.createElement('div');
    row.className = 'qty-row';
    const minus = document.createElement('button');
    minus.type = 'button';
    minus.textContent = '−';
    minus.addEventListener('click', (e) => { e.stopPropagation(); adjustHabitQuantity(card.qty.id, -1); });
    const val = document.createElement('span');
    val.textContent = String(card.qty.value);
    const plus = document.createElement('button');
    plus.type = 'button';
    plus.textContent = '+';
    plus.addEventListener('click', (e) => { e.stopPropagation(); adjustHabitQuantity(card.qty.id, 1); });
    row.append(minus, val, plus);
    el.appendChild(row);
  }

  if (card.amountText) {
    const amt = document.createElement('div');
    amt.className = 'amount';
    amt.style.color = card.amountColor;
    amt.textContent = card.amountText;
    el.appendChild(amt);
  }

  if (card.linkedLabel) {
    const link = document.createElement('button');
    link.type = 'button';
    link.style.cssText = `margin-top:auto;display:flex;align-items:center;gap:4px;background:none;border:none;padding:0;cursor:pointer;font-size:10px;color:${card.accent};text-decoration:underline;max-width:100%`;
    link.innerHTML = `<span class="material-symbols-outlined" style="font-size:12px">link</span><span style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${card.linkedLabel}</span>`;
    link.addEventListener('click', (e) => { e.stopPropagation(); card.onLinked(); });
    el.appendChild(link);
  }

  return el;
}

function renderCardGrid(cards, emptyText) {
  const grid = document.createElement('div');
  grid.className = 'card-grid';
  if (!cards.length) {
    const empty = document.createElement('div');
    empty.className = 'empty';
    empty.textContent = emptyText;
    grid.appendChild(empty);
    return grid;
  }
  cards.forEach((c) => grid.appendChild(renderTintCard(c)));
  return grid;
}

function visibleCount() {
  const w = window.innerWidth;
  if (w >= 1600) return 16;
  if (w >= 1100) return 12;
  if (w >= 720) return 9;
  return 8;
}

function renderHome() {
  const root = document.getElementById('screen-home');
  root.innerHTML = '';

  const header = document.createElement('div');
  header.innerHTML = `<div class="serif-title">${greetingForHour()}</div><div class="muted">${new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' })}</div>`;
  root.appendChild(header);

  const stats = document.createElement('div');
  stats.className = 'stats-grid';
  const dueToday = activeTasks().filter((t) => {
    const d = new Date(t.reminderAt);
    return d.toDateString() === new Date().toDateString();
  }).length;
  const habitsDone = state.data.habits.filter((h) => habitTodayDone(h)).length;
  stats.innerHTML = `
    <div class="stat-card"><div class="label">Today</div><div class="value">${dueToday}</div><div class="muted">due</div></div>
    <div class="stat-card"><div class="label">Habits</div><div class="value">${habitsDone}/${state.data.habits.length || 0}</div><div class="muted">checked in</div></div>
    <div class="stat-card"><div class="label">Balance</div><div class="value">$${totalBalance().toFixed(0)}</div><div class="muted">all accounts</div></div>`;
  root.appendChild(stats);

  const filters = document.createElement('div');
  filters.className = 'chip-row';
  [
    { key: 'all', label: 'All' },
    { key: 'note', label: 'Notes' },
    { key: 'reminder', label: 'Reminders' },
    { key: 'calendar', label: 'Calendar' },
    { key: 'habit', label: 'Habits' },
    { key: 'wallet', label: 'Wallet' },
  ].forEach((f) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `chip${ui.homeFilter === f.key ? ' active' : ''}`;
    btn.textContent = f.label;
    btn.addEventListener('click', () => { ui.homeFilter = f.key; render(); });
    filters.appendChild(btn);
  });
  root.appendChild(filters);

  let homeCards = [
    ...noteCards().slice(0, 4),
    ...reminderCards(activeTasks()).slice(0, 4),
    ...calendarCards().slice(0, 2),
    ...habitCards().slice(0, 2),
    ...walletCards().slice(0, 2),
  ];
  if (ui.homeFilter !== 'all') {
    const map = {
      note: noteCards(),
      reminder: reminderCards(activeTasks()),
      calendar: calendarCards(),
      habit: habitCards(),
      wallet: walletCards(),
    };
    homeCards = map[ui.homeFilter] || [];
  }
  root.appendChild(renderCardGrid(filterCards(homeCards).slice(0, visibleCount()), 'Nothing here yet — tap + to add something.'));
}

function renderSimpleScreen(id, title, cards, emptyText) {
  const root = document.getElementById(id);
  root.innerHTML = '';
  const h = document.createElement('div');
  h.className = 'serif-title';
  h.textContent = title;
  root.appendChild(h);
  root.appendChild(renderCardGrid(filterCards(cards), emptyText));
}

function renderReminders() {
  const root = document.getElementById('screen-reminders');
  root.innerHTML = '';
  root.appendChild(Object.assign(document.createElement('div'), { className: 'serif-title', textContent: 'Reminders' }));
  root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Pending' }));
  root.appendChild(renderCardGrid(filterCards(reminderCards(activeTasks())), 'No pending reminders.'));
  const done = filterCards(reminderCards(state.data.tasks.filter((t) => t.done && !t.archived), { done: true }));
  if (done.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Completed', style: 'margin-top:8px' }));
    root.appendChild(renderCardGrid(done, ''));
  }
  const archived = filterCards(reminderCards(state.data.tasks.filter((t) => t.archived), { done: true }));
  if (archived.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Archived', style: 'margin-top:8px' }));
    root.appendChild(renderCardGrid(archived, ''));
  }
}

function renderCalendar() {
  const root = document.getElementById('screen-calendar');
  root.innerHTML = '';
  root.appendChild(Object.assign(document.createElement('div'), { className: 'serif-title', textContent: 'Calendar' }));

  const nav = document.createElement('div');
  nav.className = 'month-nav';
  const prev = document.createElement('button');
  prev.type = 'button';
  prev.className = 'icon-btn';
  prev.innerHTML = '<span class="material-symbols-outlined">chevron_left</span>';
  prev.addEventListener('click', () => {
    ui.calendarCursor = new Date(ui.calendarCursor.getFullYear(), ui.calendarCursor.getMonth() - 1, 1);
    render();
  });
  const label = document.createElement('div');
  label.className = 'serif-title';
  label.style.fontSize = '1.1rem';
  label.textContent = ui.calendarCursor.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  const next = document.createElement('button');
  next.type = 'button';
  next.className = 'icon-btn';
  next.innerHTML = '<span class="material-symbols-outlined">chevron_right</span>';
  next.addEventListener('click', () => {
    ui.calendarCursor = new Date(ui.calendarCursor.getFullYear(), ui.calendarCursor.getMonth() + 1, 1);
    render();
  });
  nav.append(prev, label, next);
  root.appendChild(nav);

  const monthGrid = document.createElement('div');
  monthGrid.className = 'month-grid';
  WEEKDAY_LABELS.forEach((w) => {
    const h = document.createElement('div');
    h.className = 'muted';
    h.style.textAlign = 'center';
    h.textContent = w[0];
    monthGrid.appendChild(h);
  });
  const year = ui.calendarCursor.getFullYear();
  const month = ui.calendarCursor.getMonth();
  const first = new Date(year, month, 1);
  const startPad = (first.getDay() + 6) % 7;
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const selected = new Date();
  selected.setHours(0, 0, 0, 0);
  selected.setDate(selected.getDate() - ((selected.getDay() + 6) % 7) + ui.selectedDay);
  for (let i = 0; i < startPad; i++) {
    const cell = document.createElement('div');
    cell.className = 'month-day muted-day';
    monthGrid.appendChild(cell);
  }
  for (let day = 1; day <= daysInMonth; day++) {
    const d = new Date(year, month, day);
    const btn = document.createElement('button');
    btn.type = 'button';
    const mondayOffset = (d.getDay() + 6) % 7;
    const isSelected = d.toDateString() === selected.toDateString();
    const hasItem = activeTasks().some((t) => {
      const td = new Date(t.reminderAt);
      return td.toDateString() === d.toDateString();
    });
    btn.className = `month-day${isSelected ? ' active' : ''}${hasItem ? ' has-item' : ''}`;
    btn.textContent = String(day);
    btn.addEventListener('click', () => {
      ui.selectedDay = mondayOffset;
      if (d.getMonth() !== new Date().getMonth() || d.getFullYear() !== new Date().getFullYear()) {
        const today = new Date();
        const thisMonday = new Date(today);
        thisMonday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
        const clickedMonday = new Date(d);
        clickedMonday.setDate(d.getDate() - mondayOffset);
        ui.selectedDay = mondayOffset;
      }
      ui.selectedDay = mondayOffset;
      render();
    });
    monthGrid.appendChild(btn);
  }
  root.appendChild(monthGrid);

  const strip = document.createElement('div');
  strip.className = 'week-strip';
  const today = new Date();
  const monday = new Date(today);
  monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
  WEEKDAY_LABELS.forEach((labelTxt, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `week-day${ui.selectedDay === i ? ' active' : ''}`;
    btn.innerHTML = `<div class="muted">${labelTxt}</div><div>${d.getDate()}</div>`;
    btn.addEventListener('click', () => { ui.selectedDay = i; render(); });
    strip.appendChild(btn);
  });
  root.appendChild(strip);

  const cards = filterCards(calendarCards());
  if (!cards.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'muted', textContent: 'No tasks scheduled for this day.', style: 'padding:10px 2px' }));
  } else {
    root.appendChild(renderCardGrid(cards, ''));
  }
}

function renderWallet() {
  const root = document.getElementById('screen-wallet');
  root.innerHTML = '';
  root.appendChild(Object.assign(document.createElement('div'), { className: 'serif-title', textContent: 'Wallet' }));

  const chips = document.createElement('div');
  chips.className = 'chip-row';
  [{ id: 'all', name: 'All' }, ...state.data.accounts].forEach((a) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `chip${ui.walletAccount === a.id ? ' active' : ''}`;
    btn.textContent = a.name;
    btn.addEventListener('click', () => { ui.walletAccount = a.id; render(); });
    chips.appendChild(btn);
  });
  root.appendChild(chips);

  const colors = typeColors('wallet', ui.theme);
  const hero = document.createElement('div');
  hero.className = 'wallet-hero';
  hero.style.background = colors.bg;
  hero.style.borderColor = colors.border;
  const label = ui.walletAccount === 'all' ? 'Total' : (state.data.accounts.find((a) => a.id === ui.walletAccount)?.name || 'Account');
  const balance = ui.walletAccount === 'all' ? totalBalance() : accountBalance(ui.walletAccount);
  hero.innerHTML = `<div class="muted">${label}</div><div class="balance" style="color:${colors.accent}">$${balance.toFixed(2)}</div>`;
  root.appendChild(hero);

  if (state.data.accounts.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Accounts' }));
    const acctGrid = document.createElement('div');
    acctGrid.className = 'chip-row';
    state.data.accounts.forEach((a) => {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'chip';
      btn.textContent = `${a.name} · $${accountBalance(a.id).toFixed(0)}`;
      btn.addEventListener('click', () => openEditor('account', a));
      acctGrid.appendChild(btn);
    });
    root.appendChild(acctGrid);
  }

  root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Transactions', style: 'margin-top:8px' }));
  root.appendChild(renderCardGrid(filterCards(walletCards()), 'No transactions yet.'));
}

function sheetField(label, el) {
  const wrap = document.createElement('label');
  wrap.className = 'field';
  wrap.innerHTML = `<span class="field-label">${label}</span>`;
  wrap.appendChild(el);
  return wrap;
}

function renderEditorSheet() {
  const sheet = document.getElementById('sheet');
  const { type, item } = ui.editor;
  const isNew = !state.data.tasks.some((t) => t.id === item.id)
    && !state.data.notes.some((n) => n.id === item.id)
    && !state.data.habits.some((h) => h.id === item.id)
    && !state.data.accounts.some((a) => a.id === item.id)
    && !state.data.ledgerEntries.some((e) => e.id === item.id);

  const titles = {
    task: isNew ? 'New Reminder' : 'Edit Reminder',
    note: isNew ? 'New Note' : 'Edit Note',
    habit: isNew ? 'New Habit' : 'Edit Habit',
    expense: 'New Expense',
    income: 'New Income',
    transfer: 'New Transfer',
    account: isNew ? 'New Account' : 'Edit Account',
    ledger: 'Edit Transaction',
  };

  const h = document.createElement('h3');
  h.textContent = titles[type] || 'Edit';
  sheet.appendChild(h);

  const form = document.createElement('div');
  form.className = 'editor-form';

  if (type === 'task' || type === 'reminder') {
    const title = document.createElement('input');
    title.value = item.title || '';
    title.placeholder = 'Title';
    title.addEventListener('input', (e) => { item.title = e.target.value; });
    form.appendChild(sheetField('Title', title));

    const details = document.createElement('textarea');
    details.value = item.details || '';
    details.placeholder = 'Details';
    details.addEventListener('input', (e) => { item.details = e.target.value; });
    form.appendChild(sheetField('Details', details));

    const due = document.createElement('input');
    due.type = 'datetime-local';
    due.value = toDatetimeLocal(item.reminderAt);
    due.addEventListener('change', (e) => { item.reminderAt = fromDatetimeLocal(e.target.value); });
    form.appendChild(sheetField('Due', due));

    const repeat = document.createElement('select');
    [[0, 'No repeat'], [1, 'Daily'], [7, 'Weekly'], [30, 'Monthly']].forEach(([v, l]) => {
      const o = document.createElement('option');
      o.value = v;
      o.textContent = l;
      if ((item.repeatEveryDays || 0) === v) o.selected = true;
      repeat.appendChild(o);
    });
    repeat.addEventListener('change', (e) => { item.repeatEveryDays = parseInt(e.target.value, 10); });
    form.appendChild(sheetField('Repeat', repeat));

    const subtasks = parseSubtasks(item.subtasksJson);
    const subWrap = document.createElement('div');
    const subLabel = document.createElement('div');
    subLabel.className = 'field-label';
    subLabel.textContent = 'Subtasks';
    subWrap.appendChild(subLabel);
    const redrawSubs = () => {
      [...subWrap.querySelectorAll('.subtask-row')].forEach((n) => n.remove());
      subtasks.forEach((s, i) => {
        const row = document.createElement('div');
        row.className = 'subtask-row';
        const chk = document.createElement('input');
        chk.type = 'checkbox';
        chk.checked = !!s.done;
        chk.addEventListener('change', () => { s.done = chk.checked; item.subtasksJson = JSON.stringify(subtasks); });
        const inp = document.createElement('input');
        inp.type = 'text';
        inp.value = s.text || '';
        inp.placeholder = 'Subtask';
        inp.addEventListener('input', () => { s.text = inp.value; item.subtasksJson = JSON.stringify(subtasks); });
        const rm = document.createElement('button');
        rm.type = 'button';
        rm.className = 'icon-btn';
        rm.textContent = '×';
        rm.addEventListener('click', () => {
          subtasks.splice(i, 1);
          item.subtasksJson = JSON.stringify(subtasks);
          redrawSubs();
        });
        row.append(chk, inp, rm);
        subWrap.appendChild(row);
      });
    };
    redrawSubs();
    const addSub = document.createElement('button');
    addSub.type = 'button';
    addSub.className = 'secondary-btn';
    addSub.textContent = 'Add subtask';
    addSub.addEventListener('click', () => {
      subtasks.push({ id: uuid(), text: '', done: false });
      item.subtasksJson = JSON.stringify(subtasks);
      redrawSubs();
    });
    form.appendChild(subWrap);
    form.appendChild(addSub);

    if (!isNew) {
      const archiveBtn = document.createElement('button');
      archiveBtn.type = 'button';
      archiveBtn.className = 'secondary-btn';
      archiveBtn.textContent = item.archived ? 'Unarchive' : 'Archive';
      archiveBtn.addEventListener('click', async () => {
        item.archived = !item.archived;
        await saveTask(item);
        ui.sheetOpen = false;
        render();
      });
      form.appendChild(archiveBtn);
    }
  }

  if (type === 'note') {
    const title = document.createElement('input');
    title.value = item.title || '';
    title.placeholder = 'Title';
    title.addEventListener('input', (e) => { item.title = e.target.value; });
    form.appendChild(sheetField('Title', title));

    const body = document.createElement('textarea');
    body.value = stripHtml(item.body || '');
    body.placeholder = 'Write something…';
    body.rows = 6;
    body.addEventListener('input', (e) => { item.body = e.target.value; });
    form.appendChild(sheetField('Body', body));

    const tags = document.createElement('input');
    tags.value = item.tags || item.tagsCsv || '';
    tags.placeholder = 'tags, comma, separated';
    tags.addEventListener('input', (e) => { item.tags = e.target.value; });
    form.appendChild(sheetField('Tags', tags));
  }

  if (type === 'habit') {
    const title = document.createElement('input');
    title.value = item.title || '';
    title.placeholder = 'Habit name';
    title.addEventListener('input', (e) => { item.title = e.target.value; });
    form.appendChild(sheetField('Title', title));

    const goal = document.createElement('input');
    goal.type = 'number';
    goal.min = '1';
    goal.value = item.goal || 7;
    goal.addEventListener('input', (e) => { item.goal = parseInt(e.target.value, 10) || 7; });
    form.appendChild(sheetField('Weekly goal (days)', goal));

    const period = document.createElement('select');
    ['morning', 'afternoon', 'evening', 'night'].forEach((p) => {
      const o = document.createElement('option');
      o.value = p;
      o.textContent = p.charAt(0).toUpperCase() + p.slice(1);
      if ((item.dayPeriodCategory || 'morning') === p) o.selected = true;
      period.appendChild(o);
    });
    period.addEventListener('change', (e) => { item.dayPeriodCategory = e.target.value; });
    form.appendChild(sheetField('Time of day', period));

    const qtyToggle = document.createElement('select');
    [['0', 'Check-in habit'], ['1', 'Quantity habit']].forEach(([v, l]) => {
      const o = document.createElement('option');
      o.value = v;
      o.textContent = l;
      if (!!item.quantityMode === (v === '1')) o.selected = true;
      qtyToggle.appendChild(o);
    });
    qtyToggle.addEventListener('change', (e) => { item.quantityMode = e.target.value === '1'; renderSheet(); });
    form.appendChild(sheetField('Type', qtyToggle));

    if (item.quantityMode) {
      const unit = document.createElement('input');
      unit.value = item.quantityUnit || '';
      unit.placeholder = 'minutes, mL, pages…';
      unit.addEventListener('input', (e) => { item.quantityUnit = e.target.value; });
      form.appendChild(sheetField('Unit', unit));
      const target = document.createElement('input');
      target.type = 'number';
      target.min = '1';
      target.value = item.quantityDailyTarget || 30;
      target.addEventListener('input', (e) => { item.quantityDailyTarget = parseInt(e.target.value, 10) || 30; });
      form.appendChild(sheetField('Daily target', target));
    }

    if (!isNew && !item.quantityMode) {
      const checkBtn = document.createElement('button');
      checkBtn.type = 'button';
      checkBtn.className = 'secondary-btn';
      checkBtn.textContent = habitTodayDone(item) ? 'Uncheck today' : 'Check in today';
      checkBtn.addEventListener('click', async () => {
        await toggleHabitToday(item.id);
        ui.sheetOpen = false;
        render();
      });
      form.appendChild(checkBtn);
    }
  }

  if (type === 'account') {
    const name = document.createElement('input');
    name.value = item.name || '';
    name.placeholder = 'Account name';
    name.addEventListener('input', (e) => { item.name = e.target.value; });
    form.appendChild(sheetField('Name', name));

    const bal = document.createElement('input');
    bal.type = 'number';
    bal.step = '0.01';
    bal.value = item.openingBalance ?? item.starting ?? 0;
    bal.addEventListener('input', (e) => { item.openingBalance = parseFloat(e.target.value) || 0; });
    form.appendChild(sheetField('Opening balance', bal));
  }

  if (type === 'expense' || type === 'income' || type === 'transfer' || type === 'ledger') {
    const entry = type === 'ledger' ? item : { ...item, type: type === 'ledger' ? item.type : type };

    const title = document.createElement('input');
    title.value = entry.title || '';
    title.placeholder = 'Title';
    title.addEventListener('input', (e) => { entry.title = e.target.value; });
    form.appendChild(sheetField('Title', title));

    const amount = document.createElement('input');
    amount.type = 'number';
    amount.step = '0.01';
    amount.min = '0';
    amount.value = entry.amount || 0;
    amount.addEventListener('input', (e) => { entry.amount = Math.abs(parseFloat(e.target.value) || 0); });
    form.appendChild(sheetField('Amount', amount));

    const acct = document.createElement('select');
    state.data.accounts.forEach((a) => {
      const o = document.createElement('option');
      o.value = a.id;
      o.textContent = a.name;
      if (entry.accountId === a.id) o.selected = true;
      acct.appendChild(o);
    });
    acct.addEventListener('change', (e) => { entry.accountId = e.target.value; });
    form.appendChild(sheetField(entry.type === 'transfer' ? 'From account' : 'Account', acct));

    if (entry.type === 'transfer') {
      const toAcct = document.createElement('select');
      state.data.accounts.forEach((a) => {
        const o = document.createElement('option');
        o.value = a.id;
        o.textContent = a.name;
        if (entry.toAccountId === a.id) o.selected = true;
        toAcct.appendChild(o);
      });
      toAcct.addEventListener('change', (e) => { entry.toAccountId = e.target.value; });
      form.appendChild(sheetField('To account', toAcct));
    }

    const cat = document.createElement('input');
    cat.value = entry.category || '';
    cat.placeholder = 'Category';
    cat.addEventListener('input', (e) => { entry.category = e.target.value; });
    form.appendChild(sheetField('Category', cat));

    Object.assign(item, entry);
  }

  sheet.appendChild(form);

  const actions = document.createElement('div');
  actions.className = 'sheet-actions';

  if (!isNew && type !== 'expense' && type !== 'income' && type !== 'transfer') {
    const del = document.createElement('button');
    del.type = 'button';
    del.className = 'danger-btn';
    del.textContent = 'Delete';
    del.addEventListener('click', async () => {
      if (!confirm('Delete this item?')) return;
      if (type === 'task') await deleteTask(item.id);
      else if (type === 'note') await deleteNote(item.id);
      else if (type === 'habit') await deleteHabit(item.id);
      else if (type === 'account') await deleteAccount(item.id);
      else if (type === 'ledger') await deleteLedgerEntry(item.id);
      ui.sheetOpen = false;
      render();
    });
    actions.appendChild(del);
  }

  const save = document.createElement('button');
  save.type = 'button';
  save.className = 'save-btn';
  save.textContent = state.syncing ? 'Saving…' : 'Save';
  save.disabled = state.syncing;
  save.addEventListener('click', async () => {
    if (type === 'task') await saveTask(item);
    else if (type === 'note') await saveNote(item);
    else if (type === 'habit') await saveHabit(item);
    else if (type === 'account') await saveAccount(item);
    else if (type === 'expense' || type === 'income' || type === 'transfer' || type === 'ledger') {
      if (!item.accountId && state.data.accounts.length) item.accountId = state.data.accounts[0].id;
      if (!item.id) item.id = uuid();
      await saveLedgerEntry(item);
    }
    ui.sheetOpen = false;
    render();
  });
  actions.appendChild(save);
  sheet.appendChild(actions);
}

function toDatetimeLocal(ms) {
  const d = new Date(ms || Date.now());
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function fromDatetimeLocal(val) {
  return val ? new Date(val).getTime() : Date.now();
}

function renderSheet() {
  const overlay = document.getElementById('sheet-overlay');
  const sheet = document.getElementById('sheet');
  if (!ui.sheetOpen) {
    overlay.classList.add('hidden');
    sheet.classList.add('hidden');
    sheet.innerHTML = '';
    return;
  }
  overlay.classList.remove('hidden');
  sheet.classList.remove('hidden');
  sheet.innerHTML = '';

  const close = document.createElement('button');
  close.type = 'button';
  close.className = 'sheet-close';
  close.innerHTML = '<span class="material-symbols-outlined">close</span>';
  close.addEventListener('click', () => { ui.sheetOpen = false; render(); });
  sheet.appendChild(close);

  if (ui.sheetStep === 'pick') {
    const h = document.createElement('h3');
    h.textContent = 'Add to Boop';
    sheet.appendChild(h);
    const grid = document.createElement('div');
    grid.className = 'type-pick';
    [
      { key: 'note', label: 'Note' },
      { key: 'task', label: 'Reminder' },
      { key: 'habit', label: 'Habit' },
      { key: 'expense', label: 'Expense' },
      { key: 'income', label: 'Income' },
      { key: 'transfer', label: 'Transfer' },
      { key: 'account', label: 'Account' },
    ].forEach((t) => {
      const typeKey = t.key === 'task' ? 'reminder' : (['expense', 'income', 'transfer', 'account'].includes(t.key) ? 'wallet' : t.key);
      const colors = typeColors(typeKey, ui.theme);
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.style.background = colors.bg;
      btn.style.border = `1px solid ${colors.border}`;
      btn.innerHTML = `<span class="material-symbols-outlined" style="color:${colors.accent}">${TYPE_ICONS[typeKey] || 'add'}</span><span>${t.label}</span>`;
      btn.addEventListener('click', () => openCreate(t.key));
      grid.appendChild(btn);
    });
    sheet.appendChild(grid);
    return;
  }

  if (ui.sheetStep === 'editor') {
    renderEditorSheet();
  }
}

function renderSettings() {
  const panel = document.getElementById('settings-panel');
  if (!ui.settingsOpen) {
    panel.classList.add('hidden');
    panel.innerHTML = '';
    return;
  }
  panel.classList.remove('hidden');
  panel.innerHTML = '';

  const h = document.createElement('h3');
  h.textContent = 'Settings';
  panel.appendChild(h);

  const uid = state.user?.uid || '';
  const info = document.createElement('p');
  info.className = 'muted settings-info';
  info.innerHTML = state.user
    ? `Signed in as <strong>${state.user.email || 'Google user'}</strong><br>Account ID: ${uid.slice(0, 12)}…`
    : 'Using this device only. Sign in with Google to sync with your phone.';
  panel.appendChild(info);

  const syncNote = document.createElement('p');
  syncNote.className = 'muted settings-info';
  syncNote.textContent = state.user
    ? 'To sync with your phone: open Boop → Settings → Link Google account, then sign in here with the same Google account.'
    : 'Local data stays in this browser until you sign in and sync.';
  panel.appendChild(syncNote);

  if (!state.user) {
    const signIn = document.createElement('button');
    signIn.type = 'button';
    signIn.className = 'save-btn';
    signIn.textContent = 'Sign in with Google';
    signIn.addEventListener('click', () => signInWithPopup(auth, provider).catch((e) => alert(e?.message || 'Sign-in failed')));
    panel.appendChild(signIn);
  }

  const btns = [
    { label: 'Sync now', action: () => syncNow() },
    { label: 'Export backup', action: () => exportBackup() },
  ];
  if (state.user) btns.push({ label: 'Sign out', action: () => signOut(auth) });
  else btns.push({
    label: 'Clear local data',
    action: () => {
      if (!confirm('Clear data on this device?')) return;
      state.data = emptyData();
      persistData();
      ui.settingsOpen = false;
    },
  });

  btns.forEach(({ label, action }) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'secondary-btn full';
    btn.textContent = label;
    btn.addEventListener('click', action);
    panel.appendChild(btn);
  });

  const importLabel = document.createElement('label');
  importLabel.className = 'secondary-btn full import-label';
  importLabel.textContent = 'Import backup';
  const importInput = document.createElement('input');
  importInput.type = 'file';
  importInput.accept = 'application/json,.json';
  importInput.addEventListener('change', async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      await importBackup(file);
      ui.settingsOpen = false;
      render();
    } catch (err) {
      alert(err?.message || 'Import failed');
    }
  });
  importLabel.appendChild(importInput);
  panel.appendChild(importLabel);
}

function setScreen(screen) {
  if (screen === 'planner') screen = ui.plannerSection || 'reminders';
  ui.screen = screen;
  if (PLANNER_SCREENS.has(screen)) {
    ui.plannerSection = screen;
    localStorage.setItem('boop_planner', screen);
  }
  const mobileTab = PLANNER_SCREENS.has(screen) ? 'planner' : screen;
  document.querySelectorAll('.bottom-nav .nav-btn').forEach((b) => {
    b.classList.toggle('active', b.dataset.screen === mobileTab);
  });
  document.querySelectorAll('.side-nav .nav-btn').forEach((b) => {
    b.classList.toggle('active', b.dataset.screen === screen);
  });
  document.querySelectorAll('.screen').forEach((s) => {
    s.classList.toggle('active', s.id === `screen-${screen}`);
  });
  const switcher = document.getElementById('planner-switcher');
  const showSwitcher = PLANNER_SCREENS.has(screen) && window.matchMedia('(max-width: 959px)').matches;
  switcher.classList.toggle('hidden', !showSwitcher);
  switcher.querySelectorAll('[data-planner]').forEach((c) => {
    c.classList.toggle('active', c.dataset.planner === screen);
  });
  renderScreens();
}

function renderContent() {
  renderHome();
  renderSimpleScreen('screen-notes', 'Notes', noteCards(), 'No notes yet.');
  renderReminders();
  renderCalendar();
  renderSimpleScreen('screen-habits', 'Habits', habitCards(), 'No habits yet.');
  renderWallet();
}

function renderScreens() {
  renderContent();
  renderSheet();
  renderSettings();
}

function applyTheme() {
  document.body.classList.toggle('light', ui.theme === 'light');
  const icon = document.getElementById('theme-icon');
  if (icon) icon.textContent = ui.theme === 'dark' ? 'light_mode' : 'dark_mode';
  localStorage.setItem('boop_theme', ui.theme);
  const meta = document.querySelector('meta[name="theme-color"]');
  if (meta) meta.setAttribute('content', ui.theme === 'light' ? '#fbf7f1' : '#141210');
}

function enterSession(uid) {
  state.data = loadLocal(uid);
  render();
}

function render() {
  const signedIn = !!sessionUid();
  document.getElementById('login-gate').classList.toggle('hidden', signedIn);
  document.getElementById('login-gate').setAttribute('aria-hidden', signedIn ? 'true' : 'false');
  document.getElementById('app-root').classList.toggle('hidden', !signedIn);

  const status = document.getElementById('sync-status');
  const syncBtn = document.getElementById('sync-btn');

  if (signedIn) {
    syncBtn.disabled = state.syncing || !state.user;
    syncBtn.textContent = state.syncing ? '…' : 'Sync';
    status.classList.remove('ok', 'err');
    if (state.syncing) status.textContent = 'Syncing…';
    else if (state.lastError) { status.classList.add('err'); status.textContent = state.lastError; }
    else if (state.lastSync) { status.classList.add('ok'); status.textContent = state.user ? `Synced ${state.lastSync.toLocaleTimeString()}` : 'Saved on this device'; }
    else status.textContent = state.user ? 'Tap Sync to load your data' : 'This device only';
    applyTheme();
    setScreen(ui.screen);
  }
}

document.getElementById('sign-in-btn')?.addEventListener('click', async () => {
  try {
    await signInWithPopup(auth, provider);
  } catch (e) {
    const code = e?.code || '';
    const msg = code === 'auth/unauthorized-domain'
      ? 'This site isn’t an authorized domain in Firebase Auth settings.'
      : code === 'auth/popup-blocked'
        ? 'Pop-up blocked — allow pop-ups for this site and try again.'
        : (e?.message || 'Sign-in failed');
    alert(msg);
  }
});

document.getElementById('guest-btn')?.addEventListener('click', () => {
  state.guest = true;
  localStorage.setItem('boop_guest', '1');
  enterSession(GUEST_UID);
});

document.getElementById('search-input')?.addEventListener('input', (e) => {
  ui.searchQuery = e.target.value;
  renderContent();
});

document.getElementById('planner-switcher')?.querySelectorAll('[data-planner]').forEach((btn) => {
  btn.addEventListener('click', () => setScreen(btn.dataset.planner));
});

document.addEventListener('keydown', (e) => {
  if (e.key !== 'Escape') return;
  ui.sheetOpen = false;
  ui.settingsOpen = false;
  render();
});

window.addEventListener('resize', () => {
  if (sessionUid()) setScreen(ui.screen);
});

document.getElementById('profile-btn')?.addEventListener('click', () => {
  ui.settingsOpen = !ui.settingsOpen;
  renderSettings();
});

document.getElementById('theme-btn')?.addEventListener('click', () => {
  ui.theme = ui.theme === 'dark' ? 'light' : 'dark';
  render();
});

document.getElementById('sync-btn')?.addEventListener('click', () => syncNow());

document.getElementById('fab')?.addEventListener('click', () => {
  ui.sheetOpen = true;
  ui.sheetStep = 'pick';
  ui.editor = null;
  renderSheet();
});

document.getElementById('sheet-overlay')?.addEventListener('click', () => {
  ui.sheetOpen = false;
  ui.settingsOpen = false;
  render();
});

document.querySelectorAll('.nav-btn').forEach((btn) => {
  btn.addEventListener('click', () => setScreen(btn.dataset.screen));
});

onAuthStateChanged(auth, async (user) => {
  state.user = user;
  if (user) {
    const wasGuest = state.guest || localStorage.getItem('boop_guest') === '1';
    const guestData = wasGuest ? loadLocal(GUEST_UID) : emptyData();
    const accountLocal = loadLocal(user.uid);
    state.guest = false;
    localStorage.removeItem('boop_guest');
    // Carry guest-device data into the Google account before cloud sync.
    state.data = mergeDataObjects(accountLocal, guestData);
    saveLocal(user.uid, state.data);
    render();
    await syncNow();
  } else if (state.guest || localStorage.getItem('boop_guest') === '1') {
    state.guest = true;
    state.data = loadLocal(GUEST_UID);
    state.lastSync = null;
    state.lastError = null;
    ui.settingsOpen = false;
    render();
  } else {
    state.data = emptyData();
    state.lastSync = null;
    state.lastError = null;
    ui.settingsOpen = false;
    render();
  }
});

render();
