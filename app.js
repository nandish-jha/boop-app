const STORAGE_KEY = 'nandish.productivity.v1';
const uid = () => Math.random().toString(36).slice(2, 10) + Date.now().toString(36).slice(-4);
const todayISO = (d = new Date()) => d.toISOString().slice(0, 10);

const defaultState = () => ({ tasks: [], habits: SEED.habits.map(h=>({...h})), habitLogs: {}, accounts: SEED.accounts.map(a=>({...a,balance:0})), categories: [...SEED.budgetCategories], transactions: [], budget: { monthlySavingsGoal: 500, monthlyBudget: 0 }, supplements: SEED.supplements.map(s=>({...s})), supplementLogs: {}, skincare: SEED.skincare.map(s=>({...s})), skincareLogs: {}, notes: [], goals: [], workouts: SEED.workouts.map(w=>({...w})), settings: {} });
let state = load();
function load() {
  try { const raw = localStorage.getItem(STORAGE_KEY); if (!raw) return defaultState(); return { ...defaultState(), ...JSON.parse(raw) }; }
  catch { return defaultState(); }
}
function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }
function escapeHTML(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}
function escapeAttr(s){return escapeHTML(s);}

let currentTab = 'home'; let moreSub = null;
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
  else if (currentTab === 'budget') { setTitle('Budget'); renderBudget(v); }
  else if (currentTab === 'more') {
    if (!moreSub) { setTitle('More'); renderMore(v); }
    else if (moreSub === 'supplements') { setTitle('Supplements'); renderSupplements(v); }
    else if (moreSub === 'skincare') { setTitle('Skincare'); renderSkincare(v); }
    else if (moreSub === 'notes') { setTitle('Notes'); renderNotes(v); }
    else if (moreSub === 'goals') { setTitle('Goals'); renderGoals(v); }
    else { setTitle('More'); renderMore(v); }
  }
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

const fmtMoney = n => (n < 0 ? '-' : '') + '$' + Math.abs(n).toFixed(2);
function accountName(id){ return (state.accounts.find(a=>a.id===id)||{}).name || ''; }

