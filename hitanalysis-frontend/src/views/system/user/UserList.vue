<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, del } from '@/api/index'
import type { UserInfoVO, UserDTO } from '@/types/user'

const loading = ref(false)
const tableData = ref<UserInfoVO[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  username: '',
  status: undefined as number | undefined,
})

const editDialogVisible = ref(false)
const editForm = reactive<UserDTO>({
  userId: undefined,
  username: '',
  password: '',
  realName: '',
  roleId: undefined,
  hospitalId: undefined,
  status: 1,
})

const isEdit = ref(false)

// Role list
const roleOptions = ref([
  { roleId: 1, roleName: '系统管理员', roleCode: 'admin' },
  { roleId: 2, roleName: '医院院长', roleCode: 'dean' },
  { roleId: 3, roleName: '科室主任', roleCode: 'dept_director' },
  { roleId: 4, roleName: '数据分析师', roleCode: 'analyst' },
  { roleId: 5, roleName: '普通用户', roleCode: 'user' },
])

const fetchUsers = async () => {
  loading.value = true
  try {
    const result = await get('/v1/users', {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: searchForm.username || undefined,
      status: searchForm.status,
    })
    tableData.value = result.list
    total.value = result.total
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchUsers()
}

const handleAdd = () => {
  isEdit.value = false
  editForm.userId = undefined
  editForm.username = ''
  editForm.password = ''
  editForm.realName = ''
  editForm.roleId = undefined
  editForm.hospitalId = undefined
  editForm.status = 1
  editDialogVisible.value = true
}

const handleEdit = (row: UserInfoVO) => {
  isEdit.value = true
  editForm.userId = row.userId
  editForm.username = row.username
  editForm.password = ''
  editForm.realName = row.realName
  editForm.roleId = row.roleId
  editForm.hospitalId = row.hospitalId
  editForm.status = row.status ?? 1
  editDialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await put(`/v1/users/${editForm.userId}`, editForm)
      ElMessage.success('更新成功')
    } else {
      await post('/v1/users', editForm)
      ElMessage.success('创建成功')
    }
    editDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (row: UserInfoVO) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await del(`/v1/users/${row.userId}`)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch {
    // User cancelled
  }
}

const handleStatusChange = async (row: UserInfoVO) => {
  try {
    await put(`/v1/users/${row.userId}/status`, null, {
      params: { status: row.status === 1 ? 0 : 1 },
    })
    ElMessage.success('状态更新成功')
    fetchUsers()
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchUsers()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="user-list">
    <div class="card">
      <div class="card-header">
        <span>用户管理</span>
      </div>

      <!-- Search Form -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleAdd">新增</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="roleName" label="角色" width="120" />
        <el-table-column prop="hospitalId" label="医院ID" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
        class="pagination"
      />
    </div>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500">
      <el-form :model="editForm" label-width="80">
        <el-form-item label="用户名" required>
          <el-input v-model="editForm.username" />
        </el-form-item>
        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="editForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="editForm.roleId">
            <el-option
              v-for="role in roleOptions"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医院ID">
          <el-input v-model="editForm.hospitalId" type="number" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.user-list {
  .card {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
  }

  .card-header {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 20px;
  }

  .search-form {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>