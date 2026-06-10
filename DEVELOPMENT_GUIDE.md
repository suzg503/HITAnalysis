# HITAnalysis - 开发规范与最佳实践

## 📋 文档概述

本文档定义了HITAnalysis项目的开发规范、代码风格和最佳实践，确保代码质量和团队协作效率。

## 🎯 开发原则

### 核心价值观
1. **数据安全至上** - 医疗数据隐私保护是首要考虑
2. **代码质量优先** - 可读性、可维护性、可测试性
3. **性能驱动** - 优化高频查询和关键业务流程
4. **协作友好** - 清晰的文档、规范的命名、合理的结构

### 开发理念
- **简单优于复杂** - 遵循KISS原则
- **清晰优于聪明** - 代码可读性比炫技更重要
- **约定优于配置** - 统一的规范减少沟通成本
- **测试优于调试** - 自动化测试确保质量

## 📁 项目结构规范

### 后端项目结构
```
hitanalysis-backend/
├── hitanalysis-common/           # 公共模块
│   ├── config/                   # 配置类
│   ├── constant/                 # 常量定义
│   ├── exception/                # 异常定义
│   ├── result/                   # 返回结果封装
│   └ utils/                      # 工具类
│
├── hitanalysis-system/           # 系统管理模块
│   ├── controller/               # 控制器
│   ├── service/                  # 服务接口
│   │   └ impl/                   # 服务实现
│   ├── mapper/                   # 数据访问接口
│   ├── entity/                   # 实体类
│   ├── dto/                      # 数据传输对象
│   └ vo/                         # 视图对象
│
├── [其他模块类似结构]
│
└── hitanalysis-app/              # 应用启动模块
    ├── config/                   # 应用配置
    ├── resources/
    │   ├── mapper/               # Mapper XML文件
    │   │   ├── system/
    │   │   ├── metadata/
    │   │   └ report/
    │   │   └ ai/
    │   ├── application.yml       # 主配置文件
    │   └ application-dev.yml     # 开发环境配置
    │   └ prompts/                # AI提示词模板
    │   └ db/                     # 数据库脚本
    └
```

### 前端项目结构
```
hitanalysis-frontend/
├── src/
│   ├── api/                      # API接口封装
│   │   ├── auth.ts               # 认证接口
│   │   ├── user.ts               # 用户接口
│   │    index.ts                 # Axios配置
│   │
│   ├── views/                    # 页面组件
│   │   ├── dashboard/            # 仪表板页面
│   │   ├── system/               # 系统管理页面
│   │   ├── metadata/             # 元数据管理页面
│   │   ├── report/               # 报表页面
│   │   │   ├── standard/         # 标准报表
│   │   │   │   ai/               # AI报表
│   │   │   ├── layout/           # 布局组件
│   │   │   ├── login/            # 登录页面
│   │   │   │   error/            # 错误页面
│   │   │   │
│   ├── components/               # 可复用组件
│   │   ├── common/               # 通用组件
│   │   ├── charts/               # 图表组件
│   │   ├── forms/                # 表单组件
│   │   │
│   ├── stores/                   # Pinia状态管理
│   │   ├── user.ts               # 用户状态
│   │   ├── app.ts                # 应用状态
│   │   │
│   ├── router/                   # 路由配置
│   │   ├── index.ts              # 路由定义
│   │   ├── guards/               # 路由守卫
│   │   │
│   ├── types/                    # TypeScript类型定义
│   │   ├── user.ts               # 用户类型
│   │   ├── indicator.ts          # 指标类型
│   │   │
│   ├── constants/                # 常量定义
│   ├── utils/                    # 工具函数
│   ├── hooks/                    # Vue组合式函数
│   │
│   ├── App.vue                   # 根组件
│   ├── main.ts                   # 应用入口
│   │
├── public/                       # 静态资源
├── tests/                        # 测试文件
└── .env.development              # 开发环境变量
```

## 🏗️ 后端开发规范

### 1. Controller层规范

#### 基本要求
- 使用`@RestController`和REST风格URL
- 所有接口必须有Swagger注解
- 使用统一的Result封装返回结果
- 参数验证使用Jakarta Validation注解

#### 代码示例
```java
@Tag(name = "用户管理", description = "用户增删改查等管理接口")
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表", description = "分页查询用户列表")
    @GetMapping
    public Result<PageResult<UserInfoVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {

        if (pageSize > SystemConstants.PAGE_SIZE_MAX) {
            pageSize = SystemConstants.PAGE_SIZE_MAX;
        }

        PageResult<UserInfoVO> result = userService.listUsers(pageNum, pageSize, username, status);
        return Result.success(result);
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserDTO dto) {
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }
}
```

#### URL命名规范
- 使用REST风格：`/v1/{resource}`
- 查询列表：`GET /v1/users`
- 查询详情：`GET /v1/users/{id}`
- 创建资源：`POST /v1/users`
- 更新资源：`PUT /v1/users/{id}`
- 删除资源：`DELETE /v1/users/{id}`
- 特殊操作：`PUT /v1/users/{id}/status`

