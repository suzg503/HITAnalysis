<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)

const menus = computed(() => userStore.menus)

const handleToggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // User cancelled
  }
}
</script>

<template>
  <div class="main-layout">
    <!-- Sidebar -->
    <div class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <span v-if="!isCollapsed" class="logo">HITAnalysis</span>
        <span v-else class="logo-mini">HA</span>
      </div>

      <el-menu
        :default-active="$route.path"
        :collapse="isCollapsed"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-for="menu in menus" :key="menu.menuId">
          <!-- Menu with children -->
          <el-sub-menu v-if="menu.children?.length" :index="menu.linkUrl">
            <template #title>
              <el-icon><component :is="menu.menuCode" /></el-icon>
              <span>{{ menu.menuName }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.menuId"
              :index="child.linkUrl"
            >
              <el-icon><component :is="child.menuCode" /></el-icon>
              <span>{{ child.menuName }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- Menu without children -->
          <el-menu-item v-else :index="menu.linkUrl">
            <el-icon><component :is="menu.menuCode" /></el-icon>
            <span>{{ menu.menuName }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <!-- Main content area -->
    <div class="main-content">
      <!-- Header -->
      <div class="header">
        <div class="header-left">
          <el-button
            type="text"
            :icon="isCollapsed ? 'Expand' : 'Fold'"
            @click="handleToggleSidebar"
          />
        </div>

        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" icon="User" />
              <span class="user-name">{{ userStore.realName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>个人设置</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- Content area -->
      <div class="content-area">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.main-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 220px;
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;

  .logo-mini {
    font-size: 14px;
  }
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 50px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;

  .user-name {
    margin-left: 10px;
    font-size: 14px;
  }
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>