const STORAGE_KEY = 'nandish.productivity.v1';
const uid = () => Math.random().toString(36).slice(2, 10) + Date.now().toString(36).slice(-4);
const todayISO = (d = new Date()) => d.toISOString().slice(0, 10);

const defaultState = () => ({ tasks: [], habits: SEED.habits.map(h=>({...h})), habitLogs: {}, settings: {} });
let state = load();
function load() {
  try { const raw = localStorage.getItem(STORAGE_KEY); if (!raw) return defaultState(); return { ...defaultState(), ...JSON.parse(raw) }; }
  catch { return defaultState(); }
}
function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
function escapeHTML(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}
function escapeAttr(s){return escapeHTML(s);}

let currentTab = 'home';
const setTitle = t => document.getElementById('pageTitle').textContent = t;

// Sheet
const sheetEl = document.getElementById('sheet');
const sheetContent = document.getElementById('sheetContent');
function openSheet(html, onReady) {
  sheetContent.innerHTML = html;
  sheetEl.setAttribute('aria-hidden', 'false');
  if (onReady) onReady(sheetContent);
}
function closeSheet() { sheetEl.setAttribute('aria-hidden', 'true'); }
document.querySelectorAll('[data-close]').forEach(el => el.addEventListener('click', closeSheet));

function render() {
  const v = document.getElementById('view');
  v.innerHTML = '';
  if (currentTab === 'home') { setTitle('Home'); renderHome(v); }
  else if (currentTab === 'tasks') { setTitle('Tasks'); renderTasks(v); }
  else if (currentTab === 'habits') { setTitle('Habits'); renderHabits(v); }
  else { setTitle('More'); v.innerHTML = '<div class="card">Coming soon.</div>'; }
}

function renderHome(root) {
  const open = state.tasks.filter(t => !t.done).length;
  root.innerHTML = `<div class="card"><div class="card-h">Tasks</div><div>${open} open</div></div>`;
}

function renderTasks(root) {
  const sorted = state.tasks.slice().sort((a,b) => (a.done?1:0)-(b.done?1:0));
  root.innerHTML = sorted.length === 0
    ? '<div class="card">No tasks yet. Tap + to add one.</div>'
    : sorted.map(taskItem).join('');
  root.querySelectorAll('.item').forEach(el => {
    const id = el.dataset.id;
    el.querySelector('.check').onclick = () => { const t = state.tasks.find(x=>x.id===id); t.done = !t.done; save(); render(); };
    el.querySelector('[data-edit]').onclick = () => editTask(id);
  });
}
function taskItem(t) {
  const prChip = `<span class="chip ${t.priority==='high'?'high':t.priority==='medium'?'med':'low'}">${t.priority||'low'}</span>`;
  const dueChip = t.due ? `<span class="chip">${t.due}</span>` : '';
  const typeChip = t.type ? `<span class="chip">${t.type}</span>` : '';
  return `<div class="item" data-id="${t.id}">
    <button class="check ${t.done?'done':''}"></button>
    <div class="item-body">
      <div class="item-title ${t.done?'done':''}">${escapeHTML(t.title)}</div>
      <div class="item-meta">${prChip}${typeChip}${dueChip}</div>
    </div>
    <button data-edit style="background:transparent;border:none;color:var(--text-3);">✎</button>
  </div>`;
}

