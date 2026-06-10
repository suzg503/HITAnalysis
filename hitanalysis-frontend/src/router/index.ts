import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { authGuard, permissionGuard } from './guards'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/dashboard/today',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard/today',
        name: 'TodayDynamic',
        component: () => import('@/views/dashboard/TodayDynamic.vue'),
        meta: { title: '今日动态', icon: 'Calendar' },
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/users',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'users',
            name: 'UserList',
            component: () => import('@/views/system/user/UserList.vue'),
            meta: { title: '用户管理', icon: 'User' },
          },
          {
            path: 'roles',
            name: 'RoleList',
            component: () => import('@/views/system/role/RoleList.vue'),
            meta: { title: '角色管理', icon: 'Stamp' },
          },
          {
            path: 'menus',
            name: 'MenuTree',
            component: () => import('@/views/system/menu/MenuTree.vue'),
            meta: { title: '菜单管理', icon: 'Menu' },
          },
        ],
      },
      {
        path: 'report',
        name: 'Report',
        redirect: '/report/standard',
        meta: { title: '报表中心', icon: 'DataAnalysis' },
        children: [
          {
            path: 'standard',
            name: 'StandardReportList',
            component: () => import('@/views/report/standard/ReportList.vue'),
            meta: { title: '标准报表', icon: 'Document' },
          },
          {
            path: 'standard/:id',
            name: 'ReportView',
            component: () => import('@/views/report/standard/ReportView.vue'),
            meta: { title: '报表详情', hidden: true },
          },
          {
            path: 'ai',
            name: 'AiCenter',
            component: () => import('@/views/report/ai/AiCenter.vue'),
            meta: { title: 'AI洞察中心', icon: 'ChatDotSquare' },
          },
        ],
      },
      {
        path: 'ai',
        name: 'AiAssistant',
        redirect: '/ai/assistant',
        meta: { title: 'AI助手', icon: 'MagicStick' },
        children: [
          {
            path: 'assistant',
            name: 'AiAssistantMain',
            component: () => import('@/views/ai/AiAssistant.vue'),
            meta: { title: 'AI助手', icon: 'MagicStick' },
          },
          {
            path: 'history',
            name: 'AiHistory',
            component: () => import('@/views/ai/AiHistory.vue'),
            meta: { title: '查询历史', icon: 'Clock' },
          },
        ],
      },
      {
        path: 'metadata',
        name: 'Metadata',
        redirect: '/metadata/indicators',
        meta: { title: '元数据管理', icon: 'Files' },
        children: [
          {
            path: 'indicators',
            name: 'IndicatorList',
            component: () => import('@/views/metadata/indicator/IndicatorList.vue'),
            meta: { title: '指标管理', icon: 'TrendCharts' },
          },
          {
            path: 'dimensions',
            name: 'DimensionList',
            component: () => import('@/views/metadata/dimension/DimensionList.vue'),
            meta: { title: '维度管理', icon: 'Grid' },
          },
        ],
      },
    ],
  },
  {
    path: '/error/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false },
  },
  {
    path: '/error/permission',
    name: 'PermissionDenied',
    component: () => import('@/views/error/PermissionDenied.vue'),
    meta: { title: '权限不足', requiresAuth: false },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/error/404',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// Global guards
router.beforeEach(authGuard)
router.beforeEach(permissionGuard)

export default router