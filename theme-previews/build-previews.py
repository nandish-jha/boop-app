#!/usr/bin/env python3
"""Regenerate BOOP theme preview HTML pages with embedded CSS vars (no broken color-mix)."""
import json
from pathlib import Path

ROOT = Path(__file__).parent
PREVIEW_CSS = (ROOT / "preview.css").read_text()
MOCK_JS = (ROOT / "mock.js").read_text()

FONT_LINK = (
    '<link rel="preconnect" href="https://fonts.googleapis.com" />\n'
    '  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />\n'
    '  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600'
    '&family=Source+Serif+4:ital,wght@0,500;1,500&display=swap" rel="stylesheet" />'
)

def shadow_pack(glow_a=0.22, card_a=0.16, fab_a=0.4, ring_a=0.3):
    return {
        "quote-shadow": f"0 4px 18px rgba(212, 168, 160, {glow_a})",
        "card-shadow": f"0 3px 14px rgba(0, 0, 0, {card_a})",
        "fab-shadow": f"0 4px 20px rgba(155, 176, 212, {fab_a})",
        "fab-border": "rgba(212, 168, 160, 0.38)",
        "pill-bg": "rgba(212, 168, 160, 0.16)",
        "pill-border": "rgba(142, 164, 200, 0.45)",
        "ring-shadow": f"0 0 12px rgba(212, 168, 160, {ring_a})",
        "check-border": "rgba(168, 160, 152, 0.45)",
    }

def vars_block(mode: str, tokens: dict) -> str:
    lines = [f"    .theme-{mode} {{"]
    for k, v in tokens.items():
        lines.append(f"      --{k}: {v};")
    lines.append("    }")
    return "\n".join(lines)

def page(title, heading, desc, bullets, dark, light, dark_tweak, light_tweak, dark_surface, light_surface, standalone=False):
    bullet_html = "".join(f"<li>{b}</li>" for b in bullets)
    tweaks = f"""
    <section class="tweaks"><h3>Details</h3><ul>{bullet_html}</ul></section>""" if bullets else ""

    css = PREVIEW_CSS if standalone else ""
    css_link = "" if standalone else '  <link rel="stylesheet" href="preview.css" />\n'
    style_open = "  <style>\n" if standalone else "  <style>\n"
    style_inner = (css + "\n" if standalone else "") + vars_block("dark", dark) + "\n" + vars_block("light", light) + "\n"
    script_block = f"  <script>\n{MOCK_JS}\n  </script>" if standalone else '  <script src="mock.js"></script>'

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>BOOP — {title}</title>
  {FONT_LINK}
{css_link}{style_open}{style_inner}  </style>
</head>
<body>
  <div class="page">
    <a class="back" href="index.html">← All themes</a>
    <header class="page-header">
      <h1>{heading}</h1>
      <p>{desc}</p>
    </header>{tweaks}
    <div class="phones">
      <div class="phone-wrap" data-tweak="{dark_tweak}" data-surface="{dark_surface}">
        <span class="mode-label">Dark</span>
        <div class="phone theme-dark"></div>
      </div>
      <div class="phone-wrap" data-tweak="{light_tweak}" data-surface="{light_surface}">
        <span class="mode-label">Light</span>
        <div class="phone theme-light"></div>
      </div>
    </div>
  </div>
  {script_block}
