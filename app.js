// ===== ProDash — personal productivity dashboard =====
// Local-only storage (localStorage). Export / import JSON supported.

const STORAGE_KEY = 'nandish.productivity.v1';
const uid = () => Math.random().toString(36).slice(2, 10) + Date.now().toString(36).slice(-4);
const todayISO = (d = new Date()) => d.toISOString().slice(0, 10);
const fmtMoney = n => (n < 0 ? '-' : '') + '$' + Math.abs(n).toFixed(2);

// ---------- State ----------
const defaultState = () => ({
  tasks: [],
  goals: [],
  habits: SEED.habits.map(h => ({ ...h })),
  habitLogs: {}, // { 'YYYY-MM-DD': { habitId: value } }
  supplements: SEED.supplements.map(s => ({ ...s })),
  supplementLogs: {}, // { 'YYYY-MM-DD': { suppId: true } }
  skincare: SEED.skincare.map(s => ({ ...s })),
  skincareLogs: {},
  accounts: SEED.accounts.map(a => ({ ...a, balance: 0 })),
  categories: [...SEED.budgetCategories],
  transactions: [],
  budget: { monthlySavingsGoal: 500, monthlyBudget: 0 },
  notes: [],
  workouts: SEED.workouts.map(w => ({ ...w })), // static templates
  settings: { reminderTime: '21:00' }
});

let state = load();

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return defaultState();
    const parsed = JSON.parse(raw);
    // Fill any missing keys with defaults (for upgrades)
    const base = defaultState();
    return { ...base, ...parsed, settings: { ...base.settings, ...(parsed.settings || {}) } };
  } catch { return defaultState(); }
}
function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }

// ---------- Router ----------
const views = {};
let currentTab = 'home';
let moreSub = null; // 'workouts', 'supplements', 'skincare', 'notes', 'goals', 'settings'

function setTitle(t, sub) {
  const titleEl = document.getElementById('pageTitle');
  const subEl = document.getElementById('pageSubtitle');
  const topbar = document.getElementById('topbar');
  titleEl.textContent = t;
  if (sub) {
    subEl.innerHTML = sub;
    topbar.classList.add('has-subtitle');
  } else {
    subEl.innerHTML = '';
    topbar.classList.remove('has-subtitle');
  }
}

function render() {
  const v = document.getElementById('view');
  v.innerHTML = '';
  if (currentTab === 'home') {
    const g = (typeof getGreeting === 'function') ? getGreeting() : 'Hello';
    const q = (typeof getDailyQuote === 'function') ? getDailyQuote() : null;
    const sub = q ? `“${q.q}” <span class="who">— ${q.a}</span>` : '';
    setTitle(`${g}, how are you doing?`, sub);
    views.home(v);
  }
  else if (currentTab === 'tasks') { setTitle('Tasks'); views.tasks(v); }
  else if (currentTab === 'habits') { setTitle('Habits'); views.habits(v); }
  else if (currentTab === 'budget') { setTitle('Budget'); views.budget(v); }
  else if (currentTab === 'more') {
    if (!moreSub) { setTitle('More'); views.more(v); }
    else if (moreSub === 'workouts') { setTitle('Workouts'); views.workouts(v); }
    else if (moreSub === 'supplements') { setTitle('Supplements'); views.supplements(v); }
    else if (moreSub === 'skincare') { setTitle('Skincare'); views.skincare(v); }
    else if (moreSub === 'notes') { setTitle('Notes'); views.notes(v); }
    else if (moreSub === 'goals') { setTitle('Goals'); views.goals(v); }
    else if (moreSub === 'settings') { setTitle('Settings'); views.settings(v); }
  }
  v.scrollTop = 0;
  window.scrollTo(0, 0);
}

document.querySelectorAll('.nav-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    currentTab = btn.dataset.tab;
    moreSub = null;
    render();
  });
});

document.getElementById('settingsBtn').addEventListener('click', () => {
  currentTab = 'more';
  moreSub = 'settings';
  document.querySelectorAll('.nav-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === 'more'));
  render();
});

// ---------- Sheet ----------
const sheetEl = document.getElementById('sheet');
const sheetContent = document.getElementById('sheetContent');
function openSheet(html, onReady) {
  sheetContent.innerHTML = html;
  sheetEl.setAttribute('aria-hidden', 'false');
  document.body.style.overflow = 'hidden';
  if (onReady) onReady(sheetContent);
}
function closeSheet() {
  sheetEl.setAttribute('aria-hidden', 'true');
  document.body.style.overflow = '';
}
document.querySelectorAll('[data-close]').forEach(el => el.addEventListener('click', closeSheet));

// ---------- Toast ----------
let toastTimer;
function toast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => t.classList.remove('show'), 1800);
}

// ---------- FAB contextual ----------
document.getElementById('fab').addEventListener('click', () => {
  if (currentTab === 'home') quickActions();
  else if (currentTab === 'tasks') editTask();
  else if (currentTab === 'habits') markHabitsToday();
  else if (currentTab === 'budget') editTxn();
  else if (currentTab === 'more' && moreSub === 'notes') editNote();
  else if (currentTab === 'more' && moreSub === 'goals') editGoal();
  else if (currentTab === 'more' && moreSub === 'supplements') logSupplementsToday();
  else if (currentTab === 'more' && moreSub === 'skincare') logSkincareToday();
  else quickActions();
});

function quickActions() {
  openSheet(`
    <h2>Quick add</h2>
    <div class="col">
      <button class="btn btn-block" data-a="task">✓  New task</button>
      <button class="btn btn-block" data-a="txn">$  Add transaction</button>
      <button class="btn btn-block" data-a="habit">◈  Log habits today</button>
      <button class="btn btn-block" data-a="supp">💊  Log supplements</button>
      <button class="btn btn-block" data-a="note">📝  New note</button>
      <button class="btn btn-block" data-a="goal">★  New goal</button>
    </div>
  `, root => {
    root.querySelectorAll('[data-a]').forEach(b => b.addEventListener('click', () => {
      const a = b.dataset.a;
      closeSheet();
      setTimeout(() => {
        if (a === 'task') editTask();
        else if (a === 'txn') editTxn();
        else if (a === 'habit') markHabitsToday();
        else if (a === 'supp') logSupplementsToday();
        else if (a === 'note') editNote();
        else if (a === 'goal') editGoal();
      }, 250);
    }));
  });
}