function editTask(id) {
  const t = id ? state.tasks.find(x=>x.id===id) : { id: uid(), title:'', priority:'medium', type:'personal', due:'', recurrence:'none', done:false };
  openSheet(`
    <h2 style="margin:0 0 12px;">${id ? 'Edit task' : 'New task'}</h2>
    <div class="field"><label>Title</label><input class="input" id="tt" value="${escapeAttr(t.title)}"></div>
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
    <div class="field"><label>Due date</label><input class="input" id="td" type="date" value="${t.due||''}"></div>
    <div class="field"><label>Recurrence</label><select class="select" id="tr"><option value="none" ${t.recurrence==='none'?'selected':''}>None</option><option value="daily" ${t.recurrence==='daily'?'selected':''}>Daily</option><option value="weekly" ${t.recurrence==='weekly'?'selected':''}>Weekly</option><option value="monthly" ${t.recurrence==='monthly'?'selected':''}>Monthly</option></select></div>
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelector('#sv').onclick = () => {
      t.title = root.querySelector('#tt').value.trim();
      if (!t.title) return;
      t.priority = root.querySelector('#tp').value;
      t.type = root.querySelector('#ty').value;
      t.due = root.querySelector('#td').value;
      t.recurrence = root.querySelector('#tr').value;
      if (!id) state.tasks.push(t);
      save(); closeSheet(); render();
    };
  });
}

document.getElementById('fab').onclick = () => { if (currentTab === 'tasks') editTask(); else { currentTab='tasks'; document.querySelectorAll('.nav-btn').forEach(b=>b.classList.toggle('active',b.dataset.tab==='tasks')); render(); editTask(); }};

document.querySelectorAll('.nav-btn').forEach(b => b.addEventListener('click', () => {
  document.querySelectorAll('.nav-btn').forEach(x => x.classList.remove('active'));
  b.classList.add('active');
  currentTab = b.dataset.tab;
  render();
}));
render();

function renderHabits(root) {
  const t = todayISO();
  const log = state.habitLogs[t] || {};
  root.innerHTML = `
    <div class="card"><div class="card-h">Today</div>
      ${state.habits.map(h => habitRow(h, log[h.id])).join('')}
    </div>
    <div class="card"><div class="card-h">Monthly heatmap</div>${heatmapHTML()}</div>
    <div class="card"><div class="card-h">Streaks</div>
      ${state.habits.map(h => `<div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px dashed var(--border);"><span>${h.name}</span><span style="color:var(--text-2)">🔥 ${streakOf(h)}</span></div>`).join('')}
    </div>
  `;
  root.querySelectorAll('[data-hc]').forEach(b => b.onclick = () => {
    if (!state.habitLogs[t]) state.habitLogs[t] = {};
    state.habitLogs[t][b.dataset.hc] = !state.habitLogs[t][b.dataset.hc];
    save(); render();
  });
  root.querySelectorAll('[data-hq]').forEach(i => i.onchange = () => {
    if (!state.habitLogs[t]) state.habitLogs[t] = {};
    state.habitLogs[t][i.dataset.hq] = parseFloat(i.value) || 0;
    save();
  });
}
function habitRow(h, val) {
  if (h.type === 'check') {
    return `<div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;">
      <span>${h.name}</span><button class="check ${val?'done':''}" data-hc="${h.id}"></button></div>`;
  }
  return `<div class="field"><label>${h.name} (${h.unit}, target ${h.target})</label>
    <input type="number" step="0.1" min="0" class="input" data-hq="${h.id}" value="${val??''}"></div>`;
}
function streakOf(h) {
  let s = 0; const d = new Date();
  for (let i=0;i<366;i++) {
    const k = todayISO(d);
    const log = state.habitLogs[k] || {};
    const v = log[h.id];
    const hit = h.type === 'check' ? !!v : (typeof v === 'number' && v >= h.target);
    if (hit) s++; else break;
    d.setDate(d.getDate()-1);
  }
  return s;
}

function heatmapHTML() {
  const cells = [];
  const today = new Date();
  const start = new Date(today); start.setDate(start.getDate() - 89);
  for (let i = 0; i < 90; i++) {
    const d = new Date(start); d.setDate(start.getDate()+i);
    const k = todayISO(d);
    const log = state.habitLogs[k] || {};
    const hits = state.habits.filter(h => {
      const v = log[h.id];
      return h.type === 'check' ? !!v : (typeof v === 'number' && v >= h.target);
    }).length;
    const r = state.habits.length ? hits/state.habits.length : 0;
    const lvl = r === 0 ? '' : r < .34 ? 'l1' : r < .67 ? 'l2' : r < 1 ? 'l3' : 'l4';
    cells.push(`<div class="cell ${lvl}" title="${k}: ${hits}/${state.habits.length}"></div>`);
  }
  return `<div class="heatmap">${cells.join('')}</div>`;
}
