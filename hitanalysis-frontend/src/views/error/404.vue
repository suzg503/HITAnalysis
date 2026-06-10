<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const handleGoBack = () => {
  router.push('/dashboard/today')
}
</script>

<template>
  <div class="error-container">
    <div class="error-content">
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="handleGoBack">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.error-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.error-content {
  text-align: center;

  h1 {
    font-size: 120px;
    color: #409eff;
    margin-bottom: 20px;
  }

  p {
    font-size: 24px;
    color: #666;
    margin-bottom: 30px;
  }
}
</style>