// ===================================================================
// HOME
// ===================================================================
views.home = root => {
  const t = todayISO();
  const todayTasks = state.tasks.filter(x => !x.done && (!x.due || x.due <= t));
  const monthStart = t.slice(0, 7) + '-01';
  const monthTxns = state.transactions.filter(x => x.date >= monthStart);
  const income = monthTxns.filter(x => x.type === 'income').reduce((a, b) => a + b.amount, 0);
  const expense = monthTxns.filter(x => x.type === 'expense').reduce((a, b) => a + b.amount, 0);
  const saved = income - expense;

  const habitsLog = state.habitLogs[t] || {};
  const habitsDone = state.habits.filter(h => habitIsHit(h, habitsLog[h.id])).length;

  const supps = state.supplementLogs[t] || {};
  const suppsDone = Object.values(supps).filter(Boolean).length;
  const suppsTotal = state.supplements.length;

  root.innerHTML = `
    <div class="grid-2 mb-12">
      <div class="kpi"><div class="k">Tasks today</div><div class="v">${todayTasks.length}</div><div class="d">due or overdue</div></div>
      <div class="kpi"><div class="k">Habits</div><div class="v">${habitsDone}/${state.habits.length}</div><div class="d">hit target</div></div>
      <div class="kpi"><div class="k">Supplements</div><div class="v">${suppsDone}/${suppsTotal}</div><div class="d">taken today</div></div>
      <div class="kpi"><div class="k">Saved this month</div><div class="v">${fmtMoney(saved)}</div><div class="d">goal ${fmtMoney(state.budget.monthlySavingsGoal)}</div></div>
    </div>

    <div class="section-h">Quick actions</div>
    <div class="qa-row mb-12">
      <button class="qa" id="qaTask">New task<small>Add to tracker</small></button>
      <button class="qa" id="qaTxn">Add expense<small>Log transaction</small></button>
      <button class="qa" id="qaHabit">Check habits<small>Today</small></button>
      <button class="qa" id="qaSupp">Supplements<small>Morning / night</small></button>
    </div>

    <div class="section-h">Savings progress</div>
    <div class="card">
      <div class="row-between mb-8"><span>${fmtMoney(Math.max(0, saved))} of ${fmtMoney(state.budget.monthlySavingsGoal)}</span><span class="muted xs">${Math.min(100, Math.round(Math.max(0,saved)/Math.max(1,state.budget.monthlySavingsGoal)*100))}%</span></div>
      <div class="progress"><span style="width:${Math.min(100, Math.max(0,saved)/Math.max(1,state.budget.monthlySavingsGoal)*100)}%"></span></div>
    </div>

    <div class="section-h">Up next</div>
    ${todayTasks.length === 0 ? emptyHTML('No tasks due','✓') : todayTasks.slice(0,5).map(renderTaskItem).join('')}
  `;

  root.querySelector('#qaTask').addEventListener('click', () => editTask());
  root.querySelector('#qaTxn').addEventListener('click', () => editTxn());
  root.querySelector('#qaHabit').addEventListener('click', () => markHabitsToday());
  root.querySelector('#qaSupp').addEventListener('click', () => logSupplementsToday());
  bindTaskItems(root);
};

function emptyHTML(msg, ico = '—') {
  return `<div class="empty"><div class="ico">${ico}</div>${msg}</div>`;
}

// ===================================================================
// TASKS
// ===================================================================
let taskFilter = 'all'; // all | today | work | personal | errand
views.tasks = root => {
  root.innerHTML = `
    <div class="seg mb-12">
      ${['all','today','work','personal','errand'].map(f => `<button class="${f===taskFilter?'active':''}" data-f="${f}">${f[0].toUpperCase()+f.slice(1)}</button>`).join('')}
    </div>
    <div id="taskList"></div>
  `;
  root.querySelectorAll('.seg button').forEach(b => b.addEventListener('click', () => { taskFilter = b.dataset.f; views.tasks(root); }));
  renderTaskList(root.querySelector('#taskList'));
};

function renderTaskList(list) {
  const t = todayISO();
  let tasks = state.tasks.slice().sort((a,b) => {
    if (a.done !== b.done) return a.done ? 1 : -1;
    const pr = { high: 0, medium: 1, low: 2 };
    if (a.due && b.due && a.due !== b.due) return a.due.localeCompare(b.due);
    return (pr[a.priority]??3) - (pr[b.priority]??3);
  });
  if (taskFilter === 'today') tasks = tasks.filter(x => !x.done && (!x.due || x.due <= t));
  else if (['work','personal','errand'].includes(taskFilter)) tasks = tasks.filter(x => x.type === taskFilter);

  list.innerHTML = tasks.length === 0 ? emptyHTML('No tasks','✓') : tasks.map(renderTaskItem).join('');
  bindTaskItems(list);
}

function renderTaskItem(t) {
  const prChip = t.priority ? `<span class="chip ${t.priority==='high'?'high':t.priority==='medium'?'med':'low'}">${t.priority}</span>` : '';
  const typeChip = t.type ? `<span class="chip">${t.type}</span>` : '';
  const dueChip = t.due ? `<span class="chip">${t.due}</span>` : '';
  const recChip = t.recurrence && t.recurrence !== 'none' ? `<span class="chip">↻ ${t.recurrence}</span>` : '';
  return `
    <div class="item" data-id="${t.id}">
      <button class="check ${t.done?'done':''}" data-check></button>
      <div class="item-body">
        <div class="item-title ${t.done?'done':''}">${escapeHTML(t.title)}</div>
        <div class="item-meta">${prChip}${typeChip}${dueChip}${recChip}</div>
      </div>
      <div class="item-actions">
        <button data-edit>✎</button>
        <button data-del>✕</button>
      </div>
    </div>`;
}

function bindTaskItems(root) {
  root.querySelectorAll('.item[data-id]').forEach(el => {
    const id = el.dataset.id;
    el.querySelector('[data-check]')?.addEventListener('click', () => toggleTask(id));
    el.querySelector('[data-edit]')?.addEventListener('click', () => editTask(id));
    el.querySelector('[data-del]')?.addEventListener('click', () => delTask(id));
  });
}

function toggleTask(id) {
  const t = state.tasks.find(x => x.id === id);
  if (!t) return;
  t.done = !t.done;
  // recurrence: if done and has recurrence, spawn next occurrence
  if (t.done && t.recurrence && t.recurrence !== 'none' && t.due) {
    const next = new Date(t.due + 'T00:00:00');
    if (t.recurrence === 'daily') next.setDate(next.getDate() + 1);
    else if (t.recurrence === 'weekly') next.setDate(next.getDate() + 7);
    else if (t.recurrence === 'monthly') next.setMonth(next.getMonth() + 1);
    state.tasks.push({ ...t, id: uid(), done: false, due: todayISO(next) });
  }
  save(); render();
}

function delTask(id) {
  if (!confirm('Delete this task?')) return;
  state.tasks = state.tasks.filter(x => x.id !== id);
  save(); render(); toast('Deleted');
}

