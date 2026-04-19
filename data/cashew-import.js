/* Cashew CSV importer — parses export from the Cashew budget app and appends
   transactions to ProDash. Handles quoted multi-line notes, account/category
   mapping, transfers, and auto-creating missing accounts. Local-only. */
(function () {
  'use strict';

  // ---- Minimal RFC-4180 CSV parser (handles "quoted, fields\nwith newlines") ----
  function parseCSV(text) {
    const rows = [];
    let field = '';
    let row = [];
    let i = 0;
    let inQuotes = false;
    while (i < text.length) {
      const c = text[i];
      if (inQuotes) {
        if (c === '"') {
          if (text[i + 1] === '"') { field += '"'; i += 2; continue; }
          inQuotes = false; i++; continue;
        }
        field += c; i++; continue;
      }
      if (c === '"') { inQuotes = true; i++; continue; }
      if (c === ',') { row.push(field); field = ''; i++; continue; }
      if (c === '\r') { i++; continue; }
      if (c === '\n') { row.push(field); rows.push(row); row = []; field = ''; i++; continue; }
      field += c; i++;
    }
    if (field.length || row.length) { row.push(field); rows.push(row); }
    return rows.filter(r => r.length > 1 || (r.length === 1 && r[0] !== ''));
  }

  // Cashew account code -> existing ProDash account id (from seed).
  // Fallback: look up by fuzzy name; auto-create if missing.
  const ACCOUNT_ALIAS = {
    'SB Chequing':        'a1',
    'SB Savings':         'a2',
    'SB TFSA':            'a3',
    'SB TFSA (MFs)':      'a3',
    'SB Credit Card':     'a4',
    'TD Chequing':        'a5',
    'TD Savings':         'a6',
    'TD Everyday Savings':'a6',
    'TD TFSA':            'a7',
    'TD TFSA (MFs)':      'a7',
    'TD Credit Card':     'a8',
    'ACU Chequing':       'a9',
    'Affinity Chequing':  'a9',
    'ACU Savings':        'a10',
    'Affinity Savings':   'a10',
    'ACU TFSA':           'a11',
    'Affinity TFSA':      'a11',
    'WS Chequing':        'a12',
    'WS TFSA':            'a13',
    'WS TFSA (ETFs)':     'a13'
  };

  // Cashew category -> ProDash category (seed list in data/seed.js)
  const CATEGORY_MAP = {
    'Dining':            'Food',
    'Groceries':         'Groceries',
    'Bills & Fees':      'Utilities',
    'Entertainment':     'Hanging out with friends',
    'Shopping':          'Shopping',
    'Transit':           'Transport',
    'Travel':            'Transport',
    'Gifts':             'Other',
    'Beauty':            'Shopping',
    'Donation':          'Other',
    'Student Funding':   'Other',
    'Income':            'Other',
    'Refund':            'Other',
    'Interest Charge':   'Utilities',
    'Payments':          'Other',
    'E Transfer':        'Transfer',
    'Balance Correction':'Other'
  };

  function accountIdFor(code, state) {
    if (!code) return null;
    const trimmed = code.trim();
    if (ACCOUNT_ALIAS[trimmed]) return ACCOUNT_ALIAS[trimmed];
    // fuzzy search by name
    const hit = state.accounts.find(a =>
      a.name.toLowerCase() === trimmed.toLowerCase() ||
      a.name.toLowerCase().includes(trimmed.toLowerCase())
    );
    if (hit) return hit.id;
    // auto-create
    const bank = trimmed.startsWith('SB') ? 'Scotiabank'
      : trimmed.startsWith('TD') ? 'TD'
      : trimmed.startsWith('ACU') ? 'Affinity'
      : trimmed.startsWith('WS') ? 'Wealthsimple'
      : trimmed.startsWith('Coinbase') ? 'Crypto'
      : trimmed.startsWith('PCEX') ? 'Crypto'
      : trimmed.startsWith('Loblaw') ? 'Employer'
      : 'Other';
    const type = /credit|mastercard|visa/i.test(trimmed) ? 'credit'
      : /saving/i.test(trimmed) ? 'savings'
      : /tfsa/i.test(trimmed) ? 'tfsa'
      : /chequing|cash/i.test(trimmed) ? 'chequing' : 'other';
    const id = 'cx_' + trimmed.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '');
    state.accounts.push({ id, name: trimmed, type, bank, balance: 0 });
    ACCOUNT_ALIAS[trimmed] = id;
    return id;
  }

  function categoryFor(cashewCat) {
    if (!cashewCat) return 'Other';
    return CATEGORY_MAP[cashewCat.trim()] || 'Other';
  }

  // Normalize a single Cashew row into a ProDash transaction (or null to skip)
  function toTransaction(row, state) {
    const [account, amountStr, currency, title, note, dateStr, incomeFlag, , categoryName] = row;
    if (!account || !amountStr || !dateStr) return null;
    const amount = parseFloat(amountStr);
    if (isNaN(amount) || amount === 0) return null;
    const date = (dateStr || '').slice(0, 10);
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return null;

    const isIncome = (incomeFlag || '').toLowerCase() === 'true' ? true
      : (incomeFlag || '').toLowerCase() === 'false' ? false
      : amount > 0;

    const accountId = accountIdFor(account, state);
    if (!accountId) return null;

    const cat = categoryFor(categoryName);
    const noteParts = [];
    if (title) noteParts.push(title.trim());
    if (note) noteParts.push(note.trim().replace(/\s+/g, ' '));

    return {
      id: 'cx_' + date + '_' + Math.random().toString(36).slice(2, 9),
      date,
      type: isIncome ? 'income' : 'expense',
      amount: Math.abs(amount),
      category: cat,
      accountId,
      note: noteParts.join(' — ').slice(0, 200),
      source: 'cashew'
    };
  }

  // Parse the full CSV into a list of ProDash-shaped transactions
  function parseCashew(text, state) {
    const rows = parseCSV(text);
    if (!rows.length) return { txns: [], skipped: 0 };
    const header = rows[0].map(s => s.trim().toLowerCase());
    // Validate it looks like Cashew
    const expected = ['account','amount','currency','title','note','date','income'];
    const looksRight = expected.every(k => header.includes(k));
    if (!looksRight) throw new Error('This does not look like a Cashew export.');

    const txns = [];
    let skipped = 0;
    for (let i = 1; i < rows.length; i++) {
      const tx = toTransaction(rows[i], state);
      if (tx) txns.push(tx); else skipped++;
    }
    return { txns, skipped };
  }

  // Apply parsed transactions to state (append only — never replaces)
  function apply(state, txns) {
    const existing = new Set(state.transactions
      .filter(t => t.source === 'cashew')
      .map(t => t.date + '|' + t.amount + '|' + t.accountId + '|' + (t.note || '')));
    let added = 0, dup = 0;
    for (const tx of txns) {
      const key = tx.date + '|' + tx.amount + '|' + tx.accountId + '|' + (tx.note || '');
      if (existing.has(key)) { dup++; continue; }
      state.transactions.push(tx);
      existing.add(key);
      added++;
    }
    return { added, dup };
  }

  window.Cashew = { parseCSV, parseCashew, apply };
})();
