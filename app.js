// Local-only storage scaffolding
const STORAGE_KEY = 'nandish.productivity.v1';
const uid = () => Math.random().toString(36).slice(2, 10) + Date.now().toString(36).slice(-4);

const defaultState = () => ({
  tasks: [],
  settings: {}
});

let state = load();
function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return defaultState();
    return { ...defaultState(), ...JSON.parse(raw) };
  } catch { return defaultState(); }
}
function save() { localStorage.setItem(STORAGE_KEY, JSON.stringify(state)); }

let currentTab = 'home';
const view = () => document.getElementById('view');
const title = t => document.getElementById('pageTitle').textContent = t;

function escapeHTML(s){return String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));}

function renderTasks() {
  title('Tasks');
  const tasks = state.tasks;
  view().innerHTML = `
    <button id="add" class="card" style="width:100%; text-align:left; cursor:pointer;">+ New task</button>
    ${tasks.length === 0 ? '<div class="card">No tasks yet.</div>' : tasks.map(t => `
      <div class="card" data-id="${t.id}">
        <label><input type="checkbox" data-check ${t.done?'checked':''}> ${escapeHTML(t.title)}</label>
      </div>
    `).join('')}
  `;
  document.getElementById('add').onclick = () => {
    const title = prompt('Task title?');
    if (title) { state.tasks.push({ id: uid(), title, done: false }); save(); render(); }
  };
  document.querySelectorAll('[data-check]').forEach(el => el.onchange = () => {
    const id = el.closest('[data-id]').dataset.id;
    const t = state.tasks.find(x => x.id === id); t.done = el.checked; save();
  });
}

function render() {
  if (currentTab === 'home') { title('Home'); view().innerHTML = `<div class="card"><b>${state.tasks.length}</b> tasks saved.</div>`; }
  else if (currentTab === 'tasks') renderTasks();
  else { title(currentTab[0].toUpperCase()+currentTab.slice(1)); view().innerHTML = '<div class="card">Coming soon.</div>'; }
}

document.querySelectorAll('.nav-btn').forEach(b => b.addEventListener('click', () => {
  document.querySelectorAll('.nav-btn').forEach(x => x.classList.remove('active'));
  b.classList.add('active');
  currentTab = b.dataset.tab;
  render();
}));

render();
