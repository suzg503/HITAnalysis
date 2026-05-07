---
version: alpha
name: HITAnalysis
description: 医疗数据AI智能分析平台设计系统 — 专业严谨、科技智能、数据密集
colors:
  # 深色背景系统
  bg-marketing: "#08090a"
  bg-panel: "#0f1011"
  bg-surface: "#191a1b"
  bg-elevated: "#28282c"
  bg-input: "rgba(255,255,255,0.02)"
  
  # 文本系统
  text-primary: "#f7f8f8"
  text-secondary: "#d0d6e0"
  text-tertiary: "#8a8f98"
  text-muted: "#62666d"
  
  # 品牌色（医疗蓝色系）
  brand-primary: "#3B82F6"
  brand-hover: "#2563EB"
  brand-active: "#1D4ED8"
  brand-light: "#60A5FA"
  brand-subtle: "rgba(59,130,246,0.15)"
  
  # 语义色（医疗行业标准）
  success: "#10B981"
  success-light: "#34D399"
  warning: "#F59E0B"
  warning-light: "#FBBF24"
  error: "#EF4444"
  error-light: "#F87171"
  info: "#3B82F6"
  
  # AI 特色
  ai-gradient-start: "#3B82F6"
  ai-gradient-end: "#8B5CF6"
  ai-glow: "rgba(59,130,246,0.4)"
  
  # 边框系统
  border-subtle: "rgba(255,255,255,0.05)"
  border-standard: "rgba(255,255,255,0.08)"
  border-strong: "rgba(255,255,255,0.12)"
  border-brand: "rgba(59,130,246,0.3)"
  
  # 图表色板
  chart-1: "#3B82F6"
  chart-2: "#10B981"
  chart-3: "#F59E0B"
  chart-4: "#EF4444"
  chart-5: "#8B5CF6"
  chart-6: "#06B6D4"
  chart-7: "#F97316"
  chart-8: "#64748B"

typography:
  # 标题系统
  h1:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "32px"
    fontWeight: 590
    lineHeight: 1.13
    letterSpacing: "-0.704px"
  h2:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "24px"
    fontWeight: 510
    lineHeight: 1.33
    letterSpacing: "-0.288px"
  h3:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "20px"
    fontWeight: 590
    lineHeight: 1.33
    letterSpacing: "-0.24px"
  h4:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "16px"
    fontWeight: 510
    lineHeight: 1.50
  
  # 正文系统
  body-lg:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "18px"
    fontWeight: 400
    lineHeight: 1.60
    letterSpacing: "-0.165px"
  body-md:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: 1.50
  body-sm:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "15px"
    fontWeight: 400
    lineHeight: 1.60
    letterSpacing: "-0.165px"
  
  # 辅助文本
  caption:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.50
    letterSpacing: "-0.13px"
  label:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "12px"
    fontWeight: 510
    lineHeight: 1.40
  
  # 代码/数据
  mono-md:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.50
  mono-sm:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "12px"
    fontWeight: 400
    lineHeight: 1.40
  
  # 数据密集
  data-cell:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.50
  data-header:
    fontFamily: "Inter, system-ui, -apple-system, sans-serif"
    fontSize: "13px"
    fontWeight: 510
    lineHeight: 1.50

rounded:
  micro: "2px"
  sm: "4px"
  md: "6px"
  lg: "8px"
  xl: "12px"
  pill: "9999px"
  circle: "50%"

spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "32px"
  xxl: "48px"
  
  # 8px网格系统
  grid-1: "8px"
  grid-2: "16px"
  grid-3: "24px"
  grid-4: "32px"
  grid-6: "48px"

shadows:
  # 深色主题阴影（使用亮度层级而非传统阴影）
  surface: "rgba(0,0,0,0.2) 0px 0px 0px 1px"
  elevated: "rgba(0,0,0,0.4) 0px 2px 4px"
  dialog: "rgba(0,0,0,0) 0px 8px 2px, rgba(0,0,0,0.01) 0px 5px 2px, rgba(0,0,0,0.04) 0px 3px 2px, rgba(0,0,0,0.07) 0px 1px 1px, rgba(0,0,0,0.08) 0px 0px 1px"
  focus: "rgba(0,0,0,0.1) 0px 4px 12px"
  
  # AI 发光效果
  ai-glow: "0px 0px 20px rgba(59,130,246,0.3), 0px 0px 40px rgba(139,92,246,0.15)"