function editTask(id) {
  const t = id ? state.tasks.find(x => x.id === id) : { id: uid(), title: '', priority: 'medium', type: 'personal', due: '', recurrence: 'none', done: false };
  openSheet(`
    <h2>${id ? 'Edit task' : 'New task'}</h2>
    <div class="field"><label>Title</label><input class="input" id="tt" value="${escapeAttr(t.title)}" placeholder="e.g. Finish assignment"></div>
    <div class="grid-2">
      <div class="field"><label>Priority</label>
        <select class="select" id="tp">
          <option value="high" ${t.priority==='high'?'selected':''}>High</option>
          <option value="medium" ${t.priority==='medium'?'selected':''}>Medium</option>
          <option value="low" ${t.priority==='low'?'selected':''}>Low</option>
        </select></div>
      <div class="field"><label>Type</label>
        <select class="select" id="ty">
          <option value="work" ${t.type==='work'?'selected':''}>Work</option>
          <option value="personal" ${t.type==='personal'?'selected':''}>Personal</option>
          <option value="errand" ${t.type==='errand'?'selected':''}>Errand</option>
        </select></div>
    </div>
    <div class="grid-2">
      <div class="field"><label>Due date</label><input class="input" id="td" type="date" value="${t.due||''}"></div>
      <div class="field"><label>Recurrence</label>
        <select class="select" id="tr">
          <option value="none" ${t.recurrence==='none'?'selected':''}>None</option>
          <option value="daily" ${t.recurrence==='daily'?'selected':''}>Daily</option>
          <option value="weekly" ${t.recurrence==='weekly'?'selected':''}>Weekly</option>
          <option value="monthly" ${t.recurrence==='monthly'?'selected':''}>Monthly</option>
        </select></div>
    </div>
    <div class="row" style="gap:10px; margin-top:8px;">
      <button class="btn primary flex-1" id="save">Save</button>
      ${id ? '<button class="btn danger" id="del">Delete</button>' : ''}
    </div>
  `, root => {
    root.querySelector('#save').addEventListener('click', () => {
      const title = root.querySelector('#tt').value.trim();
      if (!title) { toast('Enter a title'); return; }
      t.title = title;
      t.priority = root.querySelector('#tp').value;
      t.type = root.querySelector('#ty').value;
      t.due = root.querySelector('#td').value;
      t.recurrence = root.querySelector('#tr').value;
      if (!id) state.tasks.push(t);
      save(); closeSheet(); render(); toast(id ? 'Updated' : 'Added');
    });
    root.querySelector('#del')?.addEventListener('click', () => { closeSheet(); delTask(id); });
  });
}

// ===================================================================
// HABITS
// ===================================================================
views.habits = root => {
  const t = todayISO();
  const log = state.habitLogs[t] || {};
  const streakMap = {};
  state.habits.forEach(h => streakMap[h.id] = currentStreak(h));
  root.innerHTML = `
    <div class="card">
      <div class="row-between mb-8"><div class="card-h" style="margin:0">Today · ${t}</div><button class="btn ghost small" id="logAll">Edit</button></div>
      <div class="col">
        ${state.habits.map(h => habitRow(h, log[h.id], streakMap[h.id])).join('')}
      </div>
    </div>
    <div class="section-h">Monthly heatmap</div>
    <div class="card">${heatmapHTML()}</div>
    <div class="section-h">Streaks</div>
    <div class="card">
      ${state.habits.map(h => `<div class="row-between" style="padding:6px 0; border-bottom:1px dashed var(--border);"><span>${h.name}</span><span class="muted">🔥 ${streakMap[h.id]} days</span></div>`).join('')}
    </div>
  `;
  root.querySelector('#logAll').addEventListener('click', markHabitsToday);
  root.querySelectorAll('[data-hcheck]').forEach(b => b.addEventListener('click', () => {
    const id = b.dataset.hcheck;
    const day = todayISO();
    if (!state.habitLogs[day]) state.habitLogs[day] = {};
    state.habitLogs[day][id] = !state.habitLogs[day][id];
    save(); render();
  }));
};

function habitRow(h, val, streak) {
  if (h.type === 'check') {
    const done = !!val;
    return `<div class="row-between" style="padding:6px 0;">
      <div><div>${h.name}</div><div class="xs dim">🔥 ${streak}</div></div>
      <button class="check ${done?'done':''}" data-hcheck="${h.id}"></button>
    </div>`;
  }
  if (h.type === 'timerange') {
    const hrs = timerangeHours(val);
    const stats = sleepStats(h);
    const label = hrs != null
      ? `${hrs.toFixed(1)} hrs (${val.start} → ${val.end})`
      : 'Not logged';
    const avgPct = stats.logged ? Math.min(100, (stats.avg / (h.healthyMax || 9)) * 100) : 0;
    return `<div style="padding:6px 0;">
      <div class="row-between mb-8"><div><div>${h.name}</div><div class="xs dim">${label} · 🔥 ${streak}</div></div></div>
      <div class="progress"><span style="width:${avgPct}%"></span></div>
      <div class="xs dim" style="margin-top:4px;">Avg ${stats.logged ? stats.avg.toFixed(1)+' hrs' : '—'} · Consistency ${stats.logged ? stats.consistency+'%' : '—'} ${stats.logged ? (stats.consistency >= 80 ? '✓' : '· goal 80%') : ''}</div>
    </div>`;
  }
  const cur = val || 0;
  const pct = Math.min(100, (cur / h.target) * 100);
  return `<div style="padding:6px 0;">
    <div class="row-between mb-8"><div><div>${h.name}</div><div class="xs dim">${cur}/${h.target} ${h.unit} · 🔥 ${streak}</div></div></div>
    <div class="progress"><span style="width:${pct}%"></span></div>
  </div>`;
}

// Convert {start:'HH:MM', end:'HH:MM'} to hours (handles overnight)
function timerangeHours(v) {
  if (!v || typeof v !== 'object' || !v.start || !v.end) return null;
  const [sh, sm] = v.start.split(':').map(Number);
  const [eh, em] = v.end.split(':').map(Number);
  if ([sh, sm, eh, em].some(n => isNaN(n))) return null;
  let start = sh * 60 + sm;
  let end = eh * 60 + em;
  if (end <= start) end += 24 * 60; // overnight
  return (end - start) / 60;
}

// Is a habit "hit" for a given day?
function habitIsHit(h, v) {
  if (h.type === 'check') return !!v;
  if (h.type === 'timerange') {
    const hrs = timerangeHours(v);
    if (hrs == null) return false;
    const lo = h.healthyMin ?? 7, hi = h.healthyMax ?? 9;
    return hrs >= lo && hrs <= hi;
  }
  return typeof v === 'number' && v >= h.target;
}

