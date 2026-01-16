// AMOLED Material 3 shell — bottom nav routing
let currentTab = 'home';
const view = () => document.getElementById('view');
const title = t => document.getElementById('pageTitle').textContent = t;

function render() {
  if (currentTab === 'home') { title('Home'); view().innerHTML = '<div class="card">Welcome.</div>'; }
  else if (currentTab === 'tasks') { title('Tasks'); view().innerHTML = '<div class="card">Tasks coming soon.</div>'; }
  else if (currentTab === 'habits') { title('Habits'); view().innerHTML = '<div class="card">Habits coming soon.</div>'; }
  else if (currentTab === 'budget') { title('Budget'); view().innerHTML = '<div class="card">Budget coming soon.</div>'; }
  else { title('More'); view().innerHTML = '<div class="card">More coming soon.</div>'; }
}

document.querySelectorAll('.nav-btn').forEach(b => b.addEventListener('click', () => {
  document.querySelectorAll('.nav-btn').forEach(x => x.classList.remove('active'));
  b.classList.add('active');
  currentTab = b.dataset.tab;
  render();
}));

render();
