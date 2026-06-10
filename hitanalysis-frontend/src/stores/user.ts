import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfoVO, LoginVO } from '@/types/user'
import { authApi } from '@/api/auth'
import { TOKEN_KEY, USER_INFO_KEY } from '@/constants'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<UserInfoVO | null>(
    JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null')
  )

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const userId = computed(() => userInfo.value?.userId)
  const username = computed(() => userInfo.value?.username)
  const realName = computed(() => userInfo.value?.realName)
  const roleName = computed(() => userInfo.value?.roleName)
  const roleCode = computed(() => userInfo.value?.roleCode)
  const menus = computed(() => userInfo.value?.menus || [])
  const menuCodes = computed(() =>
    menus.value.flatMap(m => [m.menuCode || '', ...(m.children?.map(c => c.menuCode || '') || [])])
  )

  async function login(username: string, password: string): Promise<LoginVO> {
    const result = await authApi.login({ username, password })

    token.value = result.accessToken
    userInfo.value = result.userInfo

    localStorage.setItem(TOKEN_KEY, result.accessToken)
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(result.userInfo))

    return result
  }

  async function logout(): Promise<void> {
    if (userId.value) {
      await authApi.logout(userId.value)
    }

    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_INFO_KEY)
  }

  async function getUserInfo(): Promise<UserInfoVO> {
    if (!userId.value) throw new Error('User not logged in')

    const result = await authApi.getUserInfo(userId.value)
    userInfo.value = result
    localStorage.setItem(USER_INFO_KEY, JSON.stringify(result))

    return result
  }

  async function refreshToken(): Promise<LoginVO> {
    const oldToken = token.value
    if (!oldToken) throw new Error('No token to refresh')

    const result = await authApi.refreshToken(oldToken)
    token.value = result.accessToken
    localStorage.setItem(TOKEN_KEY, result.accessToken)

    return result
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userId,
    username,
    realName,
    roleName,
    roleCode,
    menus,
    menuCodes,
    login,
    logout,
    getUserInfo,
    refreshToken,
  }
})