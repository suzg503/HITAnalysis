import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { TOKEN_KEY } from '@/constants'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

/**
 * Auth guard - check if user is authenticated
 */
export async function authGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  NProgress.start()

  const token = localStorage.getItem(TOKEN_KEY)
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.name === 'Login' && token) {
    next({ name: 'TodayDynamic' })
    return
  }

  next()
}

/**
 * Permission guard - check if user has access to the route
 */
export async function permissionGuard(
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && userStore.userInfo) {
    // Check menu permission (D3)
    const menuCodes = userStore.menuCodes
    const routeCode = to.meta.menuCode as string

    if (routeCode && menuCodes.length > 0 && !menuCodes.includes(routeCode)) {
      next({ name: 'PermissionDenied' })
      return
    }
  }

  next()
}

/**
 * After each navigation - close progress bar
 */
router.afterEach(() => {
  NProgress.done()
})