#!/usr/bin/env python3
"""Download remote <img src="https://..."> into assets/vendor/img and rewrite Stitch HTML."""
from __future__ import annotations

import hashlib
import re
import sys
import urllib.request
from pathlib import Path

UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
IMG_SRC_RE = re.compile(r'src="(https://[^"]+)"')


def sniff_ext(data: bytes, content_type: str) -> str:
    ct = (content_type or "").split(";")[0].strip().lower()
    if "jpeg" in ct or "jpg" in ct:
        return ".jpg"
    if "png" in ct:
        return ".png"
    if "webp" in ct:
        return ".webp"
    if "gif" in ct:
        return ".gif"
    if "avif" in ct:
        return ".avif"
    if len(data) >= 3 and data[:3] == b"\xff\xd8\xff":
        return ".jpg"
    if len(data) >= 8 and data[:8] == b"\x89PNG\r\n\x1a\n":
        return ".png"
    if len(data) >= 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP":
        return ".webp"
    return ".bin"


def fetch(url: str) -> tuple[bytes, str]:
    req = urllib.request.Request(url, headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=180) as r:
        return r.read(), r.headers.get("Content-Type", "")


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    stitch = root / "app" / "src" / "main" / "assets" / "stitch"
    img_dir = root / "app" / "src" / "main" / "assets" / "vendor" / "img"
    img_dir.mkdir(parents=True, exist_ok=True)

    urls: set[str] = set()
    for html in stitch.glob("**/code.html"):
        for m in IMG_SRC_RE.finditer(html.read_text(encoding="utf-8")):
            if m.group(1).startswith("https://"):
                urls.add(m.group(1))

    mapping: dict[str, str] = {}
    for url in sorted(urls):
        digest = hashlib.sha256(url.encode()).hexdigest()[:16]
        data, ct = fetch(url)
        ext = sniff_ext(data, ct)
        name = f"i{digest}{ext}"
        dest = img_dir / name
        if not dest.exists() or dest.stat().st_size == 0:
            dest.write_bytes(data)
        mapping[url] = f"../../vendor/img/{name}"
        print(f"OK {name} ({len(data)} bytes) <- {url[:72]}...")

    n = 0
    for html in sorted(stitch.glob("**/code.html")):
        text = html.read_text(encoding="utf-8")
        new = text
        for remote, local in mapping.items():
            new = new.replace(f'src="{remote}"', f'src="{local}"')
        if new != text:
            html.write_text(new, encoding="utf-8")
            n += 1
    print(f"rewrote images in {n} code.html files; {len(mapping)} unique URLs")
    return 0


if __name__ == "__main__":
    sys.exit(main())