// Sleep average + consistency over all logged days (never a "target" — user-requested)
function sleepStats(h) {
  const hours = [];
  let within = 0;
  const lo = h.healthyMin ?? 7, hi = h.healthyMax ?? 9;
  for (const day of Object.keys(state.habitLogs)) {
    const hrs = timerangeHours(state.habitLogs[day][h.id]);
    if (hrs != null) {
      hours.push(hrs);
      if (hrs >= lo && hrs <= hi) within++;
    }
  }
  const logged = hours.length;
  const avg = logged ? hours.reduce((a,b)=>a+b,0) / logged : 0;
  const consistency = logged ? Math.round((within / logged) * 100) : 0;
  return { logged, avg, consistency };
}

function currentStreak(h) {
  let s = 0;
  const d = new Date();
  for (let i = 0; i < 366; i++) {
    const key = todayISO(d);
    const log = state.habitLogs[key] || {};
    if (habitIsHit(h, log[h.id])) s++; else break;
    d.setDate(d.getDate() - 1);
  }
  return s;
}

function heatmapHTML() {
  const cells = [];
  const today = new Date();
  const start = new Date(today);
  start.setDate(start.getDate() - 89); // ~3 months
  for (let i = 0; i < 90; i++) {
    const d = new Date(start); d.setDate(start.getDate() + i);
    const key = todayISO(d);
    const log = state.habitLogs[key] || {};
    const hits = state.habits.filter(h => habitIsHit(h, log[h.id])).length;
    const r = state.habits.length ? hits / state.habits.length : 0;
    const lvl = r === 0 ? '' : r < .34 ? 'l1' : r < .67 ? 'l2' : r < 1 ? 'l3' : 'l4';
    cells.push(`<div class="cell ${lvl}" title="${key}: ${hits}/${state.habits.length}"></div>`);
  }
  return `<div class="heatmap">${cells.join('')}</div>
    <div class="legend"><span class="lg"><span class="dot" style="background:#2a2a2a"></span>low</span><span class="lg"><span class="dot" style="background:#9a9a9a"></span>mid</span><span class="lg"><span class="dot" style="background:#e8e8e8"></span>full</span></div>`;
}

function markHabitsToday() {
  const t = todayISO();
  const log = state.habitLogs[t] || {};
  openSheet(`
    <h2>Log habits · ${t}</h2>
    <div class="col">
      ${state.habits.map(h => {
        if (h.type === 'check') {
          return `<div class="row-between" style="padding:8px 0;"><span>${h.name}</span>
            <button class="check ${log[h.id]?'done':''}" data-hc="${h.id}"></button></div>`;
        }
        if (h.type === 'timerange') {
          const v = log[h.id] || {};
          const hrs = timerangeHours(v);
          return `<div class="field"><label>${h.name}${hrs!=null?` · ${hrs.toFixed(1)} hrs`:''}</label>
            <div class="row" style="display:flex; gap:8px;">
              <div style="flex:1;"><div class="xs dim mb-4">Start (bedtime)</div>
                <input type="time" class="input" data-htr="${h.id}" data-part="start" value="${v.start || ''}"></div>
              <div style="flex:1;"><div class="xs dim mb-4">End (wake)</div>
                <input type="time" class="input" data-htr="${h.id}" data-part="end" value="${v.end || ''}"></div>
            </div></div>`;
        }
        return `<div class="field"><label>${h.name} (${h.unit}, target ${h.target})</label>
          <input type="number" step="${h.unit==='ml'?'50':'1'}" min="0" class="input" data-hq="${h.id}" value="${log[h.id] ?? ''}"></div>`;
      }).join('')}
    </div>
    <button class="btn primary btn-block" id="saveH">Save</button>
  `, root => {
    root.querySelectorAll('[data-hc]').forEach(b => b.addEventListener('click', () => {
      b.classList.toggle('done');
    }));
    root.querySelector('#saveH').addEventListener('click', () => {
      const newLog = {};
      root.querySelectorAll('[data-hc]').forEach(b => { newLog[b.dataset.hc] = b.classList.contains('done'); });
      root.querySelectorAll('[data-hq]').forEach(i => { const v = parseFloat(i.value); if (!isNaN(v)) newLog[i.dataset.hq] = v; });
      // Time-range habits: collect start/end pairs per habit id
      const trMap = {};
      root.querySelectorAll('[data-htr]').forEach(i => {
        const id = i.dataset.htr;
        trMap[id] = trMap[id] || {};
        if (i.value) trMap[id][i.dataset.part] = i.value;
      });
      Object.keys(trMap).forEach(id => {
        if (trMap[id].start && trMap[id].end) newLog[id] = trMap[id];
      });
      state.habitLogs[t] = newLog;
      save(); closeSheet(); render(); toast('Saved');
    });
  });
}

// ===================================================================
// BUDGET
// ===================================================================
let budgetTab = 'overview';
views.budget = root => {
  root.innerHTML = `
    <div class="seg mb-12">
      <button class="${budgetTab==='overview'?'active':''}" data-b="overview">Overview</button>
      <button class="${budgetTab==='txns'?'active':''}" data-b="txns">Transactions</button>
      <button class="${budgetTab==='accts'?'active':''}" data-b="accts">Accounts</button>
    </div>
    <div id="bContent"></div>
  `;
  root.querySelectorAll('.seg button').forEach(b => b.addEventListener('click', () => { budgetTab = b.dataset.b; views.budget(root); }));
  const c = root.querySelector('#bContent');
  if (budgetTab === 'overview') renderBudgetOverview(c);
  else if (budgetTab === 'txns') renderTxns(c);
  else renderAccounts(c);
};

