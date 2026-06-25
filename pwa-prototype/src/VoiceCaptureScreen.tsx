import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { parseCapture } from './parseCapture'

type Props = {
  onSave: (result: ReturnType<typeof parseCapture>) => Promise<void>
}

type Phase = 'idle' | 'listening' | 'processing' | 'preview' | 'saved' | 'error'

export default function VoiceCaptureScreen({ onSave }: Props) {
  const navigate = useNavigate()
  const [phase, setPhase] = useState<Phase>('idle')
  const [transcript, setTranscript] = useState('')
  const [interimText, setInterimText] = useState('')
  const [parsed, setParsed] = useState<ReturnType<typeof parseCapture> | null>(null)
  const [errorMsg, setErrorMsg] = useState('')
  const [textMode, setTextMode] = useState(false)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const recognitionRef = useRef<any>(null)
  const finalRef = useRef('')

  // Clean up recognition on unmount
  useEffect(() => () => { recognitionRef.current?.abort() }, [])

  function startListening() {
    const SR = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition
    if (!SR) {
      setErrorMsg('Voice input not supported in this browser. Use the text box instead.')
      setTextMode(true)
      return
    }
    finalRef.current = ''
    setTranscript('')
    setInterimText('')

    const rec = new SR()
    rec.continuous = true
    rec.interimResults = true
    rec.lang = 'en-US'
    recognitionRef.current = rec

    rec.onstart = () => setPhase('listening')

    rec.onresult = (e: any) => {
      let interim = ''
      for (let i = e.resultIndex; i < e.results.length; i++) {
        const chunk = e.results[i][0].transcript
        if (e.results[i].isFinal) finalRef.current += chunk
        else interim += chunk
      }
      setTranscript(finalRef.current)
      setInterimText(interim)
    }

    rec.onerror = (e: any) => {
      if (e.error === 'no-speech') return
      setErrorMsg(`Mic error: ${e.error}. Try typing instead.`)
      setPhase('error')
    }

    rec.onend = () => {
      if (phase === 'listening') stopAndProcess()
    }

    rec.start()
  }

  function stopAndProcess() {
    recognitionRef.current?.stop()
    recognitionRef.current = null
    setInterimText('')
    const full = (finalRef.current + ' ' + interimText).trim() || transcript.trim()
    if (!full) { setPhase('idle'); return }
    setPhase('processing')
    const result = parseCapture(full)
    setParsed(result)
    setPhase('preview')
  }

  async function save() {
    if (!parsed) return
    setPhase('processing')
    try {
      await onSave(parsed)
      setPhase('saved')
      setTimeout(() => navigate(-1), 1400)
    } catch {
      setErrorMsg('Failed to save. Check your connection.')
      setPhase('error')
    }
  }

  function reset() {
    recognitionRef.current?.abort()
    recognitionRef.current = null
    finalRef.current = ''
    setTranscript('')
    setInterimText('')
    setParsed(null)
    setPhase('idle')
    setErrorMsg('')
  }

  const dueLabel = parsed?.dueEpochMillis
    ? new Date(parsed.dueEpochMillis).toLocaleString([], {
        month: 'short', day: 'numeric',
        hour: '2-digit', minute: '2-digit',
      })
    : null

  return (
    <section className="stack">
      {/* Header */}
      <div className="row-between">
        <button className="icon-link btn-ghost" onClick={() => { recognitionRef.current?.abort(); navigate(-1) }}>
          <span className="material-symbols-outlined">close</span>
          <span className="tiny">CLOSE</span>
        </button>
        <span className="tiny" style={{ opacity: 0.5 }}>VOICE CAPTURE</span>
      </div>

      <h1 className="title">Quick Capture</h1>
      <p className="muted">Speak naturally — say a task, thought, or reminder.</p>

      {/* ── IDLE ── */}
      {phase === 'idle' && !textMode && (
        <>
          <button className="btn-primary" onClick={startListening}>
            <span className="material-symbols-outlined">mic</span>
            START SPEAKING
          </button>
          <button className="btn-ghost tiny" onClick={() => setTextMode(true)} style={{ alignSelf: 'center', marginTop: 8 }}>
            Type instead
          </button>
        </>
      )}

      {/* ── TEXT MODE ── */}
      {textMode && phase !== 'preview' && phase !== 'saved' && (
        <>
          <textarea
            className="field body-input"
            placeholder="Type anything — a task, thought, note, reminder…"
            value={transcript}
            onChange={e => setTranscript(e.target.value)}
            rows={4}
            autoFocus
          />
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-primary" style={{ flex: 1 }} onClick={() => {
              if (!transcript.trim()) return
              finalRef.current = transcript
              const result = parseCapture(transcript.trim())
              setParsed(result)
              setPhase('preview')
            }}>
              PARSE &amp; PREVIEW
            </button>
            <button className="btn-ghost" onClick={() => { setTextMode(false); reset() }}>
              <span className="material-symbols-outlined">mic</span>
            </button>
          </div>
        </>
      )}

      {/* ── LISTENING ── */}
      {phase === 'listening' && (
        <>
          <div className="vc-pulse-ring" aria-hidden="true">
            <div className="vc-pulse-dot" />
          </div>
          <p className="muted" style={{ textAlign: 'center' }}>Listening…</p>
          {(transcript || interimText) && (
            <div className="vc-transcript">
              <span>{transcript}</span>
              <span style={{ opacity: 0.45 }}>{interimText}</span>
            </div>
          )}
          <button className="btn-primary" onClick={stopAndProcess}>
            <span className="material-symbols-outlined">stop</span>
            DONE — PARSE IT
          </button>
        </>
      )}

      {/* ── PROCESSING ── */}
      {phase === 'processing' && (
        <p className="muted" style={{ textAlign: 'center' }}>Parsing your input…</p>
      )}

      {/* ── PREVIEW ── */}
      {phase === 'preview' && parsed && (
        <>
          <div className="vc-transcript" style={{ marginBottom: 4 }}>
            <span className="tiny" style={{ opacity: 0.5 }}>You said</span>
            <p style={{ marginTop: 4 }}>{finalRef.current || transcript}</p>
          </div>

          <div className="card" style={{ gap: 12, display: 'flex', flexDirection: 'column' }}>
            <div className="row-between">
              <span className="tiny">DETECTED AS</span>
              <span className={`vc-badge vc-badge-${parsed.type.toLowerCase()}`}>
                {parsed.type}
              </span>
            </div>

            <div>
              <span className="tiny" style={{ opacity: 0.5 }}>TITLE</span>
              <p style={{ marginTop: 2, fontWeight: 600 }}>{parsed.title}</p>
            </div>

            {parsed.body !== parsed.title && (
              <div>
                <span className="tiny" style={{ opacity: 0.5 }}>CONTENT</span>
                <p className="muted" style={{ marginTop: 2 }}>{parsed.body}</p>
              </div>
            )}

            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {dueLabel && (
                <span className="vc-chip">
                  <span className="material-symbols-outlined" style={{ fontSize: 14 }}>schedule</span>
                  {dueLabel}
                </span>
              )}
              {parsed.area && (
                <span className="vc-chip">
                  <span className="material-symbols-outlined" style={{ fontSize: 14 }}>place</span>
                  {parsed.area}
                </span>
              )}
              {parsed.priority && (
                <span className={`vc-chip vc-chip-${parsed.priority}`}>
                  {parsed.priority} priority
                </span>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn-primary" style={{ flex: 1 }} onClick={save}>
              <span className="material-symbols-outlined">save</span>
              SAVE
            </button>
            <button className="btn-ghost" onClick={reset}>
              REDO
            </button>
          </div>
        </>
      )}

      {/* ── SAVED ── */}
      {phase === 'saved' && (
        <div style={{ textAlign: 'center', padding: '32px 0' }}>
          <span className="material-symbols-outlined" style={{ fontSize: 48, color: 'var(--clr-accent)' }}>check_circle</span>
          <p style={{ marginTop: 8 }}>Saved!</p>
        </div>
      )}

      {/* ── ERROR ── */}
      {phase === 'error' && (
        <>
          <p className="muted" style={{ color: 'var(--clr-danger, #ef4444)' }}>{errorMsg}</p>
          <textarea
            className="field body-input"
            placeholder="Type your capture here instead…"
            value={transcript}
            onChange={e => setTranscript(e.target.value)}
            rows={4}
          />
          <button className="btn-primary" onClick={() => {
            finalRef.current = transcript
            const result = parseCapture(transcript.trim())
            setParsed(result)
            setPhase('preview')
          }}>
            PARSE &amp; PREVIEW
          </button>
        </>
      )}

      <style>{`
        .vc-pulse-ring {
          display: flex; align-items: center; justify-content: center;
          width: 80px; height: 80px; margin: 16px auto;
          border-radius: 50%;
          animation: vc-ring-pulse 1.4s ease-in-out infinite;
          background: rgba(var(--clr-accent-rgb, 255,255,255), 0.08);
        }
        .vc-pulse-dot {
          width: 32px; height: 32px; border-radius: 50%;
          background: var(--clr-accent, #fff);
          animation: vc-dot-pulse 1.4s ease-in-out infinite;
        }
        @keyframes vc-ring-pulse {
          0%,100% { transform: scale(1); opacity: 0.7; }
          50% { transform: scale(1.15); opacity: 1; }
        }
        @keyframes vc-dot-pulse {
          0%,100% { transform: scale(0.85); }
          50% { transform: scale(1); }
        }
        .vc-transcript {
          background: var(--clr-surface, #111);
          border: 1px solid var(--clr-border, #333);
          border-radius: 8px;
          padding: 12px 14px;
          font-size: 14px;
          line-height: 1.6;
        }
        .vc-badge {
          font-size: 11px; font-weight: 700; letter-spacing: 0.08em;
          padding: 3px 10px; border-radius: 20px;
        }
        .vc-badge-task { background: rgba(99,102,241,0.15); color: #818cf8; }
        .vc-badge-note { background: rgba(34,197,94,0.12); color: #4ade80; }
        .vc-badge-journal { background: rgba(251,191,36,0.12); color: #fbbf24; }
        .vc-badge-quote { background: rgba(244,114,182,0.12); color: #f472b6; }
        .vc-chip {
          display: inline-flex; align-items: center; gap: 4px;
          font-size: 12px; padding: 3px 10px; border-radius: 20px;
          background: rgba(255,255,255,0.06); border: 1px solid rgba(255,255,255,0.1);
        }
        .vc-chip-high { border-color: rgba(239,68,68,0.4); color: #f87171; }
        .vc-chip-medium { border-color: rgba(234,179,8,0.4); color: #fbbf24; }
        .vc-chip-low { border-color: rgba(34,197,94,0.3); color: #4ade80; }
        .btn-ghost {
          background: none; border: 1px solid rgba(255,255,255,0.12);
          border-radius: 6px; cursor: pointer; padding: 10px 16px;
          display: inline-flex; align-items: center; gap: 6px;
          font-size: 13px; font-weight: 600;
          color: inherit;
        }
      `}</style>
    </section>
  )
}
