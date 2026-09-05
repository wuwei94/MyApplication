#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
将 Jetpack Macrobenchmark 输出的 benchmarkData.json 汇总为可读的 Markdown 报告。

默认行为：递归查找当前目录下所有 build/outputs/**/benchmarkData.json；
也可通过命令行参数显式指定文件或目录。

示例：
    ./tools/benchmark-report.py
    ./tools/benchmark-report.py benchmarks/build/outputs/connected_check
    ./tools/benchmark-report.py path/to/run1/benchmarkData.json path/to/run2/benchmarkData.json

报告字段优先级（同一指标可能仅输出其中若干个）：
    minimum / median / mean / stdDev / maximum / p50 / p90 / p95 / p99
"""

from __future__ import annotations

import argparse
import glob
import json
import os
import sys
from dataclasses import dataclass
from pathlib import Path

STAT_ORDER = ("minimum", "median", "mean", "stdDev", "p50", "p90", "p95", "p99", "maximum")

PARAM_ORDER = ("compilationMode", "iterations", "startupMode", "targetPackage")

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_GLOB = "**/build/outputs/**/benchmarkData.json"


@dataclass(frozen=True)
class MetricStat:
    name: str
    suffix: str = ""
    precision: int = 3


def discover_inputs(targets: list[str]) -> list[Path]:
    files: list[Path] = []
    for target in targets:
        p = Path(target)
        if p.is_file():
            files.append(p)
        elif p.is_dir():
            for child in p.rglob("benchmarkData.json"):
                files.append(child)
        else:
            for matched in glob.glob(target, recursive=True):
                files.append(Path(matched))
    if not files:
        files.extend(Path(ROOT).glob(DEFAULT_GLOB))
    return sorted(set(files))


def load_records(files: list[Path]) -> list[dict]:
    records = []
    for f in files:
        try:
            data = json.loads(f.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as e:
            print(f"# WARN: 跳过 {f}: {e}", file=sys.stderr)
            continue
        run_label = f.relative_to(ROOT).as_posix() if f.is_relative_to(ROOT) else str(f)
        for bench in data.get("benchmarks", []):
            records.append(
                {
                    "run": run_label,
                    "className": bench.get("className", bench.get("testClassName", "")),
                    "name": bench.get("name", bench.get("testName", "?")),
                    "params": bench.get("params", {}) or {},
                    "metrics": bench.get("metrics", {}) or {},
                }
            )
    return records


def stat_keys(records: list[dict]) -> list[str]:
    found: list[str] = []
    for rec in records:
        for key in rec["metrics"].keys():
            if key not in found:
                found.append(key)
    return found


def fmt_metric_value(value) -> str:
    if isinstance(value, float):
        if abs(value) >= 100 or abs(value) < 0.01:
            return f"{value:.3e}"
        return f"{value:.3f}"
    if isinstance(value, int):
        return str(value)
    return str(value)


def metric_row(metrics: dict, stat_keys: list[str], suffix: str = "", precision: int = 3) -> str:
    parts = []
    for key in stat_keys:
        if key in metrics:
            parts.append(f"{key}={fmt_metric_value(metrics[key])}")
    return " ".join(parts) if parts else "(无统计值)"


def params_text(params: dict) -> str:
    if not params:
        return ""
    keys = [k for k in PARAM_ORDER if k in params] + [k for k in params if k not in PARAM_ORDER]
    return ", ".join(f"{k}={params[k]}" for k in keys)


def render_markdown(records: list[dict]) -> str:
    out: list[str] = []
    out.append("# Benchmark Report")
    out.append("")
    if not records:
        out.append("> 未找到任何基准数据。请先运行 `./gradlew :benchmarks:connectedCheck` 或显式传入 JSON 路径。")
        return "\n".join(out)

    runs = sorted({r["run"] for r in records})
    out.append(f"- 数据来源：{len(runs)} 次运行，共 {len(records)} 个基准")
    for r in runs:
        out.append(f"  - `{r}`")
    out.append("")

    by_class: dict[str, list[dict]] = {}
    for rec in records:
        by_class.setdefault(rec["className"] or "(未命名)", []).append(rec)

    for class_name, items in sorted(by_class.items()):
        out.append(f"## `{class_name}`")
        out.append("")
        grouped: dict[tuple, list[dict]] = {}
        for rec in items:
            key = tuple(sorted(rec["params"].items()))
            grouped.setdefault(key, []).append(rec)
        for params_key, runs_in_group in sorted(grouped.items(), key=lambda kv: kv[0]):
            if params_key:
                out.append(f"### 参数：{params_text(dict(params_key))}")
                out.append("")
            for rec in runs_in_group:
                out.append(f"- **{rec['name']}** (`{rec['run']}`)")
                if rec["metrics"]:
                    for m_name, m_value in sorted(rec["metrics"].items()):
                        if not isinstance(m_value, dict):
                            out.append(f"    - `{m_name}`: {m_value}")
                            continue
                        ordered = [k for k in STAT_ORDER if k in m_value]
                        ordered += [k for k in m_value if k not in ordered]
                        cells = ", ".join(f"{k}={fmt_metric_value(m_value[k])}" for k in ordered)
                        out.append(f"    - `{m_name}`: {cells}")
                else:
                    out.append("    - _(无指标数据)_")
            out.append("")

    out.append("---")
    out.append("> 字段优先级：minimum → median → mean → stdDev → p50/p90/p95/p99 → maximum。")
    out.append("> 时间单位由指标后缀决定：`timeToInitialDisplayMs` 为毫秒；FrameTimingMetric 指标为毫秒；JankStats 自报指标由 module_performance/JankStatsActivity 输出。")
    return "\n".join(out)


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("inputs", nargs="*", help="benchmarkData.json 路径或包含它的目录；缺省时按项目默认 glob 检索")
    parser.add_argument("-o", "--out", help="输出到指定 Markdown 文件，缺省打印到 stdout")
    args = parser.parse_args(argv)

    files = discover_inputs(args.inputs or [])
    if not files:
        print("ERROR: 未发现任何 benchmarkData.json。请先生成或显式传入路径。", file=sys.stderr)
        return 1
    records = load_records(files)
    markdown = render_markdown(records)

    if args.out:
        Path(args.out).write_text(markdown, encoding="utf-8")
        print(f"已写入 {args.out}（{len(records)} 条记录）", file=sys.stderr)
    else:
        sys.stdout.write(markdown)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))