components:
  # 按钮
  button-primary:
    backgroundColor: "{colors.brand-primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    padding: "8px 16px"
    typography: "{typography.label}"
  button-primary-hover:
    backgroundColor: "{colors.brand-hover}"
  button-primary-active:
    backgroundColor: "{colors.brand-active}"
  
  button-secondary:
    backgroundColor: "{colors.bg-input}"
    textColor: "{colors.text-secondary}"
    rounded: "{rounded.md}"
    padding: "8px 16px"
    border: "1px solid {colors.border-standard}"
  button-secondary-hover:
    backgroundColor: "rgba(255,255,255,0.04)"
    textColor: "{colors.text-primary}"
  
  button-ghost:
    backgroundColor: "transparent"
    textColor: "{colors.text-tertiary}"
    rounded: "{rounded.md}"
    padding: "6px 12px"
  button-ghost-hover:
    backgroundColor: "{colors.bg-input}"
    textColor: "{colors.text-secondary}"
  
  # 输入框
  input-default:
    backgroundColor: "{colors.bg-input}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "12px 14px"
    border: "1px solid {colors.border-standard}"
    typography: "{typography.body-md}"
  input-focus:
    border: "1px solid {colors.border-brand}"
    backgroundColor: "rgba(255,255,255,0.04)"
  
  # 卡片
  card-surface:
    backgroundColor: "{colors.bg-input}"
    border: "1px solid {colors.border-standard}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  card-elevated:
    backgroundColor: "rgba(255,255,255,0.05)"
    border: "1px solid {colors.border-strong}"
    rounded: "{rounded.xl}"
    padding: "{spacing.lg}"
  
  # 标签/徽章
  badge-success:
    backgroundColor: "{colors.success}"
    textColor: "#FFFFFF"
    rounded: "{rounded.pill}"
    padding: "2px 8px"
    typography: "{typography.label}"
  badge-warning:
    backgroundColor: "{colors.warning}"
    textColor: "#FFFFFF"
  badge-error:
    backgroundColor: "{colors.error}"
    textColor: "#FFFFFF"
  badge-neutral:
    backgroundColor: "{colors.bg-elevated}"
    textColor: "{colors.text-secondary}"
    border: "1px solid {colors.border-standard}"
  
  # 导航项
  nav-item:
    textColor: "{colors.text-tertiary}"
    padding: "8px 12px"
    rounded: "{rounded.md}"
    typography: "{typography.caption}"
  nav-item-active:
    textColor: "{colors.brand-primary}"
    backgroundColor: "{colors.brand-subtle}"
  nav-item-hover:
    textColor: "{colors.text-secondary}"
    backgroundColor: "{colors.bg-input}"
  
  # 表格单元格
  table-header:
    backgroundColor: "rgba(255,255,255,0.03)"
    textColor: "{colors.text-tertiary}"
    typography: "{typography.data-header}"
    padding: "12px 16px"
    border: "1px solid {colors.border-subtle}"
  table-cell:
    textColor: "{colors.text-secondary}"
    typography: "{typography.data-cell}"
    padding: "12px 16px"
    border: "1px solid {colors.border-subtle}"
  table-cell-highlight:
    backgroundColor: "rgba(255,255,255,0.02)"
    textColor: "{colors.text-primary}"
---

## Overview

HITAnalysis 医疗数据 AI 智能分析平台的设计系统，遵循以下设计理念：

- **专业严谨**：医疗数据容不得马虎，界面需传达精确、可信的专业感
- **科技智能**：AI 智能分析能力通过视觉语言表达，但不喧宾夺主
- **数据密集**：BI 平台需要高效呈现大量数据，信息层级清晰
- **深色优先**：数据密集界面更适合深色主题，降低视觉疲劳

基于 Linear 的极简深色设计系统，适配医疗行业特点：
- 品牌色从紫色调整为**蓝色系**（医疗行业标准）
- 增加数据可视化组件规范
- 增强语义色系统（成功/警告/错误）

## Colors

### 背景系统（亮度层级）

深色主题通过背景亮度层级而非传统阴影来表达深度：

