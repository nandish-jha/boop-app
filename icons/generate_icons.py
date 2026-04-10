#!/usr/bin/env python3
"""Generate ProDash minimal app icons.

Design: A single rounded 'P' letterform — minimal, geometric, reads well at 48px.
Outputs:
  - icon-192.png, icon-512.png           (web PWA, transparent)
  - icon-maskable-512.png                (PWA maskable, full-bleed neutral bg)
  - ic_launcher_fg.png (monochrome)      (Android adaptive foreground, for Material You themed icon)
  - ic_launcher_bg.png solid color       (Android adaptive background)
"""
from PIL import Image, ImageDraw
import os

OUT = os.path.dirname(os.path.abspath(__file__))

# Color tokens
FG_WHITE = (245, 245, 245, 255)
BG_DARK  = (18, 18, 18, 255)          # app bg for web icon
BG_TRANSPARENT = (0, 0, 0, 0)

def draw_p_mark(size, fg, bg, padding_ratio=0.18, rounded_bg=True, bg_radius_ratio=0.22):
    """Draw a clean geometric uppercase 'P' mark with rounded-rect background.

    Uppercase P: stem spans full cap height; bowl sits on the top half,
    attached to the stem's upper portion so the lower stem reads as the
    descending vertical of the P (not a 'p' descender).
    """
    # Render at 4x supersample and downscale for smooth edges.
    SCALE = 4
    S = size * SCALE
    img = Image.new('RGBA', (S, S), BG_TRANSPARENT)
    d = ImageDraw.Draw(img)

    # Background
    if bg[3] > 0:
        if rounded_bg:
            r = int(S * bg_radius_ratio)
            d.rounded_rectangle([(0,0),(S-1,S-1)], radius=r, fill=bg)
        else:
            d.rectangle([(0,0),(S,S)], fill=bg)

    # Inner cap-height box
    pad = int(S * padding_ratio)
    inner_w = S - 2*pad
    inner_h = int(inner_w * 1.05)  # letter slightly taller than wide
    # Center vertically
    x0 = pad
    y0 = (S - inner_h) // 2

    # Stroke weight — bold geometric
    stroke = max(2, int(inner_w * 0.22))

    # Vertical stem — full cap height, rounded caps
    stem_x0 = x0
    stem_x1 = x0 + stroke
    stem_y0 = y0
    stem_y1 = y0 + inner_h
    d.rounded_rectangle([stem_x0, stem_y0, stem_x1, stem_y1],
                        radius=stroke//2, fill=fg)

    # Bowl — attached to top of stem, occupies upper ~60% of cap height.
    bowl_h = int(inner_h * 0.60)
    bowl_w = int(inner_w * 0.82)   # slightly narrower than full width
    bowl_x0 = stem_x0
    bowl_y0 = y0
    bowl_x1 = bowl_x0 + bowl_w
    bowl_y1 = bowl_y0 + bowl_h
    # Outer bowl (filled rounded rect — reads as a proper P bowl)
    d.rounded_rectangle([bowl_x0, bowl_y0, bowl_x1, bowl_y1],
                        radius=bowl_h//2, fill=fg)
    # Inner cutout — transparent for adaptive/monochrome, bg-colored otherwise.
    hole_pad = stroke
    hole_color = bg if bg[3] > 0 else (0, 0, 0, 0)
    hole_x0 = bowl_x0 + stroke + hole_pad
    hole_y0 = bowl_y0 + hole_pad
    hole_x1 = bowl_x1 - hole_pad
    hole_y1 = bowl_y1 - hole_pad
    if hole_color[3] == 0:
        # Carve transparency: multiply alpha by inverted hole mask.
        from PIL import ImageChops
        hole_mask = Image.new('L', (S, S), 0)
        ImageDraw.Draw(hole_mask).rounded_rectangle(
            [hole_x0, hole_y0, hole_x1, hole_y1],
            radius=(hole_y1 - hole_y0) // 2, fill=255)
        inv = hole_mask.point(lambda v: 255 - v)
        new_alpha = ImageChops.multiply(img.getchannel('A'), inv)
        img.putalpha(new_alpha)
    else:
        d.rounded_rectangle([hole_x0, hole_y0, hole_x1, hole_y1],
                            radius=(hole_y1 - hole_y0) // 2, fill=hole_color)

    # Downscale with LANCZOS for clean edges
    return img.resize((size, size), Image.LANCZOS)

# === PWA icons (transparent bg + rounded-rect dark surface) ===
for px in (192, 512):
    im = draw_p_mark(px, FG_WHITE, BG_DARK, padding_ratio=0.22, bg_radius_ratio=0.22)
    im.save(os.path.join(OUT, f'icon-{px}.png'), optimize=True)

# === Maskable icon — full-bleed dark bg, tighter padding for safe zone ===
im = draw_p_mark(512, FG_WHITE, BG_DARK, padding_ratio=0.28, rounded_bg=False)
im.save(os.path.join(OUT, 'icon-maskable-512.png'), optimize=True)

# === Android adaptive icon foreground — MONOCHROME (white on transparent) ===
# Android adaptive canvas is 108dp; inner safe zone is 66dp (central 61%).
# Render at 432px. Keep letter inside safe zone, so padding ≈ 0.30.
im_fg = draw_p_mark(432, FG_WHITE, (0,0,0,0), padding_ratio=0.30, rounded_bg=False)
im_fg.save(os.path.join(OUT, 'ic_launcher_fg.png'), optimize=True)
# Material You monochrome layer — identical silhouette, system tints it.
im_fg.save(os.path.join(OUT, 'ic_launcher_monochrome.png'), optimize=True)

print("Icons generated:")
for f in sorted(os.listdir(OUT)):
    p = os.path.join(OUT, f)
    if f.endswith('.png'):
        print(f"  {f}  {os.path.getsize(p)//1024} KB")
