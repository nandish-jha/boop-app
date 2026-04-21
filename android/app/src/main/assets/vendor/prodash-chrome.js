(function () {
  function wireClick(id, fn) {
    var el = document.getElementById(id);
    if (!el) return;
    el.onclick = function (e) {
      if (e) {
        e.preventDefault();
        e.stopPropagation();
      }
      try {
        fn();
      } catch (x) {}
    };
    el.style.cursor = 'pointer';
  }

  window.ProDashFilter = function (q) {
    q = (q || '').trim().toLowerCase();
    function filterChildren(rootId) {
      var root = document.getElementById(rootId);
      if (!root) return false;
      var kids = root.children;
      for (var i = 0; i < kids.length; i++) {
        var child = kids[i];
        var t = (child.textContent || '').toLowerCase();
        child.style.display = !q || t.indexOf(q) >= 0 ? '' : 'none';
      }
      return true;
    }
    if (filterChildren('prodash-stream')) return;
    if (filterChildren('prodash-task-list')) return;
    if (filterChildren('prodash-goals')) return;
    if (filterChildren('prodash-habits-list')) return;
    if (filterChildren('prodash-activity')) return;
    if (filterChildren('prodash-morning-grid')) return;
  };

  window.ProDashWireChrome = function () {
    wireClick('prodash-btn-menu', function () {
      ProDash.openMenu();
    });
    wireClick('prodash-btn-search', function () {
      ProDash.openSearch();
    });
    wireClick('prodash-btn-notifications', function () {
      ProDash.toast('No new notifications');
    });
    wireClick('prodash-btn-profile', function () {
      ProDash.navigate('settings');
    });
    wireClick('prodash-btn-deposit', function () {
      ProDash.navigate('vault');
    });
    wireClick('prodash-btn-details', function () {
      ProDash.navigate('vault');
    });
    wireClick('prodash-btn-view-logs', function () {
      ProDash.navigate('logs');
    });
    wireClick('prodash-btn-goal-settings', function () {
      ProDash.navigate('settings');
    });
    wireClick('prodash-btn-add-account', function () {
      ProDash.openEditor('account', '');
    });
    wireClick('prodash-btn-add-goal', function () {
      ProDash.openEditor('goal', '');
    });
    wireClick('prodash-btn-add-habit', function () {
      ProDash.openEditor('habit', '');
    });
    wireClick('prodash-btn-add-supplement-log', function () {
      ProDash.openEditor('supplement', '');
    });
    wireClick('prodash-btn-add-transaction', function () {
      ProDash.openEditor('transaction', '');
    });
    wireClick('prodash-btn-hub-create', function () {
      ProDash.openStreamCreate();
    });
    wireClick('prodash-logs-fab', function () {
      ProDash.navigate('hub');
    });
    wireClick('prodash-btn-sign-out', function () {
      ProDash.toast('You stay signed in on this device. Data is stored only here.');
    });
    wireClick('prodash-btn-reminder', function () {
      ProDash.openEditor('reminder', '');
    });

    function wireToggle(id, settingKey) {
      var el = document.getElementById(id);
      if (!el) return;
      el.onchange = function () {
        try {
          ProDash.setSetting(settingKey, el.checked ? 'true' : 'false');
        } catch (x) {}
      };
    }
    wireToggle('prodash-toggle-obsidian', 'obsidianMode');
    wireToggle('prodash-toggle-haptics', 'hapticsEnabled');

    var hubInp = document.getElementById('prodash-hub-filter');
    if (hubInp) {
      hubInp.addEventListener('input', function () {
        if (window.ProDashFilter) window.ProDashFilter(hubInp.value || '');
      });
    }
  };
})();
