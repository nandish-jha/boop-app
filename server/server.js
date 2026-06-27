require('dotenv').config();
const express = require('express');
const path = require('path');
const Database = require('better-sqlite3');
const Anthropic = require('@anthropic-ai/sdk');

const app = express();
const PORT = process.env.PORT || 3000;

// --- Database setup ---
const db = new Database('boop.db');
db.exec(`
  CREATE TABLE IF NOT EXISTS items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,       -- 'task' | 'note' | 'journal' | 'quote'
    content TEXT NOT NULL,
    title TEXT,
    area TEXT,                -- home | work | personal | etc.
    due_date TEXT,
    priority TEXT,            -- low | medium | high
    status TEXT DEFAULT 'open',
    raw_input TEXT,
    created_at TEXT DEFAULT (datetime('now','localtime')),
    updated_at TEXT DEFAULT (datetime('now','localtime'))
  );
`);

// --- Anthropic client ---
const anthropic = new Anthropic({ apiKey: process.env.ANTHROPIC_API_KEY });

app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// --- Parse voice/text input into structured data ---
app.post('/api/capture', async (req, res) => {
  const { text } = req.body;
  if (!text || !text.trim()) return res.status(400).json({ error: 'No text provided' });

  if (!process.env.ANTHROPIC_API_KEY || process.env.ANTHROPIC_API_KEY === 'your_key_here') {
    return res.status(500).json({ error: 'ANTHROPIC_API_KEY not set in .env file' });
  }

  try {
    const message = await anthropic.messages.create({
      model: 'claude-haiku-4-5-20251001',
      max_tokens: 512,
      messages: [{
        role: 'user',
        content: `Parse this voice/text capture into a structured item for a personal productivity system.

Input: "${text}"

Respond ONLY with valid JSON (no markdown, no extra text):
{
  "type": "task" | "note" | "journal" | "quote",
  "title": "short title (max 60 chars)",
  "content": "cleaned up, full content without filler words",
  "area": "home" | "work" | "personal" | "health" | "finance" | null,
  "due_date": "YYYY-MM-DD HH:MM" or null,
  "priority": "low" | "medium" | "high" | null
}

Rules:
- If it mentions a task, action, or to-do → type=task
- If it's a thought, idea, or observation → type=note
- If it's about a day, event, or personal reflection → type=journal
- If it's a quote from someone/a book → type=quote
- Extract due dates from phrases like "tomorrow", "at 2pm", "next Monday" (today is ${new Date().toLocaleDateString('en-CA')})
- area: infer from context (home=house/family, work=job/client/meeting, health=exercise/doctor, finance=money/bills)
- Strip "ums", "uhs", filler words from content
`
      }]
    });

    let parsed;
    try {
      parsed = JSON.parse(message.content[0].text.trim());
    } catch {
      parsed = {
        type: 'note',
        title: text.slice(0, 60),
        content: text,
        area: null,
        due_date: null,
        priority: null
      };
    }

    const stmt = db.prepare(`
      INSERT INTO items (type, title, content, area, due_date, priority, raw_input)
      VALUES (@type, @title, @content, @area, @due_date, @priority, @raw_input)
    `);
    const result = stmt.run({
      type: parsed.type || 'note',
      title: parsed.title || text.slice(0, 60),
      content: parsed.content || text,
      area: parsed.area || null,
      due_date: parsed.due_date || null,
      priority: parsed.priority || null,
      raw_input: text
    });

    const item = db.prepare('SELECT * FROM items WHERE id = ?').get(result.lastInsertRowid);
    res.json({ success: true, item });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// --- Get all items ---
app.get('/api/items', (req, res) => {
  const { type, area, status } = req.query;
  let query = 'SELECT * FROM items WHERE 1=1';
  const params = [];
  if (type) { query += ' AND type = ?'; params.push(type); }
  if (area) { query += ' AND area = ?'; params.push(area); }
  if (status) { query += ' AND status = ?'; params.push(status); }
  query += ' ORDER BY created_at DESC LIMIT 200';
  res.json(db.prepare(query).all(...params));
});

// --- Update item ---
app.patch('/api/items/:id', (req, res) => {
  const { status, priority, due_date, title, content, area } = req.body;
  const fields = [];
  const vals = [];
  if (status !== undefined) { fields.push('status = ?'); vals.push(status); }
  if (priority !== undefined) { fields.push('priority = ?'); vals.push(priority); }
  if (due_date !== undefined) { fields.push('due_date = ?'); vals.push(due_date); }
  if (title !== undefined) { fields.push('title = ?'); vals.push(title); }
  if (content !== undefined) { fields.push('content = ?'); vals.push(content); }
  if (area !== undefined) { fields.push('area = ?'); vals.push(area); }
  if (!fields.length) return res.status(400).json({ error: 'Nothing to update' });
  fields.push("updated_at = datetime('now','localtime')");
  vals.push(req.params.id);
  db.prepare(`UPDATE items SET ${fields.join(', ')} WHERE id = ?`).run(...vals);
  res.json(db.prepare('SELECT * FROM items WHERE id = ?').get(req.params.id));
});

// --- Delete item ---
app.delete('/api/items/:id', (req, res) => {
  db.prepare('DELETE FROM items WHERE id = ?').run(req.params.id);
  res.json({ success: true });
});

// --- Stats ---
app.get('/api/stats', (req, res) => {
  const counts = db.prepare(`
    SELECT type, status, COUNT(*) as count FROM items GROUP BY type, status
  `).all();
  const recent = db.prepare('SELECT * FROM items ORDER BY created_at DESC LIMIT 10').all();
  res.json({ counts, recent });
});

app.listen(PORT, () => {
  console.log(`\n🎉 Boop is running at http://localhost:${PORT}\n`);
  if (!process.env.ANTHROPIC_API_KEY || process.env.ANTHROPIC_API_KEY === 'your_key_here') {
    console.warn('⚠️  Add your ANTHROPIC_API_KEY to .env to enable AI organization\n');
  }
});