function renderBudget(root) {
  const monthStart = todayISO().slice(0,7) + '-01';
  const txns = state.transactions.filter(x => x.date >= monthStart);
  const income = txns.filter(x=>x.type==='income').reduce((a,b)=>a+b.amount,0);
  const expense = txns.filter(x=>x.type==='expense').reduce((a,b)=>a+b.amount,0);
  const saved = income - expense;

  const byCat = {};
  txns.filter(x=>x.type==='expense').forEach(x => { byCat[x.category] = (byCat[x.category]||0) + x.amount; });
  const cats = Object.entries(byCat).sort((a,b)=>b[1]-a[1]);
  const totalE = cats.reduce((a,b)=>a+b[1],0);
  const palette = ['#e8e8e8','#9a9a9a','#6e6e6e','#4a4a4a','#d0d0d0','#7ee787','#f0b72f','#ff9c9c','#9ed8ff'];
  let cum = 0;
  const pieStops = cats.map(([k,v],i) => {
    const f = (cum/totalE)*360; cum += v; const to = (cum/totalE)*360;
    return `${palette[i%palette.length]} ${f.toFixed(2)}deg ${to.toFixed(2)}deg`;
  }).join(', ');

  const days = [];
  for (let i = 6; i >= 0; i--) { const d = new Date(); d.setDate(d.getDate()-i); days.push(todayISO(d)); }
  const daily = days.map(d => state.transactions.filter(x=>x.date===d && x.type==='expense').reduce((a,b)=>a+b.amount,0));
  const maxD = Math.max(1, ...daily);
  root.innerHTML = `
    <div class="card"><div class="card-h">This month</div>
      <div>Income: <b>${fmtMoney(income)}</b></div>
      <div>Expense: <b>${fmtMoney(expense)}</b></div>
      <div>Saved: <b>${fmtMoney(saved)}</b> (goal ${fmtMoney(state.budget.monthlySavingsGoal)})</div>
    </div>
    <div class="card"><div class="card-h">Transactions</div>
      ${txns.length === 0 ? '<div>No transactions yet.</div>' : txns.slice().sort((a,b)=>b.date.localeCompare(a.date)).map(t => `
        <div style="display:flex;justify-content:space-between;padding:6px 0;border-bottom:1px dashed var(--border);">
          <div>${escapeHTML(t.category)} <span style="color:var(--text-3);font-size:12px">${escapeHTML(accountName(t.accountId))}</span></div>
          <div>${t.type==='income'?'+':'-'}${fmtMoney(t.amount)}</div>
        </div>`).join('')}
    </div>
    ${cats.length ? `<div class="card"><div class="card-h">Spending by category</div><div class="pie" style="background: conic-gradient(${pieStops});"></div><div class="legend">${cats.map(([k,v],i)=>`<span class="lg"><span class="dot" style="background:${palette[i%palette.length]}"></span>${k} · ${fmtMoney(v)}</span>`).join('')}</div></div>` : ''}
    <div class="card"><div class="card-h">Last 7 days</div>
      <div class="bar-chart">${daily.map((v,i)=>`<div class="bar" style="height:${(v/maxD)*100}%"><i>${days[i].slice(8)}</i></div>`).join('')}</div>
    </div>
    <button class="btn primary btn-block" id="addTxn">Add transaction</button>
  `;
  document.getElementById('addTxn').onclick = () => editTxn();
}
function editTxn(id) {
  const tx = id ? state.transactions.find(x=>x.id===id) : { id: uid(), type:'expense', amount:0, category: state.categories[0], accountId: state.accounts[0].id, date: todayISO(), note:'' };
  openSheet(`
    <h2 style="margin:0 0 12px;">${id ? 'Edit' : 'New'} transaction</h2>
    <div class="field"><label>Type</label><select class="select" id="xt">
      <option value="expense" ${tx.type==='expense'?'selected':''}>Expense</option>
      <option value="income" ${tx.type==='income'?'selected':''}>Income</option></select></div>
    <div class="field"><label>Amount</label><input type="number" step="0.01" class="input" id="xa" value="${tx.amount||''}"></div>
    <div class="field"><label>Category</label><select class="select" id="xc">
      ${state.categories.map(c=>`<option ${c===tx.category?'selected':''}>${c}</option>`).join('')}
    </select></div>
    <div class="field"><label>Account</label><select class="select" id="xac">
      ${state.accounts.map(a=>`<option value="${a.id}" ${a.id===tx.accountId?'selected':''}>${a.name}</option>`).join('')}
    </select></div>
    <div class="field"><label>Date</label><input type="date" class="input" id="xd" value="${tx.date}"></div>
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelector('#sv').onclick = () => {
      tx.type = root.querySelector('#xt').value;
      tx.amount = parseFloat(root.querySelector('#xa').value) || 0;
      tx.category = root.querySelector('#xc').value;
      tx.accountId = root.querySelector('#xac').value;
      tx.date = root.querySelector('#xd').value;
      if (!id) state.transactions.push(tx);
      save(); closeSheet(); render();
    };
  });
}

function renderMore(root) {
  root.innerHTML = `
    <button class="card btn-block" style="text-align:left;cursor:pointer;" data-k="supplements">💊 Supplements</button>
    <button class="card btn-block" style="text-align:left;cursor:pointer;" data-k="skincare">✦ Skincare</button>
    <button class="card btn-block" style="text-align:left;cursor:pointer;" data-k="notes">📝 Notes</button>
    <button class="card btn-block" style="text-align:left;cursor:pointer;" data-k="goals">★ Goals</button>
  `;
  root.querySelectorAll('[data-k]').forEach(b => b.onclick = () => { moreSub = b.dataset.k; render(); });
}
function renderSupplements(root) {
  const t = todayISO();
  const log = state.supplementLogs[t] || {};
  root.innerHTML = `
    <button class="btn" id="back">← Back</button>
    <div style="height:10px"></div>
    ${['morning','workout','night'].map(g => `
      <div class="card"><div class="card-h">${g}</div>
        ${state.supplements.filter(s => s.time === g).map(s => `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;">
            <div><div>${escapeHTML(s.name)}</div><div style="color:var(--text-3);font-size:12px">${escapeHTML(s.dose||'')}</div></div>
            <button class="check ${log[s.id]?'done':''}" data-ls="${s.id}"></button></div>
        `).join('')}
      </div>`).join('')}
  `;
  root.querySelector('#back').onclick = () => { moreSub = null; render(); };
  root.querySelectorAll('[data-ls]').forEach(b => b.onclick = () => {
    if (!state.supplementLogs[t]) state.supplementLogs[t] = {};
    state.supplementLogs[t][b.dataset.ls] = !state.supplementLogs[t][b.dataset.ls];
    save(); render();
  });
}

function renderSkincare(root) {
  const t = todayISO(); const log = state.skincareLogs[t] || {};
  root.innerHTML = `
    <button class="btn" id="back">← Back</button><div style="height:10px"></div>
    ${['morning','night'].map(g => `
      <div class="card"><div class="card-h">${g}</div>
        ${state.skincare.filter(s => s.time === g).map(s => `
          <div style="display:flex;justify-content:space-between;padding:8px 0;">
            <span>${escapeHTML(s.name)}</span>
            <button class="check ${log[s.id]?'done':''}" data-sk="${s.id}"></button></div>
        `).join('')}
      </div>`).join('')}
  `;
  root.querySelector('#back').onclick = () => { moreSub = null; render(); };
  root.querySelectorAll('[data-sk]').forEach(b => b.onclick = () => {
    if (!state.skincareLogs[t]) state.skincareLogs[t] = {};
    state.skincareLogs[t][b.dataset.sk] = !state.skincareLogs[t][b.dataset.sk];
    save(); render();
  });
}

function renderNotes(root) {
  root.innerHTML = `
    <button class="btn" id="back">← Back</button><div style="height:10px"></div>
    <button class="btn primary btn-block" id="addN">+ New note</button>
    ${state.notes.length === 0 ? '<div class="card">No notes yet.</div>' : state.notes.slice().sort((a,b)=>(b.updated||0)-(a.updated||0)).map(n => `
      <div class="card" data-n="${n.id}" style="cursor:pointer;">
        <div style="font-weight:600;">${escapeHTML(n.title||'Untitled')}</div>
        <div style="color:var(--text-2);font-size:13px;">${escapeHTML((n.body||'').slice(0,120))}</div>
      </div>`).join('')}
  `;
  root.querySelector('#back').onclick = () => { moreSub = null; render(); };
  root.querySelector('#addN').onclick = () => editNote();
  root.querySelectorAll('[data-n]').forEach(el => el.onclick = () => editNote(el.dataset.n));
}
function editNote(id) {
  const n = id ? state.notes.find(x=>x.id===id) : { id: uid(), title:'', body:'', tags:[], updated: Date.now() };
  openSheet(`
    <h2 style="margin:0 0 12px;">${id ? 'Edit note' : 'New note'}</h2>
    <div class="field"><label>Title</label><input class="input" id="nt" value="${escapeAttr(n.title)}"></div>
    <div class="field"><label>Tags (comma-separated)</label><input class="input" id="ng" value="${escapeAttr((n.tags||[]).join(', '))}"></div>
    <div class="field"><label>Body</label><textarea class="input" id="nb" style="min-height:90px;">${escapeHTML(n.body||'')}</textarea></div>
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelector('#sv').onclick = () => {
      n.title = root.querySelector('#nt').value.trim();
      n.body = root.querySelector('#nb').value;
      n.tags = root.querySelector('#ng').value.split(',').map(s=>s.trim()).filter(Boolean);
      n.updated = Date.now();
      if (!id) state.notes.push(n);
      save(); closeSheet(); render();
    };
  });
}