### 2. Service层规范

#### 基本要求
- Service接口定义业务逻辑
- Impl类实现具体业务
- 业务异常使用BusinessException
- 关键操作添加事务注解

#### 代码示例
```java
public interface UserService {
    Long createUser(UserDTO dto);
    void updateUser(UserDTO dto);
    void deleteUser(Long userId);
    PageResult<UserInfoVO> listUsers(int pageNum, int pageSize, String username, Integer status);
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final PasswordUtils passwordUtils;

    @Override
    @Transactional
    public Long createUser(UserDTO dto) {
        // 检查用户名是否存在
        SysUser existingUser = userMapper.selectByUsernameWithRole(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_EXISTS, "用户名已存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordUtils.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRoleId(dto.getRoleId());

        userMapper.insert(user);
        return user.getUserId();
    }
}
```

### 3. Mapper层规范

#### 基本要求
- 继承MyBatis Plus的BaseMapper
- 复杂查询使用XML文件
- 简单查询使用注解
- 方法命名清晰表达意图

#### 代码示例
```java
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // 简单查询使用注解
    @Select("SELECT u.*, r.role_name FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.role_id " +
            "WHERE u.username = #{username} AND u.is_deleted = 0")
    SysUser selectByUsernameWithRole(@Param("username") String username);

    // 复杂查询使用XML文件
    List<SysUser> selectUserList(@Param("username") String username,
                                  @Param("status") Integer status);
}
```

#### XML文件规范
```xml
<mapper namespace="com.hitanalysis.system.mapper.SysUserMapper">

    <select id="selectUserList" resultMap="UserWithRoleResultMap">
        SELECT u.*, r.role_name, r.role_code
        FROM sys_user u
        LEFT JOIN sys_role r ON u.role_id = r.role_id
        WHERE u.is_deleted = 0
        <if test="username != null and username != ''">
            AND u.username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="status != null">
            AND u.status = #{status}
        </if>
        ORDER BY u.create_time DESC
    </select>
</mapper>
```

### 4. Entity实体规范

#### 基本要求
- 使用Lombok简化代码
- 添加字段注释
- 继承基础实体类（如有）
- 合理使用数据类型

#### 代码示例
```java
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    private String realName;

    private Long roleId;

    private Long hospitalId;

    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 5. DTO和VO规范

#### DTO（数据传输对象）
- 用于接收前端参数
- 添加验证注解
- 使用清晰的字段命名

```java
@Data
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
```

#### VO（视图对象）
- 用于返回前端数据
- 只包含必要字段
- 可以组合多个实体字段

```java
@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private String realName;

    private Long roleId;

    private String roleName;     // 来自Role表

    private Integer status;

    private LocalDateTime createTime;
}
```

### 6. 异常处理规范

#### 异常定义
```java
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