function renderBudgetOverview(c) {
  const t = todayISO();
  const monthStart = t.slice(0,7) + '-01';
  const monthTxns = state.transactions.filter(x => x.date >= monthStart);
  const income = monthTxns.filter(x => x.type === 'income').reduce((a,b) => a+b.amount, 0);
  const expense = monthTxns.filter(x => x.type === 'expense').reduce((a,b) => a+b.amount, 0);
  const saved = income - expense;

  // Category pie
  const byCat = {};
  monthTxns.filter(x => x.type === 'expense').forEach(x => {
    byCat[x.category] = (byCat[x.category] || 0) + x.amount;
  });
  const cats = Object.entries(byCat).sort((a,b) => b[1]-a[1]);
  const totalE = cats.reduce((a,b)=>a+b[1],0);
  const palette = ['#e8e8e8','#9a9a9a','#6e6e6e','#4a4a4a','#d0d0d0','#7ee787','#f0b72f','#ff9c9c','#9ed8ff'];
  let cum = 0;
  const pieStops = cats.map(([k,v],i) => {
    const from = (cum/totalE)*360; cum += v;
    const to = (cum/totalE)*360;
    return `${palette[i%palette.length]} ${from.toFixed(2)}deg ${to.toFixed(2)}deg`;
  }).join(', ');

  // Last 7 days bar (expenses)
  const days = [];
  for (let i = 6; i >= 0; i--) { const d = new Date(); d.setDate(d.getDate()-i); days.push(todayISO(d)); }
  const daily = days.map(d => state.transactions.filter(x => x.date === d && x.type === 'expense').reduce((a,b)=>a+b.amount,0));
  const maxD = Math.max(1, ...daily);

  c.innerHTML = `
    <div class="grid-2 mb-12">
      <div class="kpi"><div class="k">Income (mo)</div><div class="v">${fmtMoney(income)}</div></div>
      <div class="kpi"><div class="k">Expense (mo)</div><div class="v">${fmtMoney(expense)}</div></div>
      <div class="kpi"><div class="k">Saved</div><div class="v">${fmtMoney(saved)}</div><div class="d">goal ${fmtMoney(state.budget.monthlySavingsGoal)}</div></div>
      <div class="kpi"><div class="k">Budget</div><div class="v">${fmtMoney(state.budget.monthlyBudget)}</div><div class="d">${state.budget.monthlyBudget>0?Math.round(expense/state.budget.monthlyBudget*100)+'% used':'set in settings'}</div></div>
    </div>

    <div class="section-h">Spending by category (this month)</div>
    <div class="card">
      ${cats.length === 0 ? emptyHTML('No expenses this month','$') : `
        <div class="pie" style="background: conic-gradient(${pieStops});"></div>
        <div class="legend">${cats.map(([k,v],i) => `<span class="lg"><span class="dot" style="background:${palette[i%palette.length]}"></span>${k} · ${fmtMoney(v)}</span>`).join('')}</div>
      `}
    </div>

    <div class="section-h">Last 7 days</div>
    <div class="card">
      <div class="bar-chart">
        ${daily.map((v,i) => `<div class="bar" style="height:${(v/maxD)*100}%"><i>${days[i].slice(8)}</i></div>`).join('')}
      </div>
      <div class="divider"></div>
      <div class="row-between xs muted"><span>Total</span><span>${fmtMoney(daily.reduce((a,b)=>a+b,0))}</span></div>
    </div>
  `;
}

function renderTxns(c) {
  const grouped = {};
  state.transactions.slice().sort((a,b) => b.date.localeCompare(a.date)).forEach(tx => {
    (grouped[tx.date] = grouped[tx.date] || []).push(tx);
  });
  const keys = Object.keys(grouped);
  c.innerHTML = keys.length === 0 ? emptyHTML('No transactions yet','$') : keys.map(d => `
    <div class="section-h">${d}</div>
    <div class="card" style="padding:0;">
      ${grouped[d].map(tx => `
        <div class="txn" data-txn="${tx.id}">
          <div class="txn-body">
            <div class="txn-cat">${escapeHTML(tx.category)}</div>
            <div class="txn-sub">${escapeHTML(accountName(tx.accountId) || '')}${tx.note ? ' · '+escapeHTML(tx.note) : ''}</div>
          </div>
          <div class="txn-amt ${tx.type}">${tx.type === 'income' ? '+' : '-'}${fmtMoney(tx.amount)}</div>
        </div>
      `).join('')}
    </div>
  `).join('');
  c.querySelectorAll('[data-txn]').forEach(el => el.addEventListener('click', () => editTxn(el.dataset.txn)));
}

function renderAccounts(c) {
  const byBank = {};
  state.accounts.forEach(a => { (byBank[a.bank] = byBank[a.bank] || []).push(a); });
  c.innerHTML = Object.keys(byBank).map(b => `
    <div class="section-h">${b}</div>
    <div class="card" style="padding:0;">
      ${byBank[b].map(a => `
        <div class="txn"><div class="txn-body"><div class="txn-cat">${escapeHTML(a.name)}</div><div class="txn-sub">${a.type}</div></div>
          <div class="txn-amt">${fmtMoney(a.balance||0)}</div></div>
      `).join('')}
    </div>
  `).join('') + `<div class="spacer"></div><button class="btn btn-block" id="editAccts">Edit balances</button>`;
  c.querySelector('#editAccts')?.addEventListener('click', editAccountBalances);
}

function accountName(id) { return state.accounts.find(a => a.id === id)?.name; }

function editTxn(id) {
  const tx = id ? state.transactions.find(x => x.id === id) :
    { id: uid(), type: 'expense', amount: 0, category: state.categories[0], accountId: state.accounts[0].id, date: todayISO(), note: '' };
  openSheet(`
    <h2>${id ? 'Edit' : 'New'} transaction</h2>
    <div class="seg mb-12" id="typeSeg">
      <button class="${tx.type==='expense'?'active':''}" data-t="expense">Expense</button>
      <button class="${tx.type==='income'?'active':''}" data-t="income">Income</button>
      <button class="${tx.type==='transfer'?'active':''}" data-t="transfer">Transfer</button>
    </div>
    <div class="grid-2">
      <div class="field"><label>Amount</label><input class="input" id="txa" type="number" step="0.01" value="${tx.amount||''}"></div>
      <div class="field"><label>Date</label><input class="input" id="txd" type="date" value="${tx.date}"></div>
    </div>
    <div class="field"><label>Category</label>
      <select class="select" id="txc">
        ${state.categories.map(c => `<option ${c===tx.category?'selected':''}>${c}</option>`).join('')}
      </select>
    </div>
    <div class="field"><label>Account</label>
      <select class="select" id="txac">
        ${state.accounts.map(a => `<option value="${a.id}" ${a.id===tx.accountId?'selected':''}>${a.name}</option>`).join('')}
      </select>
    </div>
    <div class="field"><label>Note</label><input class="input" id="txn" value="${escapeAttr(tx.note||'')}"></div>
    <div class="field"><label>Add new category</label>
      <div class="row" style="gap:8px;"><input class="input" id="newCat" placeholder="e.g. Subscriptions"><button class="btn" id="addCat">Add</button></div>
    </div>
    <div class="row" style="gap:10px; margin-top:8px;">
      <button class="btn primary flex-1" id="save">Save</button>
      ${id ? '<button class="btn danger" id="del">Delete</button>' : ''}
    </div>
  `, root => {
    let type = tx.type;
    root.querySelectorAll('#typeSeg button').forEach(b => b.addEventListener('click', () => {
      root.querySelectorAll('#typeSeg button').forEach(x => x.classList.remove('active'));
      b.classList.add('active'); type = b.dataset.t;
    }));
    root.querySelector('#addCat').addEventListener('click', () => {
      const v = root.querySelector('#newCat').value.trim();
      if (v && !state.categories.includes(v)) { state.categories.push(v); save(); toast('Added'); const sel = root.querySelector('#txc'); sel.insertAdjacentHTML('beforeend', `<option selected>${v}</option>`); }
    });
    root.querySelector('#save').addEventListener('click', () => {
      const amt = parseFloat(root.querySelector('#txa').value);
      if (isNaN(amt) || amt <= 0) { toast('Enter amount'); return; }
      tx.type = type;
      tx.amount = amt;
      tx.category = root.querySelector('#txc').value;
      tx.accountId = root.querySelector('#txac').value;
      tx.date = root.querySelector('#txd').value;
      tx.note = root.querySelector('#txn').value.trim();
      if (!id) state.transactions.push(tx);
      save(); closeSheet(); render(); toast(id ? 'Updated' : 'Added');
    });
    root.querySelector('#del')?.addEventListener('click', () => {
      state.transactions = state.transactions.filter(x => x.id !== id);
      save(); closeSheet(); render(); toast('Deleted');
    });
  });
}

