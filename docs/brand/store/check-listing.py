#!/usr/bin/env python3
"""Check the Play listing copy against Google's field limits.

Play silently truncates or rejects over-length fields, and counting by eye is
how you end up one character over. Run this after editing listing.md.

    python3 docs/brand/store/check-listing.py
"""
import pathlib
import re
import sys

LIMITS = {
    "Title": 30,
    "Short description": 80,
    "Full description": 4000,
}

listing = pathlib.Path(__file__).with_name("listing.md").read_text()

# Each field is a "## <name> (max N)" heading followed by a fenced block.
pattern = re.compile(
    r"^## (?P<name>[^(\n]+?) \(max (?P<limit>\d+)\)\s*\n+```\n(?P<body>.*?)\n```",
    re.MULTILINE | re.DOTALL,
)

found = {}
for m in pattern.finditer(listing):
    found[m.group("name").strip()] = (m.group("body"), int(m.group("limit")))

failures = 0
for name, limit in LIMITS.items():
    if name not in found:
        print(f"MISSING  {name}")
        failures += 1
        continue
    body, declared = found[name]
    if declared != limit:
        print(f"WARN     {name}: heading says max {declared}, expected {limit}")
        failures += 1
    n = len(body)
    status = "ok  " if n <= limit else "OVER"
    if n > limit:
        failures += 1
    print(f"{status}     {name}: {n} / {limit}")

# The store description must not promise anything the app cannot do.
body = found.get("Full description", ("", 0))[0].lower()
for claim in ("sync", "cloud backup", "account", "sign in", "premium"):
    if claim in body and "no account" not in body:
        print(f"WARN     full description mentions '{claim}'")

sys.exit(1 if failures else 0)