function renderGoals(root) {
  root.innerHTML = `
    <button class="btn" id="back">← Back</button><div style="height:10px"></div>
    <button class="btn primary btn-block" id="addG">+ New goal</button>
    ${state.goals.length === 0 ? '<div class="card">No goals yet.</div>' : state.goals.map(g => `
      <div class="card" data-g="${g.id}">
        <div style="display:flex;justify-content:space-between;"><b>${escapeHTML(g.title)}</b><span>${g.progress||0}%</span></div>
      </div>`).join('')}
  `;
  root.querySelector('#back').onclick = () => { moreSub = null; render(); };
  root.querySelector('#addG').onclick = () => editGoal();
  root.querySelectorAll('[data-g]').forEach(el => el.onclick = () => editGoal(el.dataset.g));
}
function editGoal(id) {
  const g = id ? state.goals.find(x=>x.id===id) : { id: uid(), title:'', target:'', deadline:'', progress:0 };
  openSheet(`
    <h2 style="margin:0 0 12px;">${id ? 'Edit goal' : 'New goal'}</h2>
    <div class="field"><label>Title</label><input class="input" id="gt" value="${escapeAttr(g.title)}"></div>
    <div class="field"><label>Progress %</label><input class="input" type="number" min="0" max="100" id="gp" value="${g.progress||0}"></div>
    <button class="btn primary btn-block" id="sv">Save</button>
  `, root => {
    root.querySelector('#sv').onclick = () => {
      g.title = root.querySelector('#gt').value.trim();
      g.progress = Math.min(100, Math.max(0, parseFloat(root.querySelector('#gp').value)||0));
      if (!id) state.goals.push(g);
      save(); closeSheet(); render();
    };
  });
}
