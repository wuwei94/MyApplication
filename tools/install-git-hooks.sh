#!/usr/bin/env bash
#
# 把 tools/ 下的 Git 钩子安装到 .git/hooks（覆盖同名文件）。
#
# 用法：./tools/install-git-hooks.sh
#
# 钩子由 Git 通过 Git for Windows 自带的 bash 执行，Windows 下同样可用。

set -euo pipefail

cd "$(git rev-parse --show-toplevel)" || exit 1

hooks_dir="$(git rev-parse --git-path hooks)"
mkdir -p "$hooks_dir"

for hook in tools/pre-push; do
    name="$(basename "$hook")"
    cp "$hook" "$hooks_dir/$name"
    chmod +x "$hooks_dir/$name"
    echo "已安装 $name -> $hooks_dir/$name"
done

echo ""
echo "卸载：rm $(git rev-parse --git-path hooks)/pre-push"
echo "单次跳过：git push --no-verify"
