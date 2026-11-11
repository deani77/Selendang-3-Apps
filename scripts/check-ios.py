#!/usr/bin/env python3
"""Run the same standalone Swift checks locally and in CI; no app signing required."""
import argparse
from pathlib import Path
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "ios" / "Selendang3"
TESTS = ROOT / "ios" / "Tests"
# Extensions are compiled with their owning model, including future dashboard splits.
MODELS = [SOURCE / "ProductionData.swift", SOURCE / "EstateReference.swift",
          *sorted(SOURCE.glob("EstateDashboardData*.swift"))]
SUITES = {
    "production": ([SOURCE / "ProductionData.swift"], [TESTS / "ProductionChecks.swift"]),
    "reference": (MODELS, [TESTS / "EstateReferenceChecks.swift"]),
    "dashboard": (MODELS + [SOURCE / "CropProductionTableData.swift"],
                  [TESTS / "EstateDashboardChecks.swift", *sorted(TESTS.glob("DashboardSummaryChecks.swift"))]),
    "crop-history": (MODELS, [TESTS / "CropHistoryChecks.swift"]),
    "tools": ([SOURCE / "PlanterCalculations.swift"], [TESTS / "PlanterToolsChecks.swift"]),
    "sync": (MODELS + [SOURCE / "EstateSyncTransport.swift"], [TESTS / "SyncTransportChecks.swift"]),
}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("suite", nargs="?", default="all", choices=["all", *SUITES])
    parser.add_argument("--list", action="store_true", help="Show source files without running Swift")
    args = parser.parse_args()
    selected = SUITES if args.suite == "all" else {args.suite: SUITES[args.suite]}
    with tempfile.TemporaryDirectory(prefix="s3-ios-checks-") as directory:
        for name, (sources, tests) in selected.items():
            files = sources + tests
            if args.list:
                print(name + ": " + ", ".join(str(path.relative_to(ROOT)) for path in files))
                continue
            print("Checking " + name, flush=True)
            combined = Path(directory) / (name + ".swift")
            combined.write_text("\n".join(path.read_text(encoding="utf-8") for path in files), encoding="utf-8")
            subprocess.run(["swift", str(combined)], cwd=ROOT, check=True)
        if not args.list and args.suite in ("all", "crop-history"):
            subprocess.run([sys.executable, str(ROOT / "scripts/test-crop-history.py")], cwd=ROOT, check=True)


if __name__ == "__main__":
    main()
