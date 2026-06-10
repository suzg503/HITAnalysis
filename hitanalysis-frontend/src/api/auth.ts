import { post, get } from './index'
import type { LoginDTO, LoginVO, UserInfoVO } from '@/types/user'

export const authApi = {
  login: (data: LoginDTO): Promise<LoginVO> =>
    post('/v1/auth/login', data),

  logout: (userId: number): Promise<void> =>
    post('/v1/auth/logout', null, { params: { userId } }),

  refreshToken: (refreshToken: string): Promise<LoginVO> =>
    post('/v1/auth/refresh', null, { params: { refreshToken } }),

  getUserInfo: (userId: number): Promise<UserInfoVO> =>
    get('/v1/auth/user-info', { userId }),
}