function editAccountBalances() {
  openSheet(`
    <h2>Account balances</h2>
    <div class="col">
      ${state.accounts.map(a => `<div class="field"><label>${a.name}</label><input class="input" type="number" step="0.01" data-ab="${a.id}" value="${a.balance||0}"></div>`).join('')}
    </div>
    <button class="btn primary btn-block" id="saveAcc">Save</button>
  `, root => {
    root.querySelector('#saveAcc').addEventListener('click', () => {
      root.querySelectorAll('[data-ab]').forEach(i => {
        const acc = state.accounts.find(x => x.id === i.dataset.ab);
        acc.balance = parseFloat(i.value) || 0;
      });
      save(); closeSheet(); render(); toast('Saved');
    });
  });
}

// ===================================================================
// MORE menu
// ===================================================================
views.more = root => {
  const items = [
    { k: 'workouts', ico: '🏋', n: 'Workouts', s: `${state.workouts.length} templates` },
    { k: 'supplements', ico: '💊', n: 'Supplements', s: `${state.supplements.length} items` },
    { k: 'skincare', ico: '✦', n: 'Skincare', s: `${state.skincare.length} steps` },
    { k: 'notes', ico: '📝', n: 'Notes', s: `${state.notes.length} saved` },
    { k: 'goals', ico: '★', n: 'Goals', s: `${state.goals.length} tracked` },
    { k: 'settings', ico: '⚙', n: 'Settings & Data', s: 'Export / import / reminders' }
  ];
  root.innerHTML = items.map(i => `
    <button class="more-item" data-k="${i.k}">
      <span class="ico">${i.ico}</span>
      <div><div>${i.n}</div><div class="xs dim">${i.s}</div></div>
      <span class="arrow">›</span>
    </button>`).join('');
  root.querySelectorAll('[data-k]').forEach(b => b.addEventListener('click', () => { moreSub = b.dataset.k; render(); }));
};

// ===================================================================
// WORKOUTS
// ===================================================================
views.workouts = root => {
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    <div class="col" id="wList"></div>
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  const list = root.querySelector('#wList');
  list.innerHTML = state.workouts.map(w => `
    <button class="more-item" data-w="${w.id}">
      <span class="ico">🏋</span>
      <div><div>${escapeHTML(w.name)}</div><div class="xs dim">${w.days.length} day${w.days.length>1?'s':''}</div></div>
      <span class="arrow">›</span>
    </button>
  `).join('');
  list.querySelectorAll('[data-w]').forEach(b => b.addEventListener('click', () => showWorkout(b.dataset.w)));
};

function showWorkout(id) {
  const w = state.workouts.find(x => x.id === id);
  if (!w) return;
  const view = document.getElementById('view');
  setTitle(w.name);
  view.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    ${w.note ? `<div class="card muted small">${escapeHTML(w.note)}</div>` : ''}
    ${w.days.map(d => `
      <div class="card">
        <div class="card-h">${escapeHTML(d.day)}</div>
        ${d.exercises.map(e => `<div class="exercise-line"><div class="name">${escapeHTML(e)}</div></div>`).join('')}
      </div>
    `).join('')}
  `;
  view.querySelector('#back').addEventListener('click', () => { render(); });
}

// ===================================================================
// SUPPLEMENTS
// ===================================================================
views.supplements = root => {
  const t = todayISO();
  const log = state.supplementLogs[t] || {};
  const groups = ['morning','workout','night'];
  const total = state.supplements.length;
  const taken = Object.values(log).filter(Boolean).length;
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    <div class="card row-between"><div><div class="card-h" style="margin:0">Today · ${t}</div><div>${taken}/${total} taken</div></div>
      <button class="btn primary" id="logAll">Log</button></div>
    ${groups.map(g => `
      <div class="section-h">${g === 'morning' ? 'Morning' : g === 'workout' ? 'Pre / Post workout' : 'Night'}</div>
      <div class="card" style="padding:10px">
        ${state.supplements.filter(s => s.time === g).map(s => `
          <div class="supp-row">
            <div class="n"><div>${escapeHTML(s.name)}</div><div class="d">${escapeHTML(s.dose||'')}</div></div>
            <button class="check ${log[s.id]?'done':''}" data-s="${s.id}"></button>
          </div>`).join('')}
      </div>
    `).join('')}
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  root.querySelector('#logAll').addEventListener('click', logSupplementsToday);
  root.querySelectorAll('[data-s]').forEach(b => b.addEventListener('click', () => {
    if (!state.supplementLogs[t]) state.supplementLogs[t] = {};
    state.supplementLogs[t][b.dataset.s] = !state.supplementLogs[t][b.dataset.s];
    save(); render();
  }));
};

function logSupplementsToday() {
  const t = todayISO();
  const log = state.supplementLogs[t] || {};
  openSheet(`
    <h2>Log supplements · ${t}</h2>
    ${['morning','workout','night'].map(g => `
      <div class="section-h">${g}</div>
      ${state.supplements.filter(s => s.time === g).map(s => `
        <div class="row-between" style="padding:8px 0;">
          <div><div>${s.name}</div><div class="xs dim">${s.dose||''}</div></div>
          <button class="check ${log[s.id]?'done':''}" data-ls="${s.id}"></button>
        </div>`).join('')}
    `).join('')}
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelectorAll('[data-ls]').forEach(b => b.addEventListener('click', () => b.classList.toggle('done')));
    root.querySelector('#sv').addEventListener('click', () => {
      const nl = {};
      root.querySelectorAll('[data-ls]').forEach(b => nl[b.dataset.ls] = b.classList.contains('done'));
      state.supplementLogs[t] = nl;
      save(); closeSheet(); render(); toast('Saved');
    });
  });
}

