#!/usr/bin/env python3
"""Point Stitch code.html at local vendor/tailwindcss.js and vendor/silent-order-fonts.css."""
from __future__ import annotations

import re
import sys
from pathlib import Path

MARKER = "<!-- offline-vendor -->"
INJECT = f"""{MARKER}
<script src="../../vendor/tailwindcss.js"></script>
<link href="../../vendor/silent-order-fonts.css" rel="stylesheet"/>
"""


def patch(html: str) -> str:
    if MARKER in html:
        return html
    html = re.sub(r"<link[^>]*fonts\.googleapis\.com[^>]*/>\s*", "", html, flags=re.I)
    html = re.sub(
        r'<script\s+src="https://cdn\.tailwindcss\.com[^"]*">\s*</script>\s*',
        "",
        html,
        flags=re.I,
    )
    html = re.sub(r"<head>", "<head>\n" + INJECT, html, count=1, flags=re.I)
    return html


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    stitch = root / "app" / "src" / "main" / "assets" / "stitch"
    n = 0
    for path in sorted(stitch.glob("**/code.html")):
        text = path.read_text(encoding="utf-8")
        new = patch(text)
        if new != text:
            path.write_text(new, encoding="utf-8")
            n += 1
    print(f"patched {n} code.html files under {stitch}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
