(function () {
  function esc(s) {
    if (s == null) return '';
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function setText(id, text) {
    var el = document.getElementById(id);
    if (el) el.textContent = text;
  }

  function setHtml(id, html) {
    var el = document.getElementById(id);
    if (el) el.innerHTML = html;
  }

  function taskIcon(done) {
    if (done) {
      return '<span class="material-symbols-outlined text-primary" style="font-variation-settings: &quot;FILL&quot; 1;">check_circle</span>';
    }
    return '<span class="material-symbols-outlined text-outline">radio_button_unchecked</span>';
  }

  function taskRow(t) {
    var strike = t.done ? ' line-through text-outline' : '';
    var pri =
      t.priority === 'high'
        ? '<span class="text-[10px] uppercase tracking-tighter bg-error/10 text-error px-2 py-0.5 rounded">High Priority</span>'
        : '';
    return (
      '<div class="bg-surface-container-low p-5 flex items-center gap-4 group cursor-pointer hover:bg-surface-bright transition-colors flex-wrap" onclick="ProDash.toggleTask(\'' +
      esc(t.id) +
      "')\">" +
      taskIcon(t.done) +
      '<span class="text-sm font-medium flex-1 min-w-0' +
      strike +
      '">' +
      esc(t.title) +
      '</span>' +
      pri +
      '<button type="button" class="text-xs text-primary uppercase font-bold px-2 py-1 shrink-0" onclick="event.stopPropagation();ProDash.openEditor(\'task\',\'' +
      esc(t.id) +
      "')\">Edit</button></div>"
    );
  }

  function hubCardTask(t) {
    var done = t.done;
    var icon = done
      ? '<span class="material-symbols-outlined text-outline">check_circle</span>'
      : '<span class="material-symbols-outlined text-primary" data-weight="fill">radio_button_unchecked</span>';
    var titleCls = done ? 'text-base font-semibold text-outline line-through editorial-text' : 'text-base font-semibold text-white editorial-text';
    var pri =
      t.priority === 'high' && !done
        ? '<span class="text-[10px] uppercase tracking-tighter bg-error/10 text-error px-2 py-0.5 rounded">High Priority</span>'
        : '';
    return (
      '<div class="group bg-surface-container-low p-5 rounded-xl border border-transparent hover:border-outline-variant/30 transition-all flex items-start gap-5" onclick="ProDash.toggleTask(\'' +
      esc(t.id) +
      "')\">" +
      '<div class="mt-1">' +
      icon +
      '</div><div class="flex-1 space-y-1"><div class="flex justify-between items-start gap-2"><h4 class="' +
      titleCls +
      '">' +
      esc(t.title) +
      '</h4>' +
      pri +
      '<button type="button" class="text-xs text-primary uppercase font-bold shrink-0" onclick="event.stopPropagation();ProDash.openEditor(\'task\',\'' +
      esc(t.id) +
      "')\">Edit</button></div><p class="text-sm text-on-surface-variant line-clamp-1">' +
      esc(t.type) +
      ' · tap to toggle</p></div></div>'
    );
  }

  function hubCardNote(n) {
    return (
      '<div class="group bg-surface-container-low p-5 rounded-xl border border-transparent hover:border-outline-variant/30 transition-all flex items-start gap-5 cursor-pointer" onclick="ProDash.openEditor(\'note\',\'' +
      esc(n.id) +
      "')\">" +
      '<div class="mt-1"><span class="material-symbols-outlined text-outline-variant">notes</span></div>' +
      '<div class="flex-1 space-y-1"><div class="flex justify-between items-start"><h4 class="text-base font-semibold text-white editorial-text">' +
      esc(n.title) +
      '</h4><span class="text-[10px] uppercase tracking-tighter bg-surface-container-highest text-outline px-2 py-0.5 rounded">' +
      esc(n.tag) +
      '</span></div><p class="text-sm text-on-surface-variant italic">&quot;' +
      esc(n.body) +
      '&quot;</p><p class="text-[10px] text-primary uppercase pt-1">Tap to edit</p></div></div>'
    );
  }

  function accountCard(a) {
    var border = a.accent ? ' border-l-2 border-primary' : '';
    var tag = a.tag
      ? '<span class="text-[10px] bg-surface-container-highest px-2 py-0.5 rounded text-on-surface-variant font-bold uppercase tracking-wider">' +
        esc(a.tag) +
        '</span>'
      : '';
    return (
      '<div class="bg-surface-container-low p-6 flex flex-col justify-between h-48 hover:bg-surface-bright transition-all duration-300 group cursor-pointer' +
      border +
      '" onclick="ProDash.openEditor(\'account\',\'' +
      esc(a.id) +
      "')\">" +
      '<div class="flex justify-between items-start"><span class="material-symbols-outlined text-outline">account_balance</span>' +
      tag +
      '</div><div><h3 class="text-outline text-xs uppercase tracking-widest font-medium mb-1">' +
      esc(a.title) +
      '</h3><p class="text-2xl font-manrope font-bold text-white">' +
      esc(a.balance) +
      '</p><p class="text-[10px] text-outline mt-1">' +
      esc(a.subtitle) +
      '</p><p class="text-[10px] text-primary uppercase mt-2">Tap to edit</p></div></div>'
    );
  }

  function txnRow(x) {
    return (
      '<div class="group flex items-center justify-between p-4 bg-transparent hover:bg-surface-container-low transition-colors cursor-pointer" onclick="ProDash.openEditor(\'transaction\',\'' +
      esc(x.id) +
      "')\">" +
      '<div class="flex items-center gap-4"><div class="w-10 h-10 bg-surface-container-low flex items-center justify-center rounded">' +
      '<span class="material-symbols-outlined text-sm">payments</span></div><div><p class="text-sm font-semibold text-white">' +
      esc(x.title) +
      '</p><p class="text-[10px] text-outline uppercase font-medium tracking-wider">' +
      esc(x.meta) +
      '</p></div></div><div class="text-right"><p class="text-sm font-bold text-white">' +
      esc(x.amount) +
      '</p><p class="text-[10px] text-outline uppercase font-medium">' +
      esc(x.account) +
      '</p></div></div>'
    );
  }

  function goalCard(g) {
    return (
      '<div class="bg-surface-container-low p-6 rounded-lg transition-all hover:bg-surface-bright cursor-pointer" onclick="ProDash.openEditor(\'goal\',\'' +
      esc(g.id) +
      "')\">" +
      '<div class="flex justify-between items-start mb-6"><div><p class="text-xs text-outline mb-1">' +
      esc(g.target) +
      '</p><h4 class="font-headline font-bold text-white uppercase tracking-wider text-sm">' +
      esc(g.title) +
      '</h4></div></div>' +
      '<div class="space-y-2"><div class="flex justify-between text-[10px] text-outline font-bold"><span>PROGRESS</span><span>' +
      g.progress +
      '%</span></div><div class="h-1 bg-surface-container-highest"><div class="h-full bg-white" style="width:' +
      g.progress +
      '%"></div></div></div>' +
      '<p class="text-[10px] text-primary uppercase mt-3">Tap to edit</p></div>'
    );
  }

  function habitRow(h) {
    var label = h.doneToday ? 'Undo' : 'Done today';
    return (
      '<div class="flex flex-wrap items-center gap-2 p-3 bg-surface-container-low rounded-lg mb-2">' +
      '<span class="text-sm text-white flex-1 min-w-[120px]">' +
      esc(h.name) +
      '</span>' +
      '<button type="button" class="text-xs text-primary font-bold uppercase px-2 py-1 border border-primary/40 rounded" onclick="ProDash.toggleHabitToday(\'' +
      esc(h.id) +
      "')\">" +
      label +
      '</button>' +
      '<button type="button" class="text-xs text-outline font-bold uppercase px-2 py-1" onclick="ProDash.openEditor(\'habit\',\'' +
      esc(h.id) +
      "')\">Edit</button></div>"
    );
  }

  function homeSupplementTile(s) {
    return (
      '<div class="bg-surface-container-lowest p-6 border-l-2 border-primary/20 cursor-pointer" onclick="ProDash.openEditor(\'supplement\',\'' +
      esc(s.id) +
      "')\">" +
      '<div class="flex justify-between items-start mb-4"><span class="material-symbols-outlined text-outline-variant">pill</span>' +
      '<span class="text-[10px] font-label text-outline uppercase">' +
      esc(s.dose) +
      '</span></div><p class="font-bold text-sm">' +
      esc(s.name) +
      '</p><p class="text-[10px] text-primary uppercase mt-2">Tap to edit</p></div>'
    );
  }

  function morningTile(m) {
    var icon = m.taken
      ? '<span class="material-symbols-outlined text-primary text-sm" style="font-variation-settings: &quot;FILL&quot; 1;">check_circle</span>'
      : '<span class="material-symbols-outlined text-outline-variant text-sm">radio_button_unchecked</span>';
    var btn = m.taken ? 'Mark not taken' : 'Mark taken';
    return (
      '<div class="p-6 bg-surface-container-low hover:bg-surface-bright transition-colors border-l-2 border-primary">' +
      '<div class="flex justify-between items-start mb-4"><span class="font-headline font-bold text-lg">' +
      esc(m.name) +
      '</span>' +
      icon +
      '</div>' +
      '<p class="text-xs text-outline uppercase tracking-widest mb-3">' +
      esc(m.detail) +
      '</p>' +
      '<div class="flex flex-wrap gap-2">' +
      '<button type="button" class="text-xs text-primary font-bold uppercase" onclick="event.stopPropagation();ProDash.toggleSupplementLog(\'' +
      esc(m.id) +
      "')\">" +
      btn +
      '</button>' +
      '<button type="button" class="text-xs text-outline font-bold uppercase" onclick="event.stopPropagation();ProDash.openEditor(\'supplement\',\'' +
      esc(m.id) +
      "')\">Edit</button></div></div>"
    );
  }

  window.ProDashHydrate = function (data) {
    if (!data || !data.page) return;
    if (data.page === 'home') {
      setText('prodash-date', data.date || '');
      setText('prodash-welcome', data.welcome || '');
      setText('prodash-vault-whole', data.vaultWhole || '');
      setText('prodash-vault-cents', data.vaultCents || '');
      setText('prodash-vault-trend', data.vaultTrend || '');
      setText('prodash-cadence', data.cadencePct || '');
      setText('prodash-focus-meta', data.focusMeta || '');
      if (data.tasks && document.getElementById('prodash-task-list')) {
        setHtml('prodash-task-list', data.tasks.map(taskRow).join(''));
      }
      if (data.supplements && document.getElementById('prodash-supplements')) {
        setHtml('prodash-supplements', data.supplements.map(homeSupplementTile).join(''));
      }
    } else if (data.page === 'hub') {
      if (data.stream && document.getElementById('prodash-stream')) {
        var html = data.stream
          .map(function (item) {
            if (item.kind === 'task') return hubCardTask(item);
            if (item.kind === 'note') return hubCardNote(item);
            return '';
          })
          .join('');
        setHtml('prodash-stream', html);
      }
    } else if (data.page === 'goals') {
      setText('prodash-habits-meta', data.habitsMeta || '');
      var bar = document.getElementById('prodash-pass-bar');
      if (bar && data.passPct != null) bar.style.width = data.passPct + '%';
      if (data.habits && document.getElementById('prodash-habits-list')) {
        setHtml('prodash-habits-list', data.habits.map(habitRow).join(''));
      }
      if (data.goals && document.getElementById('prodash-goals')) {
        setHtml('prodash-goals', data.goals.map(goalCard).join(''));
      }
    } else if (data.page === 'vault') {
      setText('prodash-net-whole', data.netWhole || '');
      setText('prodash-net-cents', data.netCents || '');
      setText('prodash-trend', data.trend || '');
      if (data.accounts && document.getElementById('prodash-accounts')) {
        setHtml('prodash-accounts', data.accounts.map(accountCard).join(''));
      }
      if (data.transactions && document.getElementById('prodash-activity')) {
        setHtml('prodash-activity', data.transactions.map(txnRow).join(''));
      }
    } else if (data.page === 'logs') {
      setText('prodash-completion', (data.completionPct != null ? data.completionPct : '') + '%');
      if (data.morning && document.getElementById('prodash-morning-grid')) {
        setHtml('prodash-morning-grid', data.morning.map(morningTile).join(''));
      }
    }
  };
})();