// ===================================================================
// SKINCARE
// ===================================================================
views.skincare = root => {
  const t = todayISO();
  const log = state.skincareLogs[t] || {};
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    ${['morning','night'].map(g => `
      <div class="section-h">${g}</div>
      <div class="card" style="padding:10px">
        ${state.skincare.filter(s => s.time === g).map(s => `
          <div class="supp-row">
            <div class="n">${escapeHTML(s.name)}</div>
            <button class="check ${log[s.id]?'done':''}" data-sk="${s.id}"></button>
          </div>`).join('')}
      </div>`).join('')}
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  root.querySelectorAll('[data-sk]').forEach(b => b.addEventListener('click', () => {
    if (!state.skincareLogs[t]) state.skincareLogs[t] = {};
    state.skincareLogs[t][b.dataset.sk] = !state.skincareLogs[t][b.dataset.sk];
    save(); render();
  }));
};

function logSkincareToday() {
  const t = todayISO();
  if (!state.skincareLogs[t]) state.skincareLogs[t] = {};
  state.skincare.forEach(s => state.skincareLogs[t][s.id] = true);
  save(); render(); toast('All checked');
}

// ===================================================================
// NOTES
// ===================================================================
let noteFilter = '';
views.notes = root => {
  const allTags = [...new Set(state.notes.flatMap(n => n.tags || []))];
  const notes = state.notes.filter(n => !noteFilter || (n.tags||[]).includes(noteFilter)).sort((a,b) => (b.updated||0) - (a.updated||0));
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    ${allTags.length ? `<div class="seg mb-12">
      <button class="${noteFilter===''?'active':''}" data-nt="">All</button>
      ${allTags.map(t => `<button class="${noteFilter===t?'active':''}" data-nt="${escapeAttr(t)}">${escapeHTML(t)}</button>`).join('')}
    </div>` : ''}
    <div id="nList">${notes.length === 0 ? emptyHTML('No notes yet','📝') : notes.map(n => `
      <div class="note-item" data-n="${n.id}">
        <div class="nt">${escapeHTML(n.title || 'Untitled')}</div>
        <div class="nb">${escapeHTML((n.body||'').slice(0,200))}</div>
        <div class="nm">${(n.tags||[]).map(t => `<span class="tag">${escapeHTML(t)}</span>`).join('')}</div>
      </div>`).join('')}</div>
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  root.querySelectorAll('[data-nt]').forEach(b => b.addEventListener('click', () => { noteFilter = b.dataset.nt; render(); }));
  root.querySelectorAll('[data-n]').forEach(el => el.addEventListener('click', () => editNote(el.dataset.n)));
};

function editNote(id) {
  const n = id ? state.notes.find(x => x.id === id) : { id: uid(), title:'', body:'', tags:[], updated: Date.now() };
  openSheet(`
    <h2>${id ? 'Edit note' : 'New note'}</h2>
    <div class="field"><label>Title</label><input class="input" id="nt" value="${escapeAttr(n.title)}"></div>
    <div class="field"><label>Tags (comma-separated)</label><input class="input" id="ng" value="${escapeAttr((n.tags||[]).join(', '))}"></div>
    <div class="field"><label>Body</label><textarea class="textarea" id="nb">${escapeHTML(n.body||'')}</textarea></div>
    <div class="row" style="gap:10px;">
      <button class="btn primary flex-1" id="sv">Save</button>
      ${id ? '<button class="btn danger" id="del">Delete</button>' : ''}
    </div>
  `, root => {
    root.querySelector('#sv').addEventListener('click', () => {
      n.title = root.querySelector('#nt').value.trim();
      n.body = root.querySelector('#nb').value;
      n.tags = root.querySelector('#ng').value.split(',').map(s => s.trim()).filter(Boolean);
      n.updated = Date.now();
      if (!id) state.notes.push(n);
      save(); closeSheet(); render(); toast(id ? 'Updated' : 'Saved');
    });
    root.querySelector('#del')?.addEventListener('click', () => {
      state.notes = state.notes.filter(x => x.id !== id);
      save(); closeSheet(); render(); toast('Deleted');
    });
  });
}

// ===================================================================
// GOALS
// ===================================================================
views.goals = root => {
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    <div id="gList">${state.goals.length === 0 ? emptyHTML('No goals yet','★') : state.goals.map(g => `
      <div class="card" data-g="${g.id}">
        <div class="row-between mb-8"><b>${escapeHTML(g.title)}</b><span class="xs muted">${g.target||''}</span></div>
        <div class="progress"><span style="width:${Math.min(100, (g.progress||0))}%"></span></div>
        <div class="row-between mt-8 xs muted"><span>${g.progress||0}%</span><span>${g.deadline||''}</span></div>
        <div class="row mt-8" style="gap:6px;">
          <button class="btn small" data-edit="${g.id}">Edit</button>
          <button class="btn small" data-plus="${g.id}">+10%</button>
          <button class="btn small danger" data-del="${g.id}">Delete</button>
        </div>
      </div>`).join('')}</div>
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  root.querySelectorAll('[data-edit]').forEach(b => b.addEventListener('click', () => editGoal(b.dataset.edit)));
  root.querySelectorAll('[data-del]').forEach(b => b.addEventListener('click', () => { state.goals = state.goals.filter(g => g.id !== b.dataset.del); save(); render(); }));
  root.querySelectorAll('[data-plus]').forEach(b => b.addEventListener('click', () => {
    const g = state.goals.find(x => x.id === b.dataset.plus);
    g.progress = Math.min(100, (g.progress||0) + 10); save(); render();
  }));
};

