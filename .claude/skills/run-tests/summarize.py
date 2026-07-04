#!/usr/bin/env python3
"""Parse JUnit XML from the Gradle `test` task and print an actionable summary.

Gradle's own stdout truncates per-test failure detail, and the IDE run-configuration
tool often returns an empty output snapshot. Both problems are avoided by reading the
XML that `build/test-results/test/` always writes in full.

Usage: summarize.py [ClassNameFilter] [max_failures_shown]
  ClassNameFilter: substring of the test class file name, e.g. "SnippetsTest".
                   Omit or pass "" to include every result file (the whole suite).
  max_failures_shown: cap on how many failing tests to list (default 40).
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET

RESULTS_DIR = "build/test-results/test"


def main() -> int:
    class_filter = sys.argv[1] if len(sys.argv) > 1 else ""
    max_shown = int(sys.argv[2]) if len(sys.argv) > 2 else 40

    pattern = os.path.join(RESULTS_DIR, f"TEST-*{class_filter}*.xml")
    files = sorted(glob.glob(pattern))
    if not files:
        print(f"No result XML matched {pattern}")
        print("Did the test task run? (check the gradle output above)")
        return 2

    total = failures = errors = skipped = 0
    failed = []  # (testcase_name, first message line)

    for path in files:
        root = ET.parse(path).getroot()
        # A file may be a <testsuite> or a <testsuites> wrapper.
        suites = [root] if root.tag == "testsuite" else root.findall("testsuite")
        for suite in suites:
            total += int(suite.get("tests", 0))
            failures += int(suite.get("failures", 0))
            errors += int(suite.get("errors", 0))
            skipped += int(suite.get("skipped", 0))
            for tc in suite.findall("testcase"):
                for kind in ("failure", "error"):
                    node = tc.find(kind)
                    if node is None:
                        continue
                    msg = (node.get("message") or (node.text or "")).strip()
                    first = msg.splitlines()[0] if msg else "(no message)"
                    classname = tc.get("classname", "")
                    name = tc.get("name", "?")
                    label = f"{classname}.{name}" if classname else name
                    failed.append((label, first))

    passed = total - failures - errors - skipped
    status = "PASS" if (failures == 0 and errors == 0) else "FAIL"
    print(f"[{status}] {total} tests | {passed} passed | "
          f"{failures} failures | {errors} errors | {skipped} skipped")

    if failed:
        print(f"\nFailing tests ({len(failed)}, showing up to {max_shown}):")
        for label, first in failed[:max_shown]:
            print(f"  - {label}\n      {first}")
        if len(failed) > max_shown:
            print(f"  ... and {len(failed) - max_shown} more")

    return 0 if status == "PASS" else 1


if __name__ == "__main__":
    sys.exit(main())
