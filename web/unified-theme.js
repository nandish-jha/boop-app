export const TYPE_ICONS = {
  note: 'description',
  reminder: 'notifications',
  calendar: 'calendar_today',
  habit: 'local_fire_department',
  wallet: 'account_balance_wallet',
};

export const TYPE_LABELS = {
  note: 'Note',
  reminder: 'Reminder',
  calendar: 'Event',
  habit: 'Habit',
  wallet: 'Wallet',
};

export const HUES = {
  note: 85,
  reminder: 25,
  calendar: 250,
  habit: 150,
  wallet: 300,
};

export const WEEKDAY_LABELS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

export function typeColors(type, theme) {
  const h = HUES[type];
  if (theme === 'dark') {
    return {
      bg: `oklch(30% 0.045 ${h})`,
      border: `oklch(50% 0.1 ${h})`,
      accent: `oklch(80% 0.09 ${h})`,
    };
  }
  return {
    bg: `oklch(95% 0.035 ${h})`,
    border: `oklch(80% 0.09 ${h})`,
    accent: `oklch(45% 0.1 ${h})`,
  };
}

export function themeTokens(theme) {
  if (theme === 'dark') {
    return {
      mode: 'dark',
      pageBg: '#0c0b10',
      phoneBg: '#141210',
      surfaceBg: '#2e2b28',
      surfaceBorder: 'rgba(250,246,240,0.1)',
      text: '#faf6f0',
      muted: '#a8a098',
      topbarBg: 'rgba(20,18,16,0.85)',
      chipBg: '#3a3632',
      inputBg: '#242120',
      inputBorder: 'rgba(250,246,240,0.16)',
      sheetBg: '#2e2b28',
      overlay: 'rgba(0,0,0,0.55)',
      deleteColor: 'rgba(250,246,240,0.35)',
      positiveColor: 'oklch(78% 0.12 150)',
      negativeColor: 'oklch(78% 0.13 25)',
      sheetHandle: 'rgba(250,246,240,0.2)',
      toggleIcon: 'light_mode',
      chipOverlay: 'rgba(0,0,0,0.25)',
      accent: 'oklch(78% 0.09 55)',
    };
  }
  return {
    mode: 'light',
    pageBg: '#efe9df',
    phoneBg: '#fbf7f1',
    surfaceBg: '#fffdf9',
    surfaceBorder: 'rgba(26,22,18,0.08)',
    text: '#1a1612',
    muted: '#8a8278',
    topbarBg: 'rgba(251,247,241,0.92)',
    chipBg: '#ede6dc',
    inputBg: '#fbf7f1',
    inputBorder: 'rgba(26,22,18,0.12)',
    sheetBg: '#fffdf9',
    overlay: 'rgba(26,22,18,0.35)',
    deleteColor: 'rgba(26,22,18,0.35)',
    positiveColor: 'oklch(42% 0.1 150)',
    negativeColor: 'oklch(45% 0.12 25)',
    sheetHandle: 'rgba(26,22,18,0.15)',
    toggleIcon: 'dark_mode',
    chipOverlay: 'rgba(255,255,255,0.5)',
    accent: 'oklch(45% 0.1 55)',
  };
}

export function greetingForHour(h = new Date().getHours()) {
  if (h < 12) return 'Good morning.';
  if (h < 17) return 'Good afternoon.';
  return 'Good evening.';
}

export function fmtMoney(n) {
  const sign = n < 0 ? '-' : '+';
  return `${sign}$${Math.abs(n).toFixed(2)}`;
}

export function stripHtml(html) {
  const d = document.createElement('div');
  d.innerHTML = html || '';
  return (d.textContent || '').trim();
}

export function fmtDue(ms) {
  if (!ms) return 'No date set';
  const d = new Date(ms);
  const now = new Date();
  const sameDay = d.toDateString() === now.toDateString();
  const tomorrow = new Date(now);
  tomorrow.setDate(tomorrow.getDate() + 1);
  const isTomorrow = d.toDateString() === tomorrow.toDateString();
  const time = d.toLocaleTimeString(undefined, { hour: 'numeric', minute: '2-digit' });
  if (sameDay) return `Today · ${time}`;
  if (isTomorrow) return `Tomorrow · ${time}`;
  return d.toLocaleString(undefined, { weekday: 'short', month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' });
}