function editGoal(id) {
  const g = id ? state.goals.find(x => x.id === id) : { id: uid(), title:'', target:'', deadline:'', progress: 0 };
  openSheet(`
    <h2>${id ? 'Edit goal' : 'New goal'}</h2>
    <div class="field"><label>Title</label><input class="input" id="gt" value="${escapeAttr(g.title)}"></div>
    <div class="field"><label>Target / description</label><input class="input" id="gtg" value="${escapeAttr(g.target||'')}"></div>
    <div class="grid-2">
      <div class="field"><label>Deadline</label><input class="input" type="date" id="gd" value="${g.deadline||''}"></div>
      <div class="field"><label>Progress %</label><input class="input" type="number" min="0" max="100" id="gp" value="${g.progress||0}"></div>
    </div>
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelector('#sv').addEventListener('click', () => {
      g.title = root.querySelector('#gt').value.trim();
      if (!g.title) { toast('Enter a title'); return; }
      g.target = root.querySelector('#gtg').value.trim();
      g.deadline = root.querySelector('#gd').value;
      g.progress = Math.min(100, Math.max(0, parseFloat(root.querySelector('#gp').value) || 0));
      if (!id) state.goals.push(g);
      save(); closeSheet(); render(); toast('Saved');
    });
  });
}

// ===================================================================
// SETTINGS
// ===================================================================
views.settings = root => {
  root.innerHTML = `
    <button class="btn small mb-12" id="back">← Back</button>
    <div class="card">
      <div class="card-h">Data</div>
      <button class="btn btn-block mb-8" id="exp">Export JSON</button>
      <button class="btn btn-block mb-8" id="expCsv">Export transactions CSV</button>
      <label class="btn btn-block mb-8" style="display:block; text-align:center; cursor:pointer;">Import JSON
        <input type="file" accept="application/json" id="imp" style="display:none">
      </label>
      <label class="btn btn-block mb-8" style="display:block; text-align:center; cursor:pointer;">Import Cashew CSV
        <input type="file" accept=".csv,text/csv" id="impCsh" style="display:none">
      </label>
      <div class="xs dim mb-8">Cashew transactions are appended (duplicates skipped). Unknown accounts are auto-created.</div>
      <button class="btn btn-block danger" id="reset">Reset all data</button>
    </div>

    <div class="card">
      <div class="card-h">Reminder</div>
      <div class="field"><label>Daily habit reminder time</label><input class="input" type="time" id="remT" value="${state.settings.reminderTime}"></div>
      <button class="btn btn-block" id="remSave">Save</button>
      <div class="xs dim" style="margin-top:8px;">Enable notifications to receive a nudge each day when the app is open.</div>
      <button class="btn btn-block mt-8" id="notif">Enable notifications</button>
    </div>

    <div class="card">
      <div class="card-h">Budget</div>
      <div class="field"><label>Monthly savings goal</label><input class="input" type="number" id="mSav" value="${state.budget.monthlySavingsGoal}"></div>
      <div class="field"><label>Monthly spending budget (optional)</label><input class="input" type="number" id="mBud" value="${state.budget.monthlyBudget}"></div>
      <button class="btn btn-block" id="bSave">Save</button>
    </div>

    <div class="card">
      <div class="card-h">About</div>
      <div class="small muted">All data is stored locally on this device. Install this app from your browser menu (“Add to home screen” / “Install app”) for a native feel and offline use.</div>
    </div>
  `;
  root.querySelector('#back').addEventListener('click', () => { moreSub = null; render(); });
  root.querySelector('#exp').addEventListener('click', exportJSON);
  root.querySelector('#expCsv').addEventListener('click', exportCSV);
  root.querySelector('#imp').addEventListener('change', importJSON);
  root.querySelector('#impCsh').addEventListener('change', importCashew);
  root.querySelector('#reset').addEventListener('click', () => {
    if (!confirm('Erase ALL data? This cannot be undone.')) return;
    localStorage.removeItem(STORAGE_KEY);
    state = defaultState(); save(); render(); toast('Reset');
  });
  root.querySelector('#remSave').addEventListener('click', () => {
    state.settings.reminderTime = root.querySelector('#remT').value;
    save(); toast('Saved'); scheduleReminder();
  });
  root.querySelector('#notif').addEventListener('click', async () => {
    if (!('Notification' in window)) { toast('Not supported'); return; }
    const p = await Notification.requestPermission();
    toast(p === 'granted' ? 'Enabled' : 'Denied');
  });
  root.querySelector('#bSave').addEventListener('click', () => {
    state.budget.monthlySavingsGoal = parseFloat(root.querySelector('#mSav').value) || 0;
    state.budget.monthlyBudget = parseFloat(root.querySelector('#mBud').value) || 0;
    save(); toast('Saved');
  });
};

function exportJSON() {
  const blob = new Blob([JSON.stringify(state, null, 2)], { type: 'application/json' });
  downloadBlob(blob, `productivity-backup-${todayISO()}.json`);
}
function exportCSV() {
  const rows = [['date','type','amount','category','account','note']];
  state.transactions.forEach(t => rows.push([t.date, t.type, t.amount, t.category, accountName(t.accountId) || '', (t.note||'').replace(/\n/g,' ')]));
  const csv = rows.map(r => r.map(c => `"${String(c).replace(/"/g,'""')}"`).join(',')).join('\n');
  downloadBlob(new Blob([csv], { type: 'text/csv' }), `transactions-${todayISO()}.csv`);
}
function importJSON(e) {
  const f = e.target.files[0]; if (!f) return;
  const r = new FileReader();
  r.onload = () => {
    try {
      const obj = JSON.parse(r.result);
      if (!confirm('Replace current data with imported file?')) return;
      state = { ...defaultState(), ...obj };
      save(); render(); toast('Imported');
    } catch { toast('Invalid file'); }
  };
  r.readAsText(f);
}
function importCashew(e) {
  const f = e.target.files[0]; if (!f) return;
  const r = new FileReader();
  r.onload = () => {
    try {
      if (!window.Cashew) { toast('Importer not loaded'); return; }
      const { txns, skipped } = window.Cashew.parseCashew(r.result, state);
      if (!txns.length) { toast('No valid rows'); return; }
      if (!confirm(`Append ${txns.length} transactions from Cashew? (${skipped} skipped)`)) return;
      const { added, dup } = window.Cashew.apply(state, txns);
      save(); render();
      toast(`Imported ${added} · ${dup} duplicates skipped`);
    } catch (err) { toast(err.message || 'Import failed'); }
  };
  r.readAsText(f);
}
function downloadBlob(blob, name) {
  const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = name;
  document.body.appendChild(a); a.click(); a.remove();
}

// Reminder (simple in-page scheduler, only fires while app is open)
let reminderTimer;
function scheduleReminder() {
  clearTimeout(reminderTimer);
  if (!('Notification' in window) || Notification.permission !== 'granted') return;
  const [h, m] = (state.settings.reminderTime || '21:00').split(':').map(Number);
  const now = new Date();
  const next = new Date(); next.setHours(h, m, 0, 0);
  if (next <= now) next.setDate(next.getDate()+1);
  reminderTimer = setTimeout(() => {
    try { new Notification('Productivity', { body: 'Time to check in on habits, supplements & tasks.' }); } catch {}
    scheduleReminder();
  }, next - now);
}
scheduleReminder();

// ---------- Helpers ----------
function escapeHTML(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function escapeAttr(s) { return escapeHTML(s); }

// Initial render
render();