</body>
</html>
"""

# Base parchment dark/light shells
def parchment_dark(**over):
    base = {
        "bg": "#141210", "surface": "#2e2b28", "surface-variant": "#242120", "surface-elevated": "#3a3632",
        "text": "#faf6f0", "muted": "#a8a098", "accent-on": "#141210",
        "card-border": "rgba(168, 160, 152, 0.14)",
        **shadow_pack(),
    }
    base.update(over)
    return base

def parchment_light(**over):
    base = {
        "bg": "#fbf7f1", "surface": "#fffdf9", "surface-variant": "#ede6dc", "surface-elevated": "#f5efe6",
        "text": "#1a1612", "muted": "#8a8278", "accent-on": "#ffffff",
        "card-border": "rgba(138, 130, 120, 0.12)",
        "quote-shadow": "0 4px 18px rgba(200, 136, 128, 0.18)",
        "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.08)",
        "fab-shadow": "0 4px 20px rgba(90, 116, 148, 0.35)",
        "fab-border": "rgba(200, 136, 128, 0.32)",
        "pill-bg": "rgba(200, 136, 128, 0.14)",
        "pill-border": "rgba(122, 144, 176, 0.4)",
        "ring-shadow": "0 0 12px rgba(200, 136, 128, 0.25)",
        "check-border": "rgba(138, 130, 120, 0.4)",
    }
    base.update(over)
    return base

THEMES = [
    {
        "file": "slate-glow.html",
        "title": "Slate + rose glow",
        "heading": "Slate + rose glow",
        "desc": "Baseline in app v5.1.9 — popped slate accent, rose faded glow, warm parchment.",
        "bullets": ["Accent: slate · Glow: rose", "Quote tint + card depth", "Serif Medium titles"],
        "dark": parchment_dark(**{
            "accent": "#9bb0d4", "quote-fill": "#2a2426", "quote-border": "#8ea4c8", "ring": "#c4a8c0",
        }),
        "light": parchment_light(**{
            "accent": "#5a7494", "quote-fill": "#f5ebe8", "quote-border": "#7a90b0", "ring": "#c4a8c0",
        }),
        "dark_tweak": "Slate pop · rose glow", "light_tweak": "Deeper slate · cream rose",
        "dark_surface": "Warm parchment dark", "light_surface": "Warm cream light",
        "standalone": True,
    },
    {
        "file": "slate-glow-soft.html",
        "title": "Slate glow soft",
        "heading": "Slate glow — soft",
        "desc": "Same palette as v5.1.9 but rose glow dialed back. Slate reads more dominant; glow is a hint.",
        "bullets": ["Glow opacity ~12% (was ~22%)", "Quote fill slightly less rose-tinted", "FAB glow border thinner"],
        "dark": parchment_dark(**{
            "accent": "#9bb0d4", "quote-fill": "#282422", "quote-border": "#8ea4c8", "ring": "#b8a8b8",
            **shadow_pack(glow_a=0.12, ring_a=0.18, fab_a=0.28),
        }),
        "light": parchment_light(**{
            "accent": "#5a7494", "quote-fill": "#f7f0ee", "quote-border": "#7a90b0", "ring": "#c4a8c0",
            "quote-shadow": "0 4px 18px rgba(200, 136, 128, 0.1)",
            "fab-shadow": "0 4px 20px rgba(90, 116, 148, 0.28)",
            "fab-border": "rgba(200, 136, 128, 0.22)",
            "pill-bg": "rgba(200, 136, 128, 0.1)",
            "ring-shadow": "0 0 12px rgba(200, 136, 128, 0.18)",
        }),
        "dark_tweak": "Whisper rose glow", "light_tweak": "Whisper rose glow",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
    {
        "file": "slate-glow-strong.html",
        "title": "Slate glow strong",
        "heading": "Slate glow — strong",
        "desc": "Maximum rose atmosphere — quote halo, FAB border, and nav pill all glow harder.",
        "bullets": ["Glow ~38%", "Rose-tinted quote fill", "Thicker FAB glow ring"],
        "dark": parchment_dark(**{
            "accent": "#9bb0d4", "quote-fill": "#322628", "quote-border": "#a0b8dc", "ring": "#d4b0b8",
            **shadow_pack(glow_a=0.38, ring_a=0.45, fab_a=0.55, card_a=0.22),
        }),
        "light": parchment_light(**{
            "accent": "#5a7494", "quote-fill": "#f8e4e0", "quote-border": "#8aa0c0", "ring": "#d4a8b8",
            "quote-shadow": "0 4px 26px rgba(216, 152, 144, 0.32)",
            "fab-shadow": "0 4px 22px rgba(90, 116, 148, 0.45)",
            "fab-border": "rgba(216, 152, 144, 0.55)",
            "pill-bg": "rgba(216, 152, 144, 0.24)",
            "ring-shadow": "0 0 14px rgba(216, 152, 144, 0.4)",
        }),
        "dark_tweak": "Heavy rose halo", "light_tweak": "Heavy rose halo",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
    {
        "file": "slate-glow-bright.html",
        "title": "Slate glow bright",
        "heading": "Slate glow — bright pop",
        "desc": "Electric lighter slate accent — more sky-blue pop on the same warm parchment.",
        "bullets": ["Accent #A8C4E8 dark / #6480A8 light", "Brighter quote stroke", "Same rose glow"],
        "dark": parchment_dark(**{
            "accent": "#a8c4e8", "accent-on": "#101010", "quote-fill": "#2a2628",
            "quote-border": "#a8c4e8", "ring": "#c8b0d0",
        }),
        "light": parchment_light(**{
            "accent": "#6480a8", "quote-fill": "#f5ebe8", "quote-border": "#6480a8", "ring": "#a8b8d8",
        }),
        "dark_tweak": "Electric slate", "light_tweak": "Electric slate",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
    {
        "file": "slate-glow-deep.html",
        "title": "Slate glow deep",
        "heading": "Slate glow — deep",
        "desc": "Richer, grounded slate — more serious and less pastel.",
        "bullets": ["Accent #6A84A8 dark / #445A78 light", "Deeper quote stroke", "Muted ring"],
        "dark": parchment_dark(**{
            "accent": "#6a84a8", "accent-on": "#ffffff", "quote-fill": "#2a2426",
            "quote-border": "#6a84a8", "ring": "#b0a0b0",
        }),
        "light": parchment_light(**{
            "accent": "#445a78", "quote-fill": "#f5ebe8", "quote-border": "#445a78", "ring": "#8898b8",
        }),
        "dark_tweak": "Grounded slate", "light_tweak": "Grounded slate",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
    {
        "file": "slate-glow-dusk.html",
        "title": "Slate glow dusk",
        "heading": "Slate glow — dusk",
        "desc": "Darker moody parchment with muted rose glow — evening feel.",
        "bullets": ["Darker bg #0E0D0C", "Muted glow #C09088", "Softer contrast"],
        "dark": {
            "bg": "#0e0d0c", "surface": "#262320", "surface-variant": "#1c1a18", "surface-elevated": "#32302c",
            "text": "#f0ebe4", "muted": "#948c84", "accent": "#8ea4c4", "accent-on": "#0e0d0c",
            "quote-fill": "#221c1e", "quote-border": "#7a90b0", "ring": "#b098a8",
            "card-border": "rgba(148, 140, 132, 0.14)",
            **shadow_pack(glow_a=0.18, ring_a=0.22, fab_a=0.32),
            "quote-shadow": "0 4px 18px rgba(192, 144, 136, 0.18)",
            "fab-border": "rgba(192, 144, 136, 0.32)",
            "pill-bg": "rgba(192, 144, 136, 0.12)",
            "ring-shadow": "0 0 12px rgba(192, 144, 136, 0.22)",
        },
        "light": {
            "bg": "#f3ede5", "surface": "#faf6ef", "surface-variant": "#e6ddd2", "surface-elevated": "#f0e9e0",
            "text": "#181412", "muted": "#827870", "accent": "#506888", "accent-on": "#ffffff",
            "quote-fill": "#efe4e0", "quote-border": "#6880a0", "ring": "#b8a0b0",
            "card-border": "rgba(130, 120, 112, 0.12)",
            "quote-shadow": "0 4px 18px rgba(184, 120, 112, 0.16)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.08)",
            "fab-shadow": "0 4px 20px rgba(80, 104, 136, 0.32)",
            "fab-border": "rgba(184, 120, 112, 0.3)",
            "pill-bg": "rgba(184, 120, 112, 0.12)",
            "pill-border": "rgba(104, 128, 160, 0.35)",
            "ring-shadow": "0 0 12px rgba(184, 120, 112, 0.2)",
            "check-border": "rgba(130, 120, 112, 0.4)",
        },
        "dark_tweak": "Evening mood", "light_tweak": "Evening mood",
        "dark_surface": "Dusk parchment", "light_surface": "Dusk cream",
        "standalone": True,
    },
    {
        "file": "slate-glow-pearl.html",
        "title": "Slate glow pearl",
        "heading": "Slate glow — pearl",
        "desc": "Brighter light mode surfaces with a deeper slate accent for crisp contrast.",
        "bullets": ["Light bg #FFFCF7", "Accent #547090", "Pearl-white cards"],
        "dark": parchment_dark(**{
            "accent": "#9bb0d4", "quote-fill": "#2a2426", "quote-border": "#8ea4c8", "ring": "#c4a8c0",
        }),
        "light": {
            "bg": "#fffcf7", "surface": "#ffffff", "surface-variant": "#f2ebe4", "surface-elevated": "#faf6f0",
            "text": "#161310", "muted": "#908880", "accent": "#547090", "accent-on": "#ffffff",
            "quote-fill": "#faf0ec", "quote-border": "#6a84a8", "ring": "#b8a8c8",
            "card-border": "rgba(144, 136, 128, 0.12)",
            "quote-shadow": "0 4px 18px rgba(208, 160, 152, 0.18)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.07)",
            "fab-shadow": "0 4px 20px rgba(84, 112, 144, 0.38)",
            "fab-border": "rgba(208, 160, 152, 0.32)",
            "pill-bg": "rgba(208, 160, 152, 0.14)",
            "pill-border": "rgba(106, 132, 168, 0.4)",
            "ring-shadow": "0 0 12px rgba(208, 160, 152, 0.25)",
            "check-border": "rgba(144, 136, 128, 0.4)",
        },
        "dark_tweak": "Pearl light", "light_tweak": "Pearl light",
        "dark_surface": "Parchment dark", "light_surface": "Pearl white",
        "standalone": True,
    },
    {
        "file": "slate-glow-moonlit.html",
        "title": "Slate glow moonlit",
        "heading": "Slate glow — moonlit",
        "desc": "Cool blue-grey surfaces with lavender glow — night-sky mood.",
        "bullets": ["Cool surfaces #12141A", "Lavender glow #B8A8C8", "Blue-grey accent"],
        "dark": {
            "bg": "#12141a", "surface": "#22262e", "surface-variant": "#1a1e26", "surface-elevated": "#2e3440",
            "text": "#eef0f4", "muted": "#949aa8", "accent": "#94a8c8", "accent-on": "#101218",
            "quote-fill": "#1e2228", "quote-border": "#88a0c4", "ring": "#a8b0d0",
            "card-border": "rgba(148, 154, 168, 0.14)",
            "quote-shadow": "0 4px 18px rgba(184, 168, 200, 0.22)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.18)",
            "fab-shadow": "0 4px 20px rgba(148, 168, 200, 0.4)",
            "fab-border": "rgba(184, 168, 200, 0.38)",
            "pill-bg": "rgba(184, 168, 200, 0.16)",
            "pill-border": "rgba(136, 160, 196, 0.45)",
            "ring-shadow": "0 0 12px rgba(184, 168, 200, 0.3)",
            "check-border": "rgba(148, 154, 168, 0.45)",
        },
        "light": {
            "bg": "#f6f7fa", "surface": "#ffffff", "surface-variant": "#e8eaef", "surface-elevated": "#f0f2f6",
            "text": "#141820", "muted": "#707888", "accent": "#587098", "accent-on": "#ffffff",
            "quote-fill": "#eef0f6", "quote-border": "#587098", "ring": "#98a8c8",
            "card-border": "rgba(112, 120, 136, 0.12)",
            "quote-shadow": "0 4px 18px rgba(168, 152, 184, 0.18)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.08)",
            "fab-shadow": "0 4px 20px rgba(88, 112, 152, 0.35)",
            "fab-border": "rgba(168, 152, 184, 0.32)",
            "pill-bg": "rgba(168, 152, 184, 0.14)",
            "pill-border": "rgba(88, 112, 152, 0.4)",
            "ring-shadow": "0 0 12px rgba(168, 152, 184, 0.25)",
            "check-border": "rgba(112, 120, 136, 0.4)",
        },
        "dark_tweak": "Lavender glow", "light_tweak": "Lavender glow",
        "dark_surface": "Moonlit dark", "light_surface": "Cool light",
        "standalone": True,
    },
    {
        "file": "slate-glow-ember.html",
        "title": "Slate glow ember",
        "heading": "Slate glow — ember",
        "desc": "Slate accent with warm gold-rose glow — campfire warmth on parchment.",
        "bullets": ["Glow #D4B090 ember gold", "Warmer surfaces", "Ring #D4B890"],
        "dark": {
            "bg": "#161310", "surface": "#302c28", "surface-variant": "#262220", "surface-elevated": "#3c3832",
            "text": "#faf4ec", "muted": "#aca498", "accent": "#9bb0d4", "accent-on": "#141210",
            "quote-fill": "#2e2620", "quote-border": "#8ea4c8", "ring": "#d4b890",
            "card-border": "rgba(172, 164, 152, 0.14)",
            **shadow_pack(glow_a=0.24, ring_a=0.32, fab_a=0.38),
            "quote-shadow": "0 4px 18px rgba(212, 176, 144, 0.24)",
            "fab-border": "rgba(212, 176, 144, 0.38)",
            "pill-bg": "rgba(212, 176, 144, 0.16)",
            "ring-shadow": "0 0 12px rgba(212, 184, 144, 0.32)",
        },
        "light": {
            "bg": "#faf5ec", "surface": "#fffaf2", "surface-variant": "#efe6d8", "surface-elevated": "#f7f0e4",
            "text": "#1c1610", "muted": "#8e8274", "accent": "#5a7494", "accent-on": "#ffffff",
            "quote-fill": "#f8ece0", "quote-border": "#7a90b0", "ring": "#c8a878",
            "card-border": "rgba(142, 130, 116, 0.12)",
            "quote-shadow": "0 4px 18px rgba(200, 160, 112, 0.2)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.08)",
            "fab-shadow": "0 4px 20px rgba(90, 116, 148, 0.35)",
            "fab-border": "rgba(200, 160, 112, 0.32)",
            "pill-bg": "rgba(200, 160, 112, 0.14)",
            "pill-border": "rgba(122, 144, 176, 0.4)",
            "ring-shadow": "0 0 12px rgba(200, 160, 112, 0.28)",
            "check-border": "rgba(142, 130, 116, 0.4)",
        },
        "dark_tweak": "Gold-rose ember", "light_tweak": "Gold-rose ember",
        "dark_surface": "Warm dark", "light_surface": "Warm cream",
        "standalone": True,
    },
    {
        "file": "rose-glow-primary.html",
        "title": "Rose glow primary",
        "heading": "Rose glow — primary",
        "desc": "Flipped crossover: rose is the accent (FAB, links), slate is the faded glow.",
        "bullets": ["Accent = rose #D4A8A0", "Glow = slate #9BB0D4", "Same parchment surfaces"],
        "dark": parchment_dark(**{
            "accent": "#d4a8a0", "quote-fill": "#2e2424", "quote-border": "#d4a8a0", "ring": "#c4a8c0",
            **shadow_pack(),
        }),
        "light": parchment_light(**{
            "accent": "#c08078", "quote-fill": "#f8ece8", "quote-border": "#c08078", "ring": "#a8b8d0",
        }),
        "dark_tweak": "Rose accent · slate glow", "light_tweak": "Rose accent · slate glow",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
    {
        "file": "terracotta-glow.html",
        "title": "Terracotta glow",
        "heading": "Terracotta + rose glow",
        "desc": "Terracotta accent with rose glow halo — classic warm orange on parchment.",
        "bullets": ["Accent = terracotta #D97757", "Glow = rose", "Classic Claude parchment"],
        "dark": {
            "bg": "#141413", "surface": "#30302e", "surface-variant": "#252320", "surface-elevated": "#3d3d3a",
            "text": "#faf9f5", "muted": "#b0aea5", "accent": "#d97757", "accent-on": "#ffffff",
            "quote-fill": "#2e2420", "quote-border": "#d97757", "ring": "#e8a898",
            "card-border": "rgba(176, 174, 165, 0.12)",
            **shadow_pack(glow_a=0.26),
        },
        "light": {
            "bg": "#faf9f5", "surface": "#f5f4ed", "surface-variant": "#e8e6dc", "surface-elevated": "#efe9de",
            "text": "#141413", "muted": "#87867f", "accent": "#c96442", "accent-on": "#ffffff",
            "quote-fill": "#f8ece6", "quote-border": "#c96442", "ring": "#d97757",
            "card-border": "rgba(135, 134, 127, 0.12)",
            "quote-shadow": "0 4px 18px rgba(200, 136, 128, 0.2)",
            "card-shadow": "0 3px 14px rgba(0, 0, 0, 0.08)",
            "fab-shadow": "0 4px 20px rgba(217, 119, 87, 0.38)",
            "fab-border": "rgba(212, 168, 160, 0.35)",
            "pill-bg": "rgba(212, 168, 160, 0.15)",
            "pill-border": "rgba(201, 100, 66, 0.42)",
            "ring-shadow": "0 0 12px rgba(217, 119, 87, 0.28)",
            "check-border": "rgba(135, 134, 127, 0.4)",
        },
        "dark_tweak": "Terracotta + rose halo", "light_tweak": "Terracotta + rose halo",
        "dark_surface": "Classic parchment", "light_surface": "Classic cream",
        "standalone": True,
    },
    {
        "file": "rose-terracotta-glow.html",
        "title": "Rose terracotta glow",
        "heading": "Rose + terracotta glow",
        "desc": "Rose accent with terracotta-orange glow — warmest crossover of all three families.",
        "bullets": ["Accent = dusty rose", "Glow = terracotta orange", "All three families at once"],
        "dark": parchment_dark(**{
            "accent": "#c98b84", "quote-fill": "#2e2422", "quote-border": "#c98b84", "ring": "#d4a090",
            **shadow_pack(glow_a=0.24, fab_a=0.35),
        }),
        "light": parchment_light(**{
            "accent": "#b0746d", "quote-fill": "#f8eae6", "quote-border": "#b0746d", "ring": "#c96442",
        }),
        "dark_tweak": "Rose + orange glow", "light_tweak": "Rose + orange glow",
        "dark_surface": "Warm dark", "light_surface": "Warm cream",
        "standalone": True,
    },
    {
        "file": "rose-slate-even.html",
        "title": "Rose slate even",
        "heading": "Rose + slate — even blend",
        "desc": "50/50 mauve accent — neither rose nor slate dominates.",
        "bullets": ["Accent = mauve #B8A0B0", "Dual glow tones", "Most neutral option"],
        "dark": parchment_dark(**{
            "accent": "#b8a0b0", "quote-fill": "#2a2428", "quote-border": "#a898b8", "ring": "#b8a8c8",
            "pill-bg": "rgba(155, 176, 212, 0.12)", "pill-border": "rgba(184, 160, 176, 0.4)",
        }),
        "light": parchment_light(**{
            "accent": "#887888", "quote-fill": "#f5eaee", "quote-border": "#7880a0", "ring": "#98a0c0",
            "pill-bg": "rgba(90, 116, 148, 0.1)", "pill-border": "rgba(120, 128, 160, 0.35)",
        }),
        "dark_tweak": "Mauve blend", "light_tweak": "Mauve blend",
        "dark_surface": "Parchment dark", "light_surface": "Cream light",
        "standalone": True,
    },
]

BASE_THEMES = [
    {
        "file": "terracotta.html", "heading": "Terracotta — classic parchment",
        "desc": "Original BOOP direction. Warm orange accent on parchment surfaces.",
        "bullets": ["Accent #D97757 / #C96442", "Classic parchment surfaces"],
        "dark": {"bg": "#141413", "surface": "#30302e", "surface-variant": "#252320", "surface-elevated": "#3d3d3a",
                 "text": "#faf9f5", "muted": "#b0aea5", "accent": "#d97757", "accent-on": "#ffffff",
                 "quote-fill": "#252320", "quote-border": "rgba(250,249,245,0.3)", "ring": "#e8a090",
                 "card-border": "rgba(176,174,165,0.12)"},
        "light": {"bg": "#faf9f5", "surface": "#f5f4ed", "surface-variant": "#e8e6dc", "surface-elevated": "#efe9de",
                  "text": "#141413", "muted": "#87867f", "accent": "#c96442", "accent-on": "#ffffff",
                  "quote-fill": "#efe9de", "quote-border": "rgba(20,20,19,0.2)", "ring": "#d97757",
                  "card-border": "rgba(135,134,127,0.14)"},
        "dark_tweak": "Base terracotta", "light_tweak": "Base terracotta",
        "dark_surface": "Parchment dark", "light_surface": "Parchment light",
        "card_title": "Terracotta classic", "card_desc": "Warm orange · parchment",
        "swatch": "background:#d97757",
    },
    {
        "file": "terracotta-blush.html", "heading": "Terracotta blush",
        "desc": "Soft peach terracotta — lighter and friendlier.",
        "bullets": ["Accent #E09578", "Peach ring"],
        "dark": {"bg": "#161514", "surface": "#33312e", "surface-variant": "#2a2825", "surface-elevated": "#403d39",
                 "text": "#faf8f4", "muted": "#b5b0a8", "accent": "#e09578", "accent-on": "#1a1412",
                 "quote-fill": "#2a2825", "quote-border": "rgba(250,248,244,0.28)", "ring": "#e8a898",
                 "card-border": "rgba(181,176,168,0.12)"},
        "light": {"bg": "#fcf9f6", "surface": "#f7f2eb", "surface-variant": "#ebe4da", "surface-elevated": "#f5f0e8",
                  "text": "#1a1816", "muted": "#8a847c", "accent": "#d08268", "accent-on": "#ffffff",
                  "quote-fill": "#f5f0e8", "quote-border": "rgba(26,24,22,0.18)", "ring": "#d08268",
                  "card-border": "rgba(138,132,124,0.14)"},
        "dark_tweak": "Peach terracotta", "light_tweak": "Peach terracotta",
        "dark_surface": "Warm dark", "light_surface": "Cream light",
        "card_title": "Terracotta blush", "card_desc": "Soft peach accent",
        "swatch": "background:#e09578",
    },
    {
        "file": "rose.html", "heading": "Rose classic",
        "desc": "Dusty coral blush accent on warm neutrals.",
        "bullets": ["Accent #C98B84", "Rose parchment"],
        "dark": {"bg": "#141316", "surface": "#2a272c", "surface-variant": "#211f24", "surface-elevated": "#353239",
                 "text": "#f3f0ee", "muted": "#a8a2a0", "accent": "#c98b84", "accent-on": "#1a1514",
                 "quote-fill": "#211f24", "quote-border": "rgba(243,240,238,0.28)", "ring": "#d4a8a0",
                 "card-border": "rgba(168,162,160,0.12)"},
        "light": {"bg": "#f8f4f2", "surface": "#f0eae6", "surface-variant": "#e6ded9", "surface-elevated": "#f5efeb",
                  "text": "#1c1817", "muted": "#857a78", "accent": "#b0746d", "accent-on": "#ffffff",
                  "quote-fill": "#f5efeb", "quote-border": "rgba(28,24,23,0.18)", "ring": "#b0746d",
                  "card-border": "rgba(133,122,120,0.14)"},
        "dark_tweak": "Dusty rose", "light_tweak": "Dusty rose",
        "dark_surface": "Rose dark", "light_surface": "Rose light",
        "card_title": "Rose classic", "card_desc": "Dusty coral blush",
        "swatch": "background:#c98b84",
    },
    {
        "file": "rose-dust.html", "heading": "Rose mauve dust",
        "desc": "Most muted rose — low saturation, very calm.",
        "bullets": ["Accent #B08E88", "Muted mauve"],
        "dark": {"bg": "#131215", "surface": "#272528", "surface-variant": "#1f1d21", "surface-elevated": "#323035",
                 "text": "#f0ecee", "muted": "#9e989c", "accent": "#b08e88", "accent-on": "#141012",
                 "quote-fill": "#1f1d21", "quote-border": "rgba(240,236,238,0.26)", "ring": "#b8a0a0",
                 "card-border": "rgba(158,152,156,0.12)"},
        "light": {"bg": "#f6f3f4", "surface": "#ebe6e8", "surface-variant": "#ded8db", "surface-elevated": "#f2eef0",
                  "text": "#1a1718", "muted": "#7d767a", "accent": "#967670", "accent-on": "#ffffff",
                  "quote-fill": "#f2eef0", "quote-border": "rgba(26,23,24,0.16)", "ring": "#967670",
                  "card-border": "rgba(125,118,122,0.14)"},
        "dark_tweak": "Mauve dust", "light_tweak": "Mauve dust",
        "dark_surface": "Muted dark", "light_surface": "Muted light",
        "card_title": "Rose mauve dust", "card_desc": "Most muted rose",
        "swatch": "background:#b08e88",
    },
    {
        "file": "slate-parchment.html", "heading": "Slate on parchment",
        "desc": "Plain slate accent on classic parchment — no rose glow.",
        "bullets": ["Accent #8A9AAD", "No glow halo"],
        "dark": {"bg": "#141413", "surface": "#30302e", "surface-variant": "#252320", "surface-elevated": "#3d3d3a",
                 "text": "#faf9f5", "muted": "#b0aea5", "accent": "#8a9aad", "accent-on": "#101218",
                 "quote-fill": "#252320", "quote-border": "rgba(250,249,245,0.28)", "ring": "#8a9aad",
                 "card-border": "rgba(176,174,165,0.12)"},
        "light": {"bg": "#faf9f5", "surface": "#f5f4ed", "surface-variant": "#e8e6dc", "surface-elevated": "#efe9de",
                  "text": "#141413", "muted": "#87867f", "accent": "#6d7f96", "accent-on": "#ffffff",
                  "quote-fill": "#efe9de", "quote-border": "rgba(20,20,19,0.18)", "ring": "#6d7f96",
                  "card-border": "rgba(135,134,127,0.14)"},
        "dark_tweak": "Plain slate", "light_tweak": "Plain slate",
        "dark_surface": "Parchment dark", "light_surface": "Parchment light",
        "card_title": "Slate on parchment", "card_desc": "No glow — plain slate",
        "swatch": "background:#8a9aad",
    },
    {
        "file": "slate-fog.html", "heading": "Slate fog",
        "desc": "Soft low-contrast slate on cool grey surfaces.",
        "bullets": ["Accent #A8B6C8", "Cool fog surfaces"],
        "dark": {"bg": "#15171c", "surface": "#22262e", "surface-variant": "#1c1f26", "surface-elevated": "#2e333c",
                 "text": "#f0f2f5", "muted": "#a0a6b0", "accent": "#a8b6c8", "accent-on": "#141820",
                 "quote-fill": "#1c1f26", "quote-border": "rgba(240,242,245,0.26)", "ring": "#a8b6c8",
                 "card-border": "rgba(160,166,176,0.12)"},
        "light": {"bg": "#f8f9fb", "surface": "#eef1f5", "surface-variant": "#e4e8ee", "surface-elevated": "#f4f6f9",
                  "text": "#1a1e24", "muted": "#848c98", "accent": "#8a9cb0", "accent-on": "#ffffff",
                  "quote-fill": "#f4f6f9", "quote-border": "rgba(26,30,36,0.16)", "ring": "#8a9cb0",
                  "card-border": "rgba(132,140,152,0.14)"},
        "dark_tweak": "Soft fog", "light_tweak": "Soft fog",
        "dark_surface": "Cool dark", "light_surface": "Cool light",
        "card_title": "Slate fog", "card_desc": "Soft low-contrast slate",
        "swatch": "background:#a8b6c8",
    },
]

CARD_GROUPS = [
    ("featured", None, ["slate-glow.html"]),
    ("refinement", "Slate + glow refinements", [
        "slate-glow-soft.html", "slate-glow-strong.html", "slate-glow-bright.html",
        "slate-glow-deep.html", "slate-glow-dusk.html", "slate-glow-pearl.html",
        "slate-glow-moonlit.html", "slate-glow-ember.html",
    ]),
    ("crossover", "Crossovers (accent ↔ glow flip)", [
        "rose-glow-primary.html", "terracotta-glow.html",
        "rose-terracotta-glow.html", "rose-slate-even.html",
    ]),
    ("original", "Original families (base variants)", [
        "terracotta.html", "terracotta-blush.html", "rose.html",
        "rose-dust.html", "slate-parchment.html", "slate-fog.html",
    ]),
]

CARD_EXTRAS = {
    "slate-glow.html": {"card_title": "Slate + rose glow", "card_desc": "Baseline — currently in the app", "tag": "v5.1.9", "swatch": "background:linear-gradient(135deg,#9bb0d4,#d4a8a0)"},
    "slate-glow-soft.html": {"card_title": "Soft", "card_desc": "Less rose glow — slate dominates", "swatch": "background:linear-gradient(135deg,#9bb0d4 60%,#d4a8a0 40%)"},
    "slate-glow-strong.html": {"card_title": "Strong", "card_desc": "Maximum rose atmosphere", "swatch": "background:linear-gradient(135deg,#9bb0d4 40%,#e0b0a8 60%)"},
    "slate-glow-bright.html": {"card_title": "Bright pop", "card_desc": "Electric lighter slate accent", "swatch": "background:#a8c4e8"},
    "slate-glow-deep.html": {"card_title": "Deep", "card_desc": "Richer, grounded slate", "swatch": "background:#6a84a8"},
    "slate-glow-dusk.html": {"card_title": "Dusk", "card_desc": "Darker moody parchment", "swatch": "background:#1a1816;border:2px solid #8ea4c4"},
    "slate-glow-pearl.html": {"card_title": "Pearl", "card_desc": "Brighter light mode", "swatch": "background:#fffcf7;border:2px solid #547090"},
    "slate-glow-moonlit.html": {"card_title": "Moonlit", "card_desc": "Cool surfaces · lavender glow", "swatch": "background:linear-gradient(135deg,#94a8c8,#b8a8c8)"},
    "slate-glow-ember.html": {"card_title": "Ember", "card_desc": "Slate · gold-rose glow", "swatch": "background:linear-gradient(135deg,#9bb0d4,#d4b090)"},
    "rose-glow-primary.html": {"card_title": "Rose primary", "card_desc": "Rose accent · slate glow", "swatch": "background:linear-gradient(135deg,#d4a8a0,#9bb0d4)"},
    "terracotta-glow.html": {"card_title": "Terracotta + glow", "card_desc": "Orange accent · rose halo", "swatch": "background:linear-gradient(135deg,#d97757,#d4a8a0)"},
    "rose-terracotta-glow.html": {"card_title": "Rose + terracotta glow", "card_desc": "Warmest — all three families", "swatch": "background:linear-gradient(135deg,#c98b84,#d97757)"},
    "rose-slate-even.html": {"card_title": "Rose + slate even", "card_desc": "50/50 mauve blend accent", "swatch": "background:#b8a0b0"},
}


def theme_id(theme: dict) -> str:
    return theme["file"].replace(".html", "")


def theme_for_index(theme: dict) -> dict:
    extras = CARD_EXTRAS.get(theme["file"], {})
    return {
        "id": theme_id(theme),
        "heading": theme["heading"],
        "desc": theme["desc"],
        "bullets": theme.get("bullets", []),
        "dark": theme["dark"],
        "light": theme["light"],
        "dark_tweak": theme["dark_tweak"],
        "light_tweak": theme["light_tweak"],
        "dark_surface": theme["dark_surface"],
        "light_surface": theme["light_surface"],
        "card_title": extras.get("card_title") or theme.get("card_title") or theme["heading"],
        "card_desc": extras.get("card_desc") or theme.get("card_desc") or theme["desc"],
        "tag": extras.get("tag", theme.get("tag", "")),
        "swatch": extras.get("swatch") or theme.get("swatch", "background:#888"),
    }


def build_index(all_themes: list[dict]) -> str:
    by_file = {t["file"]: t for t in all_themes}
    index_data = {theme_id(t): theme_for_index(t) for t in all_themes}
    themes_json = json.dumps(index_data, indent=2)

    cards_html = []
    for group_key, group_title, files in CARD_GROUPS:
        if group_key == "featured":
            t = by_file[files[0]]
            meta = theme_for_index(t)
            tag = f'<span class="tag">{meta["tag"]}</span>' if meta["tag"] else ""
            cards_html.append(
                f'<button type="button" class="card-btn featured" data-id="{meta["id"]}">'
                f'<span class="swatch" style="{meta["swatch"]}"></span>'
                f'<div><h2>{meta["card_title"]}</h2><p>{meta["card_desc"]}</p></div>{tag}</button>'
            )
            continue
        cards_html.append(f'<section class="family"><h2>{group_title}</h2><div class="grid">')
        for f in files:
            meta = theme_for_index(by_file[f])
            cards_html.append(
                f'<button type="button" class="card-btn" data-id="{meta["id"]}">'
                f'<span class="swatch" style="{meta["swatch"]}"></span>'
                f'<div><h2>{meta["card_title"]}</h2><p>{meta["card_desc"]}</p></div></button>'
            )
        cards_html.append("</div></section>")

    hub_css = """
