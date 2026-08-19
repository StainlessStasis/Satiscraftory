#!/usr/bin/env python3
"""
DISCLAIMER: This tool was made using generative AI (Claude).
I have reviewed it to double check that it isn't doing anything stupid.
However, I have not touched Python in years so my knowledge of it is very limited.
You shouldn't need to ever use this, as it only exists to save me time for making the wiki - but if you do use it, use it at your own risk.

--------------------------------------------------

Scans every .mdx file under the wiki docs root and fills in width/height on
any <Asset location="..."/> tag that's missing them, using the referenced
image's real pixel dimensions - so you can just write:

  <Asset location="screenshots:build_menu"/>

and running this script turns it into:

  <Asset location="screenshots:build_menu" width={1244} height={838} />

Tags that already specify width and/or height are left untouched - if you
want to override a dimension by hand, this script won't fight you on it.
Safe to re-run any time after adding new screenshots or Asset tags.

Only PNG and GIF are supported (read straight from the file header, no
Pillow dependency needed) - matches the two formats the wiki itself
explicitly documents for assets.
"""

import re
import struct
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent  # run from repo root, or edit this
DOCS_ROOT = REPO_ROOT / "main" / "wiki"
ASSETS_ROOT = DOCS_ROOT / "assets"

# Matches a full self-closing <Asset .../> tag, attributes in any order/case.
ASSET_TAG_RE = re.compile(r"<Asset\b[^>]*?/>", re.DOTALL)
LOCATION_RE = re.compile(r'location\s*=\s*"([^"]+)"')
# Matches an existing width=/height= *attribute* specifically (JSX {..} or
# quoted string) - won't false-positive on something like style={{width: ...}}
# since that uses a colon, not an equals sign, right after the word.
WIDTH_RE = re.compile(r"\bwidth\s*=")
HEIGHT_RE = re.compile(r"\bheight\s*=")


def image_size(path: Path) -> tuple[int, int]:
    """Reads width/height straight from the file header."""
    with open(path, "rb") as f:
        head = f.read(26)
    if head[:8] == b"\x89PNG\r\n\x1a\n":
        return struct.unpack(">II", head[16:24])
    if head[:6] in (b"GIF87a", b"GIF89a"):
        return struct.unpack("<HH", head[6:10])
    raise ValueError(f"unrecognized image format (only PNG/GIF supported): {path}")


def resolve_asset_path(location: str) -> Path:
    """'namespace:some/path' -> assets/namespace/some/path.png (default ext).
    'namespace:some/path.gif' -> assets/namespace/some/path.gif (explicit ext
    kept as-is, matching how the wiki itself resolves asset locations)."""
    namespace, sep, path = location.partition(":")
    if not sep or not namespace or not path:
        raise ValueError(f"location must be 'namespace:path', got {location!r}")
    basename = path.rsplit("/", 1)[-1]
    if "." in basename:
        return ASSETS_ROOT / namespace / path
    return ASSETS_ROOT / namespace / f"{path}.png"


def fill_tag(tag: str) -> str | None:
    """Returns the tag with width/height inserted, or None if it already has
    them, or its location/image can't be resolved (left untouched either way,
    with a reason printed for the latter case)."""
    if WIDTH_RE.search(tag) or HEIGHT_RE.search(tag):
        return None  # already sized by hand - don't touch it

    m = LOCATION_RE.search(tag)
    if not m:
        print(f"[skip] {tag.strip()} - no location attribute found")
        return None
    location = m.group(1)

    try:
        asset_path = resolve_asset_path(location)
    except ValueError as e:
        print(f"[skip] {tag.strip()} - {e}")
        return None

    if not asset_path.exists():
        print(f"[skip] {tag.strip()} - no file at {asset_path.relative_to(DOCS_ROOT)}")
        return None

    try:
        width, height = image_size(asset_path)
    except ValueError as e:
        print(f"[skip] {tag.strip()} - {e}")
        return None

    insert = f" width={{{width}}} height={{{height}}}"
    stripped = tag.rstrip()
    if not stripped.endswith("/>"):
        print(f"[skip] {tag.strip()} - not a self-closing tag, can't safely edit")
        return None
    body = stripped[:-2].rstrip()
    return f"{body}{insert} />"


def process_file(mdx_file: Path) -> int:
    text = mdx_file.read_text(encoding="utf-8")
    changed = 0

    def replace(match: re.Match) -> str:
        nonlocal changed
        new_tag = fill_tag(match.group(0))
        if new_tag is None:
            return match.group(0)
        changed += 1
        return new_tag

    new_text = ASSET_TAG_RE.sub(replace, text)
    if changed:
        mdx_file.write_text(new_text, encoding="utf-8")
        print(f"[updated] {mdx_file.relative_to(DOCS_ROOT)} ({changed} tag(s))")
    return changed


def main():
    total = 0
    for mdx_file in sorted(DOCS_ROOT.rglob("*.mdx")):
        total += process_file(mdx_file)
    print(f"\nDone - filled in {total} Asset tag(s).")


if __name__ == "__main__":
    main()