- **Marketing Black (#08090a)**：最深层背景，用于页面底色
- **Panel Dark (#0f1011)**：侧边栏、面板背景
- **Surface (#191a1b)**：卡片、下拉菜单等浮动元素
- **Elevated (#28282c)**：悬停态、高亮元素

### 品牌色（医疗蓝色）

- **Primary (#3B82F6)**：医疗行业信任蓝，用于主按钮、链接
- **Hover (#2563EB)**：交互悬停态
- **Light (#60A5FA)**：点缀、图标
- **Subtle (rgba 0.15)**：背景点缀、选中态

### 语义色（医疗标准）

- **Success (#10B981)**：达标、正常状态（绿色）
- **Warning (#F59E0B)**：预警、接近上限（橙色）
- **Error (#EF4444)**：超标、异常（红色）

### AI 特色

- **Gradient (3B82F6 → 8B5CF6)**：AI 功能区域渐变蓝紫
- **Glow (rgba 0.4)**：AI 输入框发光效果

### 图表色板

8色数据可视化色板，用于图表系列：
1. Blue (#3B82F6) — 主系列
2. Emerald (#10B981) — 对比系列
3. Amber (#F59E0B) — 预警系列
4. Red (#EF4444) — 异常系列
5. Violet (#8B5CF6) — AI 系列
6. Cyan (#06B6D4) — 辅助系列
7. Orange (#F97316) — 辅助系列
8. Slate (#64748B) — 中性系列

## Typography

### 字体选择

- **主字体：Inter** — 清晰、几何感、现代，适合数据密集界面
- **代码字体：JetBrains Mono** — 数据标识、代码片段

### 层级系统

| 层级 | 用途 | 特点 |
|------|------|------|
| H1 (32px/590) | 页面标题 | 压缩字间距，权威感 |
| H2 (24px/510) | 区块标题 | 功能分区 |
| H3 (20px/590) | 卡片标题 | 数据模块 |
| Body (16px/400) | 正文内容 | 阅读舒适 |
| Caption (13px) | 元数据 | 时间、来源 |
| Label (12px/510) | 标签 | 导航、按钮 |

### 数据密集排版

- **表格单元 (14px/400)**：数据密集场景的标准字号
- **表头 (13px/510)**：紧凑、强调层级
- **数据标识 (mono)**：使用等宽字体确保数字对齐

## Layout

### 8px 网格系统

所有间距、字号、尺寸遵循 8px 基准：
- 4px：微间距（图标与文字）
- 8px：基础间距
- 16px：组件内间距
- 24px：区块间距
- 32px：模块间距
- 48px：章节间距

### 响应式断点

| 设备 | 断点 | 布局 |
|------|------|------|
| 桌面 | ≥1280px | 全功能，侧边栏+主内容 |
| 小桌面 | 1024-1280px | 紧凑侧边栏 |
| 平板 | 768-1024px | 隐藏侧边栏，全宽主内容 |
| 移动 | <768px | 单列堆叠 |

### 导航布局

- **左侧边栏 (240px)**：目录导航、AI 报表列表
- **顶部栏 (56px)**：全局搜索、用户信息、机构切换
- **主内容区 (剩余宽度)**：报表展示、AI 对话

## Components

### 按钮

- **Primary**：品牌蓝，主操作（确认生成、保存）
- **Secondary**：次操作（导出、筛选）
- **Ghost**：轻操作（追问调优、切换图表类型）

### 数据卡片

- **Surface Card**：数据展示区，bg-input + border-standard
- **Elevated Card**：AI 解析确认框，bg-elevated + border-strong

### 输入框

- **AI 输入框**：带发光效果，渐变边框
- **筛选输入**：标准深色输入

### 表格

- **紧凑型**：数据密集场景，字号 14px，行高 48px
- **斑马纹**：交替背景色增强可读性

## Do's and Don'ts

### ✅ Do

- 使用 Inter 字体系统，确保数据密集场景的可读性
- 品牌蓝色仅用于交互元素，不装饰性使用
- 图表使用色板规范，确保数据可视化一致性
- 深色背景层级表达深度，避免传统阴影
- 语义色严格按医疗标准使用（绿色=正常，橙色=预警，红色=异常）

### ❌ Don't

- 不在深色背景上使用纯白 (#FFFFFF) 文字，使用 #f7f8f8
- 不使用超过 590 的字重，避免过于粗重
- 不在数据密集区域使用过大字号（>16px）
- 不使用彩色装饰元素，保持专业严谨
- 不使用传统阴影在深色主题表达深度