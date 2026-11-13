#!/usr/bin/env python3
"""Prints what an APK is made of, so a size change can be judged instead of guessed.

The report becomes an annotation of the run (visible in the UI and in `gh run view`), because the
build artifacts themselves are not always downloadable.
"""
import sys
import zipfile
from collections import defaultdict

GROUPS = (
    ("dex (code)", lambda name: name.endswith(".dex")),
    ("resources", lambda name: name.startswith("res/")),
    ("assets", lambda name: name.startswith("assets/")),
    ("kotlin metadata", lambda name: name.startswith("META-INF/")),
    ("manifest", lambda name: name == "AndroidManifest.xml"),
)


def main() -> int:
    for path in sys.argv[1:]:
        report(path)
    return 0


def report(path: str) -> None:
    with zipfile.ZipFile(path) as apk:
        entries = sorted(apk.infolist(), key=lambda entry: -entry.compress_size)

    total = sum(entry.compress_size for entry in entries)
    groups: dict = defaultdict(int)
    for entry in entries:
        for label, matches in GROUPS:
            if matches(entry.filename):
                groups[label] += entry.compress_size
                break
        else:
            groups["other"] += entry.compress_size

    breakdown = ", ".join(
        f"{label} {size // 1024} KiB" for label, size in sorted(groups.items(), key=lambda item: -item[1])
    )
    largest = " | ".join(
        f"{entry.filename} {entry.compress_size // 1024} KiB" for entry in entries[:10]
    )

    import os

    print(f"::notice title=APK size::{os.path.basename(path)} is {os.path.getsize(path) // 1024} KiB "
          f"({total // 1024} KiB of contents): {breakdown}. Largest entries: {largest}")


if __name__ == "__main__":
    raise SystemExit(main())
