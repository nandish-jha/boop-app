function renderMock(phone, meta, surface) {
  phone.innerHTML =
    (meta ? '<div class="mock-meta">' + meta + '</div>' : '') +
    (surface ? '<div class="mock-surface-tag">' + surface + '</div>' : '') +
    '<div class="mock-top">' +
      '<div class="mock-greeting"><h2>Good evening</h2><p>Wednesday, June 24</p></div>' +
      '<div class="mock-icons">' +
        '<div class="icon-btn filled">⌕</div>' +
        '<div class="icon-btn ghost">⚙</div>' +
      '</div>' +
    '</div>' +
    '<div class="quote">' +
      '<blockquote>"Hope is a discipline. Keep showing up."</blockquote>' +
      '<cite>— Unknown</cite>' +
    '</div>' +
    '<div class="stats">' +
      '<div class="stat"><div class="label">Today</div><div class="value">3</div><div class="cap">tasks due</div></div>' +
      '<div class="stat"><div class="label">Habits</div><div class="value">2/4</div><div class="cap">checked in</div></div>' +
      '<div class="stat"><div class="label">Balance</div><div class="value">1,240</div><div class="cap">CAD</div></div>' +
    '</div>' +
    '<div class="section"><span class="bar"></span><h3>Up next</h3></div>' +
    '<div class="task"><div class="task-body"><strong>Review quarterly notes</strong><span>Today · 6:00 PM</span></div><div class="check"></div></div>' +
    '<div class="task"><div class="task-body"><strong>Call dentist</strong><span>Tomorrow · 9:30 AM</span></div><div class="check"></div></div>' +
    '<span class="accent-btn">Week view</span>' +
    '<div class="ring-demo"><div class="ring"></div><span>App icon / launch ring</span></div>' +
    '<div class="nav-preview">' +
      '<div class="nav-item active">Home</div>' +
      '<div class="nav-item">Tasks</div>' +
      '<div class="nav-fab">+</div>' +
      '<div class="nav-item">Calendar</div>' +
      '<div class="nav-item">Accounts</div>' +
    '</div>';
}

function initPreviews() {
  document.querySelectorAll('.phone').forEach(function (phone) {
    var wrap = phone.closest('.phone-wrap');
    var meta = wrap && wrap.getAttribute('data-tweak') ? wrap.getAttribute('data-tweak') : '';
    var surface = wrap && wrap.getAttribute('data-surface') ? wrap.getAttribute('data-surface') : '';
    renderMock(phone, meta, surface);
  });
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initPreviews);
} else {
  initPreviews();
}
