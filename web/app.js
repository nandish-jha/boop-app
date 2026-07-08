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

const ui = {
  theme: localStorage.getItem('boop_theme') || 'dark',
  screen: 'home',
  homeFilter: 'all',
  selectedDay: new Date().getDay() === 0 ? 6 : new Date().getDay() - 1,
  walletAccount: 'all',
  sheetOpen: false,
  sheetStep: 'pick',
  createType: null,
  formTitle: '',
  formDetail: '',
};

const state = {
  user: null,
  syncing: false,
  lastSync: null,
  lastError: null,
  cloudMeta: null,
  data: emptyData(),
};

function emptyData() {
  return { tasks: [], notes: [], habits: [], accounts: [], ledgerEntries: [] };
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
    byId.set(e.id, (e.createdAt || 0) >= (ex.createdAt || 0) ? e : ex);
  });
  return [...byId.values()].sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
}

function mergeAll(local, remoteFields) {
  return {
    tasks: mergeTasks(local.tasks, parseArray(remoteFields.tasks)),
    notes: mergeNotes(local.notes, parseArray(remoteFields.notes)),
    habits: mergeById(local.habits, parseArray(remoteFields.habits)),
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

async function syncNow() {
  if (!state.user || state.syncing) return;
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

    let merged = state.data;
    if (snap.exists()) {
      const remote = snap.data();
      state.cloudMeta = {
        exists: true,
        webSyncedAt: remote.webSyncedAt?.toDate?.()?.toISOString?.() || null,
      };
      merged = mergeAll(loadLocal(uid), remote);
    } else {
      state.cloudMeta = { exists: false };
      merged = loadLocal(uid);
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

function activeTasks() {
  return state.data.tasks.filter((t) => !t.archived && !t.done);
}

function activeNotes() {
  return state.data.notes.filter((n) => !n.archived);
}

function accountBalance(accountId) {
  const acct = state.data.accounts.find((a) => a.id === accountId);
  const starting = acct?.starting ?? 0;
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

function habitWeekDots(habit) {
  const keys = (habit.dayKeys || '').split(',').filter(Boolean);
  const colors = typeColors('habit', ui.theme);
  return WEEKDAY_LABELS.map((_, i) => {
    const on = keys.length > i ? !!keys[i] : (habit.progress || 0) > i;
    return {
      bg: on ? colors.accent : 'transparent',
      border: `1.5px solid ${on ? colors.accent : colors.border}`,
    };
  });
}

function buildCardModel(type, obj, options = {}) {
  const colors = typeColors(type, ui.theme);
  const tt = themeTokens(ui.theme);
  const card = {
    type,
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
    done: !!obj.done,
  };
  card.chipOverlay = tt.chipOverlay;
  return card;
}

function reminderCards(tasks, { done = false } = {}) {
  const notes = activeNotes();
  return tasks
    .filter((t) => !!t.done === done)
    .map((t) => {
      const linked = t.linkedNoteId ? notes.find((n) => n.id === t.linkedNoteId) : null;
      return buildCardModel('reminder', {
        title: t.title,
        meta: fmtDue(t.reminderAt),
        showCheckbox: !done,
        checked: !!t.done,
        linkedLabel: linked ? `Note · ${linked.title}` : null,
        onLinked: () => setScreen('notes'),
        done: !!t.done,
      });
    });
}

function noteCards() {
  const reminders = state.data.tasks;
  return activeNotes().map((n) => {
    const linked = n.linkedTaskId ? reminders.find((r) => r.id === n.linkedTaskId) : null;
    return buildCardModel('note', {
      title: n.title || 'Untitled note',
      body: stripHtml(n.body).slice(0, 180) || 'No content',
      linkedLabel: linked ? `Reminder · ${linked.title}` : null,
      onLinked: () => setScreen('reminders'),
    });
  });
}

function habitCards() {
  return state.data.habits.map((h) => buildCardModel('habit', {
    title: h.title,
    meta: `${h.progress || 0} / ${h.goal || 0}`,
    dots: habitWeekDots(h),
  }));
}

function walletCards() {
  const entries = ui.walletAccount === 'all'
    ? state.data.ledgerEntries
    : state.data.ledgerEntries.filter((e) => e.accountId === ui.walletAccount || e.toAccountId === ui.walletAccount);
  return entries.slice(0, 40).map((e) => {
    const amt = e.type === 'expense' ? -(e.amount || 0) : (e.amount || 0);
    return buildCardModel('wallet', {
      title: e.title || e.type || 'Entry',
      meta: `${e.category || e.type} · ${new Date(e.createdAt || 0).toLocaleDateString()}`,
      amountText: fmtMoney(amt),
      amountColor: amt >= 0 ? themeTokens(ui.theme).positiveColor : themeTokens(ui.theme).negativeColor,
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
      title: t.title,
      meta: fmtDue(t.reminderAt),
    }));
}

function renderTintCard(card) {
  const el = document.createElement('article');
  el.className = 'tint-card';
  el.style.background = card.bg;
  el.style.border = `1px solid ${card.border}`;
  if (card.done) el.style.opacity = '0.72';

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
    const n = new Date();
    return d.toDateString() === n.toDateString();
  }).length;
  const habitsDone = state.data.habits.filter((h) => (h.progress || 0) >= (h.goal || 1)).length;
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
    ...noteCards().slice(0, 2),
    ...reminderCards(activeTasks()).slice(0, 2),
    ...calendarCards().slice(0, 1),
    ...habitCards().slice(0, 1),
    ...walletCards().slice(0, 1),
  ];
  if (ui.homeFilter !== 'all') {
    const map = {
      note: noteCards(),
      reminder: reminderCards(activeTasks()),
      calendar: calendarCards(),
      habit: habitCards(),
      wallet: walletCards(),
    };
    homeCards = (map[ui.homeFilter] || []).slice(0, 6);
  }
  root.appendChild(renderCardGrid(homeCards, 'Nothing here yet — add items on your phone and sync.'));
}

function renderSimpleScreen(id, title, cards, emptyText) {
  const root = document.getElementById(id);
  root.innerHTML = '';
  const h = document.createElement('div');
  h.className = 'serif-title';
  h.textContent = title;
  root.appendChild(h);
  root.appendChild(renderCardGrid(cards, emptyText));
}

function renderReminders() {
  const root = document.getElementById('screen-reminders');
  root.innerHTML = '';
  root.appendChild(Object.assign(document.createElement('div'), { className: 'serif-title', textContent: 'Reminders' }));
  root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Pending' }));
  root.appendChild(renderCardGrid(reminderCards(activeTasks()), 'No pending reminders.'));
  const done = reminderCards(state.data.tasks.filter((t) => t.done && !t.archived), { done: true });
  if (done.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'section-label', textContent: 'Completed', style: 'margin-top:8px' }));
    root.appendChild(renderCardGrid(done, ''));
  }
}

