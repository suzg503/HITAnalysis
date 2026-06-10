export const TOKEN_KEY = 'hitanalysis_token'
export const USER_INFO_KEY = 'hitanalysis_user_info'
export const LANGUAGE_KEY = 'hitanalysis_language'

export const PAGE_SIZE_DEFAULT = 10
export const PAGE_SIZE_MAX = 100

export const STATUS_ENABLE = 1
export const STATUS_DISABLE = 0

export const CHART_TYPES = {
  BAR: 'bar',
  LINE: 'line',
  PIE: 'pie',
  TABLE: 'table',
  GAUGE: 'gauge',
}

export const TIME_RANGE_OPTIONS = [
  { label: '今日', value: 'today' },
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本季度', value: 'quarter' },
  { label: '本年', value: 'year' },
  { label: '自定义', value: 'custom' },
]

export const REPORT_VISIBILITY = {
  PRIVATE: 'private',
  DEPT: 'dept',
  HOSPITAL: 'hospital',
  ALL: 'all',
}

export const AI_INTENT_TYPES = {
  TREND: 'trend_analysis',
  COMPARISON: 'comparison_analysis',
  RANKING: 'ranking_analysis',
  DISTRIBUTION: 'distribution_analysis',
  DETAIL: 'detail_query',
}