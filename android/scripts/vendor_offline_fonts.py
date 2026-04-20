#!/usr/bin/env python3
"""Download Google Fonts CSS + woff2 into assets/vendor and emit silent-order-fonts.css."""
from __future__ import annotations

import hashlib
import re
import sys
import urllib.request
from pathlib import Path

UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"


def fetch(url: str) -> str:
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=120) as r:
        return r.read().decode("utf-8")


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    vendor = root / "app" / "src" / "main" / "assets" / "vendor"
    fonts_dir = vendor / "fonts"
    fonts_dir.mkdir(parents=True, exist_ok=True)

    css_parts = [
        fetch(
            "https://fonts.googleapis.com/css2?"
            "family=Inter:wght@300;400;500;600;700;800&"
            "family=Manrope:wght@200;300;400;500;600;700;800&"
            "subset=latin&display=swap"
        ),
        fetch(
            "https://fonts.googleapis.com/css2?"
            "family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap"
        ),
    ]
    combined = "\n\n".join(css_parts)

    urls = sorted(set(re.findall(r"url\((https://fonts\.gstatic\.com[^)]+)\)", combined)))
    mapping: dict[str, str] = {}
    for u in urls:
        digest = hashlib.sha256(u.encode()).hexdigest()[:14]
        name = f"{digest}.woff2"
        dest = fonts_dir / name
        if not dest.exists():
            req = urllib.request.Request(u, headers={"User-Agent": UA})
            with urllib.request.urlopen(req, timeout=120) as r:
                dest.write_bytes(r.read())
        mapping[u] = f"fonts/{name}"

    def repl(m: re.Match[str]) -> str:
        u = m.group(1)
        return f"url({mapping[u]})"

    out_css = re.sub(r"url\((https://fonts\.gstatic\.com[^)]+)\)", repl, combined)
    (vendor / "silent-order-fonts.css").write_text(out_css, encoding="utf-8")
    print(f"wrote {vendor / 'silent-order-fonts.css'} ({len(mapping)} woff2 files)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