function renderCalendar() {
  const root = document.getElementById('screen-calendar');
  root.innerHTML = '';
  root.appendChild(Object.assign(document.createElement('div'), { className: 'serif-title', textContent: 'Calendar' }));

  const strip = document.createElement('div');
  strip.className = 'week-strip';
  const today = new Date();
  const monday = new Date(today);
  monday.setDate(today.getDate() - ((today.getDay() + 6) % 7));
  WEEKDAY_LABELS.forEach((label, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = `week-day${ui.selectedDay === i ? ' active' : ''}`;
    btn.innerHTML = `<div class="muted">${label}</div><div>${d.getDate()}</div>`;
    btn.addEventListener('click', () => { ui.selectedDay = i; render(); });
    strip.appendChild(btn);
  });
  root.appendChild(strip);

  const cards = calendarCards();
  if (!cards.length) {
    root.appendChild(Object.assign(document.createElement('div'), { className: 'muted', textContent: 'No events scheduled.', style: 'padding:10px 2px' }));
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

  root.appendChild(renderCardGrid(walletCards(), 'No transactions yet.'));
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

  if (ui.sheetStep === 'pick') {
    const h = document.createElement('h3');
    h.textContent = 'Add to Boop';
    sheet.appendChild(h);
    const grid = document.createElement('div');
    grid.className = 'type-pick';
    [
      { key: 'note', label: 'Note' },
      { key: 'reminder', label: 'Reminder' },
      { key: 'habit', label: 'Habit' },
      { key: 'wallet', label: 'Wallet entry' },
    ].forEach((t) => {
      const colors = typeColors(t.key === 'reminder' ? 'reminder' : t.key, ui.theme);
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.style.background = colors.bg;
      btn.style.border = `1px solid ${colors.border}`;
      btn.innerHTML = `<span class="material-symbols-outlined" style="color:${colors.accent}">${TYPE_ICONS[t.key === 'reminder' ? 'reminder' : t.key]}</span><span>${t.label}</span>`;
      btn.addEventListener('click', () => {
        ui.sheetStep = 'form';
        ui.createType = t.key;
        ui.formTitle = '';
        ui.formDetail = '';
        renderSheet();
      });
      grid.appendChild(btn);
    });
    sheet.appendChild(grid);
    return;
  }

  const configs = {
    note: { title: 'New Note', p1: 'Title', p2: 'Write something...', body: true },
    reminder: { title: 'New Reminder', p1: 'What needs attention?', p2: 'Due date / time note' },
    habit: { title: 'New Habit', p1: 'Habit name', p2: '' },
    wallet: { title: 'New Wallet Entry', p1: 'Title', p2: 'Amount (use - for expense)' },
  };
  const cfg = configs[ui.createType] || configs.note;

  const back = document.createElement('button');
  back.type = 'button';
  back.textContent = '← Back';
  back.style.cssText = 'background:none;border:none;color:var(--muted);font-size:12px;cursor:pointer;margin-bottom:8px;padding:0';
  back.addEventListener('click', () => { ui.sheetStep = 'pick'; renderSheet(); });
  sheet.appendChild(back);

  const h = document.createElement('h3');
  h.textContent = cfg.title;
  sheet.appendChild(h);

  const t1 = document.createElement('input');
  t1.placeholder = cfg.p1;
  t1.value = ui.formTitle;
  t1.addEventListener('input', (e) => { ui.formTitle = e.target.value; });
  sheet.appendChild(t1);

  if (cfg.body || cfg.p2) {
    const field = cfg.body ? document.createElement('textarea') : document.createElement('input');
    field.placeholder = cfg.p2;
    field.value = ui.formDetail;
    field.addEventListener('input', (e) => { ui.formDetail = e.target.value; });
    sheet.appendChild(field);
  }

  const save = document.createElement('button');
  save.type = 'button';
  save.className = 'save-btn';
  save.textContent = 'SAVE (sync on phone)';
  save.addEventListener('click', () => {
    ui.sheetOpen = false;
    alert('Create on web is view-first for now — add items in the Android app, then tap Sync.');
    render();
  });
  sheet.appendChild(save);
}

function setScreen(screen) {
  ui.screen = screen;
  document.querySelectorAll('.nav-btn').forEach((b) => {
    b.classList.toggle('active', b.dataset.screen === screen);
  });
  document.querySelectorAll('.screen').forEach((s) => {
    s.classList.toggle('active', s.id === `screen-${screen}`);
  });
  renderScreens();
}

function renderScreens() {
  renderHome();
  renderSimpleScreen('screen-notes', 'Notes', noteCards(), 'No notes yet.');
  renderReminders();
  renderCalendar();
  renderSimpleScreen('screen-habits', 'Habits', habitCards(), 'No habits yet.');
  renderWallet();
  renderSheet();
}

function applyTheme() {
  document.body.classList.toggle('light', ui.theme === 'light');
  const icon = document.getElementById('theme-icon');
  if (icon) icon.textContent = ui.theme === 'dark' ? 'light_mode' : 'dark_mode';
  localStorage.setItem('boop_theme', ui.theme);
}

function render() {
  const signedIn = !!state.user;
  document.getElementById('login-gate').classList.toggle('hidden', signedIn);
  document.getElementById('app-root').classList.toggle('hidden', !signedIn);

  const pill = document.getElementById('sync-pill');
  const status = document.getElementById('sync-status');
  const syncBtn = document.getElementById('sync-btn');

  if (signedIn) {
    pill.classList.remove('hidden');
    syncBtn.disabled = state.syncing;
    syncBtn.textContent = state.syncing ? '…' : 'Sync';
    pill.classList.remove('ok', 'err');
    if (state.syncing) status.textContent = 'Syncing…';
    else if (state.lastError) { pill.classList.add('err'); status.textContent = state.lastError; }
    else if (state.lastSync) { pill.classList.add('ok'); status.textContent = `Synced ${state.lastSync.toLocaleTimeString()}`; }
    else status.textContent = 'Tap Sync to load phone data';
    applyTheme();
    renderScreens();
  }
}

document.getElementById('sign-in-btn')?.addEventListener('click', async () => {
  try { await signInWithPopup(auth, provider); }
  catch (e) { alert(e?.message || 'Sign-in failed'); }
});

document.getElementById('profile-btn')?.addEventListener('click', async () => {
  if (state.user) await signOut(auth);
});

document.getElementById('theme-btn')?.addEventListener('click', () => {
  ui.theme = ui.theme === 'dark' ? 'light' : 'dark';
  render();
});

document.getElementById('sync-btn')?.addEventListener('click', () => syncNow());

document.getElementById('fab')?.addEventListener('click', () => {
  ui.sheetOpen = true;
  ui.sheetStep = 'pick';
  renderSheet();
});

document.getElementById('sheet-overlay')?.addEventListener('click', () => {
  ui.sheetOpen = false;
  render();
});

document.querySelectorAll('.nav-btn').forEach((btn) => {
  btn.addEventListener('click', () => setScreen(btn.dataset.screen));
});

onAuthStateChanged(auth, async (user) => {
  state.user = user;
  if (user) {
    state.data = loadLocal(user.uid);
    render();
    await syncNow();
  } else {
    state.data = emptyData();
    state.lastSync = null;
    state.lastError = null;
    render();
  }
});

render();