#### 全局异常处理
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.fail(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return Result.fail(ErrorCode.VALIDATION_ERROR, message);
    }
}
```

### 7. 工具类规范

#### 工具类命名
- 以`Utils`结尾：`PasswordUtils`, `JwtUtils`
- 静态方法，无状态
- 方法命名清晰

#### 代码示例
```java
@Component
public class PasswordUtils {

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
```

## 🎨 前端开发规范

### 1. 组件规范

#### Vue组件结构
```vue
<script setup lang="ts">
// Imports
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'

// Reactive state
const loading = ref(false)
const users = ref<User[]>([])

// Computed properties
const totalUsers = computed(() => users.value.length)

// Methods
const fetchUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.listUsers()
    users.value = response.data
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

// Lifecycle hooks
onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <!-- HTML -->
</template>

<style scoped lang="scss">
/* CSS */
</style>
```

#### 命名规范
- 组件文件：PascalCase（`UserList.vue`）
- 组件名称：PascalCase
- props：camelCase
- emits：kebab-case

### 2. API调用规范

#### API封装
```typescript
// src/api/user.ts
import { get, post, put, del } from './index'
import type { UserDTO, UserInfoVO } from '@/types/user'

export const userApi = {
  listUsers: (pageNum: number, pageSize: number) =>
    get<UserInfoVO[]>('/v1/users', { pageNum, pageSize }),

  getUserById: (userId: number) =>
    get<UserInfoVO>(`/v1/users/${userId}`),

  createUser: (data: UserDTO) =>
    post<number>('/v1/users', data),

  updateUser: (userId: number, data: UserDTO) =>
    put<void>(`/v1/users/${userId}`, data),

  deleteUser: (userId: number) =>
    del<void>(`/v1/users/${userId}`),
}
```

#### Axios配置
```typescript
// src/api/index.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

// Request interceptor
instance.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

// Response interceptor
instance.interceptors.response.use(
  response => {
    const { code, data, message } = response.data
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  error => {
    if (error.response?.status === 401) {
      // Redirect to login
      router.push('/login')
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)
```

### 3. TypeScript规范

#### 类型定义
```typescript
// src/types/user.ts
export interface User {
  userId: number
  username: string
  realName: string
  roleId: number
  status: number
}

export interface UserDTO {
  username: string
  password: string
  realName: string
  roleId: number
}

export interface UserInfoVO extends User {
  roleName: string
  createTime: string
}
```

#### 类型使用
```typescript
// 明确指定类型
const users = ref<User[]>([])

// 使用泛型
export const userApi = {
  listUsers: (): Promise<User[]> =>
    get<User[]>('/v1/users'),
}
```

### 4. 状态管理规范

#### Pinia Store
```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { userApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: null as UserInfoVO | null,
    menus: [] as Menu[],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    realName: (state) => state.userInfo?.realName || '',
  },

  actions: {
    async login(username: string, password: string) {
      const response = await authApi.login({ username, password })
      this.token = response.token
      this.userInfo = response.userInfo
      localStorage.setItem('token', response.token)
    },

    logout() {
      this.token = ''
      this.userInfo = null
      this.menus = []
      localStorage.removeItem('token')
    },
  },
})
```

### 5. 样式规范

#### SCSS规范
```scss
// 使用scoped避免样式冲突
<style scoped lang="scss">
.user-list {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
  }

  .table {
    width: 100%;

    .el-table {
      font-size: 14px;
    }
  }
}
</style>
```

#### CSS命名
- 类名：kebab-case（`.user-list`）
- BEM命名：`.block__element--modifier`

## 🧪 测试规范

### 后端测试

#### 单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Create user successfully")
    void testCreateUser_Success() {
        // Given
        when(userMapper.selectByUsername(anyString())).thenReturn(null);
        when(userMapper.insert(any(SysUser.class))).thenReturn(1);

        // When
        Long userId = userService.createUser(new UserDTO());

        // Then
        assertNotNull(userId);
        verify(userMapper, times(1)).insert(any(SysUser.class));
    }
}
```

#### 集成测试
```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

### 前端测试

#### Vitest测试
```typescript
import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UserList from '@/views/system/UserList.vue'

describe('UserList', () => {
  it('renders user list', () => {
    const wrapper = mount(UserList)
    expect(wrapper.find('.user-list').exists()).toBe(true)
  })

  it('calls API on mount', async () => {
    const mockApi = vi.spyOn(userApi, 'listUsers')
    mount(UserList)
    expect(mockApi).toHaveBeenCalled()
  })
})
```

## 📝 代码审查清单

### 提交前检查
- ✅ 代码符合命名规范
- ✅ 有必要的注释和文档
- ✅ 单元测试覆盖核心逻辑
- ✅ 无安全漏洞（SQL注入、XSS等）
- ✅ 无性能问题（N+1查询、大对象等）
- ✅ 错误处理完善
- ✅ 日志输出合理

### Pull Request规范
1. **标题**: 清晰描述本次改动（如："feat: Add user management module"）
2. **描述**: 详细说明改动原因、实现方案、测试结果
3. **关联Issue**: 链接相关Issue编号
4. **截图**: 如有UI改动，提供对比截图
5. **测试**: 说明测试方法和结果

## 🔐 安全规范

### 数据安全
- 所有数据库查询必须包含`is_deleted = 0`过滤
- 患者数据必须脱敏处理
- SQL参数必须使用预编译
- 密码必须BCrypt加密存储

### API安全
- 所有API必须有认证授权
- 敏感操作记录审计日志
- 输入参数必须验证
- 输出数据必须过滤敏感字段

### 前端安全
- Token存储使用localStorage（或cookie）
- 路由守卫检查权限
- XSS防护（Vue自动处理）
- CSRF防护（使用Token）

## 📚 文档规范

### 代码注释
- 类注释：说明类的用途和主要功能
- 方法注释：说明参数、返回值、异常
- 关键逻辑注释：解释复杂算法或业务规则

### API文档
- 使用Swagger注解完整描述
- 提供请求和响应示例
- 说明错误码含义

### 项目文档
- README.md：项目简介和快速开始
- STARTUP_GUIDE.md：详细启动步骤
- DEPLOYMENT_GUIDE.md：部署和运维指南
- DEVELOPMENT_GUIDE.md：开发规范（本文档）

## 🚀 性能优化规范

### 数据库优化
- 使用索引覆盖高频查询
- 避免N+1查询
- 使用分页限制结果集
- 合理使用连接池

### 缓存优化
- Redis缓存热点数据
- 合理设置过期时间
- 缓存键命名规范

### 前端优化
- 组件懒加载
- 图片压缩和懒加载
- 路由懒加载
- 合理使用keep-alive

---

**版本**: v1.0.0
**更新日期**: 2026-05-11
**维护团队**: HITAnalysis开发团队