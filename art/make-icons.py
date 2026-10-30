#!/usr/bin/env python3
"""Regenerate the launcher icon and TV banner in res/ from art/icon.png. Needs Pillow."""
import os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

src = Image.open(os.path.join(ROOT, "art", "icon.png")).convert("RGBA")
for density, px in SIZES.items():
    out = os.path.join(ROOT, "res", f"mipmap-{density}")
    os.makedirs(out, exist_ok=True)
    src.resize((px, px), Image.LANCZOS).save(os.path.join(out, "ic_launcher.png"), optimize=True)

# TV launchers show a 16:9 banner (320x180 at xhdpi). The icon's background is black, so
# center it on black.
banner = Image.new("RGBA", (320, 180), (0, 0, 0, 255))
banner.alpha_composite(src.resize((172, 172), Image.LANCZOS), (74, 4))
out = os.path.join(ROOT, "res", "drawable-xhdpi")
os.makedirs(out, exist_ok=True)
banner.save(os.path.join(out, "banner.png"), optimize=True)
