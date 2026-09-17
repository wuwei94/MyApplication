# Git 提交规范

> 提交信息规范、提交模板、本地钩子与历史遗留说明。
> 标题禁止负向叙事与复盘过程见 [delivery.md](delivery.md)。规范导航见 [README.md](README.md)。

## 提交信息格式

采用 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/v1.0.0/)：

```
<type>(<scope>): <subject>

正文（可选）：说明「为什么」而非复述改动

页脚（可选）：BREAKING CHANGE: <说明> / Closes #<issue>
```

### type 取值

| type       | 含义                                       |
| ---------- | ------------------------------------------ |
| `feat`     | 新增功能                                   |
| `fix`      | 修复缺陷                                   |
| `docs`     | 文档与注释                                 |
| `style`    | 格式调整，不影响代码逻辑（spotless/ktlint） |
| `refactor` | 重构，既不新增功能也不修复缺陷             |
| `perf`     | 性能优化                                   |
| `test`     | 测试代码                                   |
| `build`    | 构建系统或外部依赖变更                     |
| `ci`       | CI 配置与脚本                              |
| `chore`    | 其他杂项                                   |
| `revert`   | 回滚此前的提交                             |

### scope 取值

模块名或领域名，小写、连字符分词：

- 基础层：`basic_lib` / `basic_shared` / `basic_repo` / `build-logic`
- 功能模块：`module_<名称>`，如 `module_http` / `module_bluetooth`
- 库封装：`libs:lib_<名称>`，如 `libs:lib_okhttp`
- 其他：`app` / `flutter` / `ci` / `docs`

scope 可省略（`docs: ...` 这类全仓范围的改动）。

### 硬性规则

1. 标题长度分级：**≤ 72 字符**为理想值；73~100 字符仅打印提示；**超过 100 字符会被拒绝**（中文按 1 字符计）
2. 标题结尾**不加句号**
3. subject 用祈使句，只写「做了什么」；原因写进正文（提交约定，钩子不机械判定）
4. 一次提交只做一件事，不要把重构、格式化、新功能混在一起（提交约定，钩子不机械判定）
5. subject 与正文默认使用**中文**书写；type、scope 及专有技术术语（OkHttp、ktlint、ARouter 等）保留英文。**标题必须包含至少一个汉字**，否则提交被拒绝

机械校验（`tools/commit-msg`，不通过则拒绝提交）：标题非空、Conventional Commits 格式、长度 >100、结尾句号、标题至少含一个汉字。第 3、4 条靠交付自检与 Review。

## 快速开始

```bash
# 1. 安装钩子（pre-push + commit-msg）
# Windows：在 Git Bash 中执行（脚本依赖 Git for Windows 自带 bash）
./tools/install-git-hooks.sh

# 2. 启用提交模板，git commit 不带 -m 时自动套用格式说明
git config commit.template .gitmessage

# 3. 批量格式化提交不污染 git blame
git config blame.ignoreRevsFile .git-blame-ignore-revs
```

安装脚本会把 `tools/pre-push` 与 `tools/commit-msg` 复制到 `.git/hooks/`。

**临时跳过（仅限人工明确决策）**：`git commit --no-verify` / `git push --no-verify`，或 `COMMIT_MSG_DISABLE=1 git commit`（仅跳过提交信息校验）。Agent 交付纠错不得使用这些开关掩盖门禁失败（见 [delivery.md](delivery.md)）。

## 历史遗留

统计时点 2026-09-17：仓库约 500+ 条提交，其中 **2026-08-12 之前的提交不符合本规范**（典型如 `first commit`、`commit`、`flutter ci` ×7、`整理 module`）。

**决定：不重写这部分历史。** 理由：

1. 个人项目，无团队协作，历史日志的检索价值有限
2. 重写会改变该批历史提交的全部 hash，GitHub 上已有的链接与引用全部失效，且需要 force push
3. 早期提交本身信息量极低，即使重写消息也补不回有效上下文

后续如需检索早期改动，用内容检索而非提交信息：

```bash
git log -S "关键字" --oneline        # 按改动内容检索
git log --oneline -- path/to/file    # 按文件看演进
git log --oneline --before=2026-08-12
```

若未来确需清理（例如开源前），再单独评估 `git filter-repo` 重写方案，届时一次性处理并 force push。

## 其他约定

- **批量格式化提交**（`style(spotless): ...`）必须将其 hash 追加到 `.git-blame-ignore-revs`，避免 `git blame` 全部指向格式化者
- **线性历史**：本仓库无 merge commit。多人协作时用 `git pull --rebase`，冲突在本地解决后再推送
- **pre-push 门禁**：推送前自动执行 `spotlessCheck` 与变更模块的 `lintProdDebug`，两处 Gradle 调用均传 `-PenableFlutter=false`（与 CI 口径一致，Flutter 侧由 `flutter.yml` 独立流水线负责），详见 `tools/pre-push`
