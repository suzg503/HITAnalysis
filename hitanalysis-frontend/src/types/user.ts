export interface UserInfoVO {
  userId: number
  username: string
  realName: string
  roleId: number
  roleName: string
  roleCode: string
  hospitalId?: number
  deptCodes?: string[]
  hospitalIds?: number[]
  menus: MenuTreeVO[]
}

export interface MenuTreeVO {
  menuId: number
  parentId: number
  menuName: string
  menuCode: string
  menuLevel: number
  linkUrl: string
  sortNum: number
  children?: MenuTreeVO[]
}

export interface LoginDTO {
  username: string
  password: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: UserInfoVO
}

export interface UserDTO {
  userId?: number
  username: string
  password?: string
  realName: string
  roleId: number
  hospitalId?: number
  status?: number
}

export interface RoleVO {
  roleId: number
  roleName: string
  roleCode: string
  systemName: string
  remark?: string
  status: number
}