.hub { max-width: 380px; }
.hub h1 {
  font-family: "Source Serif 4", Georgia, serif;
  font-size: 1.85rem; font-weight: 500; letter-spacing: -0.02em; margin-bottom: 8px;
}
.hub .sub { color: #9a97a8; margin-bottom: 16px; font-size: 0.9rem; line-height: 1.45; }
.grid { display: grid; gap: 8px; }
.card-btn {
  display: flex; align-items: center; gap: 12px; width: 100%;
  padding: 12px 14px; background: #1a1816; border: 1px solid rgba(250,246,240,0.1);
  border-radius: 14px; color: inherit; cursor: pointer; text-align: left;
  font: inherit; transition: border-color 0.15s, transform 0.15s;
}
.card-btn:hover { border-color: rgba(250,246,240,0.22); transform: translateY(-1px); }
.card-btn.active { border-color: rgba(155,176,212,0.45); background: #201e1c; }
.card-btn.featured { margin-bottom: 12px; border-color: rgba(155,176,212,0.28); }
.swatch {
  width: 36px; height: 36px; border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.12); flex-shrink: 0;
}
.card-btn h2 { font-size: 0.9rem; font-weight: 600; margin-bottom: 2px; }
.card-btn p { font-size: 0.76rem; color: #9a97a8; line-height: 1.35; }
.tag {
  margin-left: auto; font-size: 0.65rem; color: #9a97a8;
  border: 1px solid rgba(255,255,255,0.1); padding: 3px 7px; border-radius: 999px;
}
.family { margin-top: 18px; }
.family h2 {
  font-size: 0.72rem; text-transform: uppercase; letter-spacing: 0.1em;
  color: #948c84; margin-bottom: 8px;
}
.app {
  display: grid; grid-template-columns: minmax(280px, 380px) 1fr;
  gap: 28px; max-width: 1320px; margin: 0 auto; padding: 20px 16px 48px;
  align-items: start;
}
.viewer { min-width: 0; }
.viewer .page-header h1 { font-size: 1.65rem; }
.viewer-empty {
  padding: 48px 24px; text-align: center; color: #6e6b78;
  border: 1px dashed rgba(255,255,255,0.12); border-radius: 20px; margin-top: 24px;
}
.note {
  margin-top: 18px; padding: 12px 14px; border-radius: 12px;
  background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08);
  font-size: 0.8rem; color: #9a97a8; line-height: 1.45;
}
@media (max-width: 960px) {
  .app { grid-template-columns: 1fr; }
}
"""

    spa_js = """
var INDEX_THEMES = THEMES_DATA;

function applyTokens(phone, tokens) {
  Object.keys(tokens).forEach(function (key) {
    phone.style.setProperty('--' + key, tokens[key]);
  });
}

function showTheme(id) {
  var t = INDEX_THEMES[id];
  if (!t) return;

  document.getElementById('viewer-empty').style.display = 'none';
  document.getElementById('viewer-content').style.display = 'block';
  document.getElementById('theme-heading').textContent = t.heading;
  document.getElementById('theme-desc').textContent = t.desc;

  var bullets = document.getElementById('theme-bullets');
  bullets.innerHTML = '';
  (t.bullets || []).forEach(function (b) {
    var li = document.createElement('li');
    li.textContent = b;
    bullets.appendChild(li);
  });
  document.getElementById('theme-tweaks').style.display = (t.bullets && t.bullets.length) ? 'block' : 'none';

  var darkWrap = document.getElementById('dark-wrap');
  var lightWrap = document.getElementById('light-wrap');
  darkWrap.setAttribute('data-tweak', t.dark_tweak);
  darkWrap.setAttribute('data-surface', t.dark_surface);
  lightWrap.setAttribute('data-tweak', t.light_tweak);
  lightWrap.setAttribute('data-surface', t.light_surface);

  var darkPhone = document.getElementById('phone-dark');
  var lightPhone = document.getElementById('phone-light');
  applyTokens(darkPhone, t.dark);
  applyTokens(lightPhone, t.light);
  renderMock(darkPhone, t.dark_tweak, t.dark_surface);
  renderMock(lightPhone, t.light_tweak, t.light_surface);

  document.querySelectorAll('.card-btn').forEach(function (btn) {
    btn.classList.toggle('active', btn.getAttribute('data-id') === id);
  });

  if (location.hash !== '#' + id) {
    history.replaceState(null, '', '#' + id);
  }
}

document.querySelectorAll('.card-btn').forEach(function (btn) {
  btn.addEventListener('click', function () {
    showTheme(btn.getAttribute('data-id'));
  });
});

window.addEventListener('hashchange', function () {
  var id = location.hash.replace('#', '');
  if (id && INDEX_THEMES[id]) showTheme(id);
});

var startId = location.hash.replace('#', '') || 'slate-glow';
if (!INDEX_THEMES[startId]) startId = 'slate-glow';
showTheme(startId);
"""

    return f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>BOOP — Theme previews</title>
  {FONT_LINK}
  <style>
{PREVIEW_CSS}
{hub_css}
  </style>
</head>
<body>
  <div class="app">
    <aside class="hub">
      <h1>BOOP themes</h1>
      <p class="sub">Click any theme to preview dark &amp; light side by side. Everything runs in this one file — no extra pages needed.</p>
      {''.join(cards_html)}
      <p class="note">Tip: bookmark a theme with the URL hash, e.g. <strong>#rose-glow-primary</strong></p>
    </aside>
    <main class="viewer">
      <div id="viewer-empty" class="viewer-empty">Select a theme from the list</div>
      <div id="viewer-content" style="display:none">
        <header class="page-header">
          <h1 id="theme-heading"></h1>
          <p id="theme-desc"></p>
        </header>
        <section class="tweaks" id="theme-tweaks"><h3>Details</h3><ul id="theme-bullets"></ul></section>
        <div class="phones">
          <div class="phone-wrap" id="dark-wrap">
            <span class="mode-label">Dark</span>
            <div class="phone theme-dark" id="phone-dark"></div>
          </div>
          <div class="phone-wrap" id="light-wrap">
            <span class="mode-label">Light</span>
            <div class="phone theme-light" id="phone-light"></div>
          </div>
        </div>
      </div>
    </main>
  </div>
  <script>
var THEMES_DATA = {themes_json};
{MOCK_JS}
{spa_js}
  </script>
</body>
</html>
"""

ALL_THEMES = THEMES + BASE_THEMES

for t in THEMES:
    html = page(
        t["title"], t["heading"], t["desc"], t["bullets"],
        t["dark"], t["light"],
        t["dark_tweak"], t["light_tweak"],
        t["dark_surface"], t["light_surface"],
        standalone=t.get("standalone", False),
    )
    (ROOT / t["file"]).write_text(html)
    print("wrote", t["file"])

for t in BASE_THEMES:
    html = page(
        t.get("title", t["heading"]), t["heading"], t["desc"], t.get("bullets", []),
        t["dark"], t["light"],
        t["dark_tweak"], t["light_tweak"],
        t["dark_surface"], t["light_surface"],
        standalone=True,
    )
    (ROOT / t["file"]).write_text(html)
    print("wrote", t["file"])

(ROOT / "index.html").write_text(build_index(ALL_THEMES))
print("wrote index.html (single-page app)")

template = ROOT / "_template-glow.html"
if template.exists():
    template.unlink()

print("done")
