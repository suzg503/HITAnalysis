<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { post, get } from '@/api/index'

// AI Chat State
const messages = ref<Array<{role: string; content: string}>>([])
const userInput = ref('')
const loading = ref(false)
const sessionId = ref<number | null>(null)

// Parse confirmation state (D1)
const parseResult = ref<any>(null)
const showConfirmDialog = ref(false)

// Preview state
const previewResult = ref<any>(null)
const showPreviewDialog = ref(false)

// Insight state (D2)
const insightResult = ref<any>(null)
const showInsightDialog = ref(false)

// Chart config
const chartType = ref('bar')
const chartData = ref<any>(null)

const userId = 1 // Placeholder for MVP

const createSession = async () => {
  try {
    sessionId.value = await post('/v1/ai/session', null, { params: { userId } })
    messages.value.push({
      role: 'system',
      content: 'AI会话已创建，请输入您的查询需求。',
    })
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

const handleSubmit = async () => {
  if (!userInput.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }

  loading.value = true
  messages.value.push({
    role: 'user',
    content: userInput.value,
  })

  try {
    // Submit query and get parse result (D1)
    const result = await post('/v1/ai/query', {
      queryText: userInput.value,
      sessionId: sessionId.value,
      userId: userId,
    })

    parseResult.value = result
    showConfirmDialog.value = true

    messages.value.push({
      role: 'ai',
      content: `已解析您的查询，识别到意图：${result.intent}，请确认配置是否正确。`,
    })
  } catch (error) {
    messages.value.push({
      role: 'ai',
      content: '解析失败，请检查您的输入。',
    })
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
    userInput.value = ''
  }
}

const handleConfirm = async () => {
  if (!parseResult.value) return

  loading.value = true
  try {
    // Confirm and execute (D1/D3)
    const result = await post('/v1/ai/confirm', null, {
      params: { parseId: parseResult.value.parseId, userId },
    })

    previewResult.value = result
    showConfirmDialog.value = false
    showPreviewDialog.value = true

    // Set chart config
    chartType.value = result.chartType || 'bar'
    chartData.value = result.dataRows

    messages.value.push({
      role: 'ai',
      content: `查询执行完成，共返回 ${result.total} 条数据。请查看预览结果。`,
    })
  } catch (error) {
    ElMessage.error('执行失败')
  } finally {
    loading.value = false
  }
}

const handleGenerateInsight = async () => {
  if (!previewResult.value) return

  loading.value = true
  try {
    // Generate insight (D2)
    const result = await post('/v1/ai/insight', null, {
      params: { previewId: previewResult.value.previewId },
    })

    insightResult.value = result
    showInsightDialog.value = true

    messages.value.push({
      role: 'ai',
      content: `洞察已生成：${result.title}`,
    })
  } catch (error) {
    ElMessage.error('洞察生成失败')
  } finally {
    loading.value = false
  }
}

const handleSaveReport = async () => {
  if (!previewResult.value) return

  try {
    const reportId = await post('/v1/reports/ai/save', {
      reportName: `AI报表-${new Date().toLocaleDateString()}`,
      configJson: previewResult.value.configJson, // D1 - Save config JSON
      visibility: 'private',
      status: 'saved',
    }, { params: { userId } })

    ElMessage.success('报表已保存')
    showPreviewDialog.value = false
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleRefine = () => {
  showPreviewDialog.value = false
  userInput.value = ''
}

onMounted(() => {
  createSession()
})
</script>

<template>
  <div class="ai-center">
    <div class="chat-area">
      <!-- Message List -->
      <div class="message-list">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message"
          :class="msg.role"
        >
          <div class="message-avatar">
            <el-avatar :icon="msg.role === 'user' ? 'User' : 'MagicStick'" />
          </div>
          <div class="message-content">
            {{ msg.content }}
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <el-input
          v-model="userInput"
          placeholder="请输入您的查询需求，如：查看本月门诊人次趋势"
          :disabled="loading"
          @keyup.enter="handleSubmit"
        />
        <el-button type="primary" :loading="loading" @click="handleSubmit">
          发送
        </el-button>
      </div>
    </div>

    <!-- Parse Confirm Dialog (D1) -->
    <el-dialog v-model="showConfirmDialog" title="确认解析结果 (D1)" width="600">
      <div v-if="parseResult">
        <p><strong>原始查询：</strong>{{ parseResult.originalQuery }}</p>
        <p><strong>识别意图：</strong>{{ parseResult.intent }}</p>
        <p><strong>置信度：</strong>{{ parseResult.confidence }}</p>

        <div class="confirm-section">
          <strong>识别的指标：</strong>
          <el-tag v-for="m in parseResult.metrics" :key="m.id">
            {{ m.name }} ({{ m.code }})
          </el-tag>
        </div>

        <div class="confirm-section">
          <strong>识别的维度：</strong>
          <el-tag v-for="d in parseResult.dimensions" :key="d.code" type="success">
            {{ d.name }}
          </el-tag>
        </div>

        <div class="confirm-section">
          <strong>时间范围：</strong>{{ parseResult.timeRange?.displayText }}
        </div>

        <div class="confirm-section">
          <strong>建议图表：</strong>
          <el-select v-model="parseResult.suggestedChartType" style="width: 100px">
            <el-option label="折线图" value="line" />
            <el-option label="柱状图" value="bar" />
            <el-option label="饼图" value="pie" />
            <el-option label="表格" value="table" />
          </el-select>
        </div>

        <p class="d1-note">
          <el-icon><InfoFilled /></el-icon>
          D1决策：AI只生成配置JSON，不直接生成SQL。请确认配置是否正确。
        </p>
      </div>

      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确认执行</el-button>
      </template>
    </el-dialog>

    <!-- Preview Dialog -->
    <el-dialog v-model="showPreviewDialog" title="预览结果" width="800">
      <div v-if="previewResult">
        <div class="preview-chart">
          <!-- Placeholder chart -->
          <div class="chart-placeholder">
            图表类型：{{ chartType }}
            <div class="chart-data">
              <pre>{{ JSON.stringify(chartData?.slice(0, 5), null, 2) }}</pre>
            </div>
          </div>
        </div>

        <div class="preview-info">
          <p>数据总量：{{ previewResult.total }} 条</p>
          <p>查询耗时：{{ previewResult.queryDuration }} ms</p>
        </div>

        <div class="preview-actions">
          <el-button @click="handleGenerateInsight" type="success">
            生成洞察 (D2)
          </el-button>
          <el-button @click="handleSaveReport" type="primary">
            保存报表
          </el-button>
          <el-button @click="handleRefine">继续追问</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- Insight Dialog (D2) -->
    <el-dialog v-model="showInsightDialog" title="AI洞察 (D2)" width="600">
      <div v-if="insightResult">
        <h3>{{ insightResult.title }}</h3>
        <p>{{ insightResult.content }}</p>

        <div class="insight-findings">
          <strong>关键发现：</strong>
          <ul>
            <li v-for="f in insightResult.keyFindings" :key="f">{{ f }}</li>
          </ul>
        </div>

        <div class="insight-recommendations">
          <strong>建议行动：</strong>
          <ul>
            <li v-for="r in insightResult.recommendations" :key="r">{{ r }}</li>
          </ul>
        </div>

        <!-- D2: Data Source and Calculation Logic -->
        <div class="insight-source">
          <strong>数据来源 (D2)：</strong>
          <p>表：{{ insightResult.dataSource?.tableName }}</p>
          <p>来源系统：{{ insightResult.dataSource?.sourceSystem }}</p>
          <p>时间范围：{{ insightResult.dataSource?.timeRange }}</p>
          <p>数据量：{{ insightResult.dataSource?.dataCount }} 条</p>
        </div>

        <div class="insight-logic">
          <strong>计算逻辑 (D2)：</strong>
          <p>指标：{{ insightResult.calculationLogic?.metricName }}</p>
          <p>公式：{{ insightResult.calculationLogic?.formula }}</p>
          <p>聚合方式：{{ insightResult.calculationLogic?.aggregationType }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ai-center {
  display: flex;
  height: calc(100vh - 120px);

  .chat-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    background-color: #fff;
    border-radius: 4px;
    overflow: hidden;
  }

  .message-list {
    flex: 1;
    padding: 20px;
    overflow-y: auto;

    .message {
      display: flex;
      margin-bottom: 20px;

      &.user {
        justify-content: flex-end;

        .message-content {
          background-color: #409eff;
          color: #fff;
        }
      }

      &.ai, &.system {
        justify-content: flex-start;

        .message-content {
          background-color: #f0f0f0;
          color: #333;
        }
      }
    }

    .message-content {
      max-width: 70%;
      padding: 12px 16px;
      border-radius: 8px;
    }
  }

  .input-area {
    padding: 15px;
    border-top: 1px solid #eee;
    display: flex;
    gap: 10px;

    .el-input {
      flex: 1;
    }
  }

  .confirm-section {
    margin: 10px 0;

    .el-tag {
      margin-right: 5px;
    }
  }

  .d1-note {
    margin-top: 20px;
    padding: 10px;
    background-color: #e6f7ff;
    border-radius: 4px;
    color: #1890ff;
    font-size: 12px;

    .el-icon {
      margin-right: 5px;
    }
  }

  .preview-chart {
    height: 300px;
    background-color: #f9f9f9;
    border-radius: 4px;
    margin-bottom: 20px;

    .chart-placeholder {
      padding: 20px;
      text-align: center;

      .chart-data {
        max-height: 200px;
        overflow-y: auto;
        text-align: left;
        font-size: 12px;
      }
    }
  }

  .preview-info {
    margin-bottom: 20px;
    font-size: 12px;
    color: #666;
  }

  .preview-actions {
    display: flex;
    gap: 10px;
  }

  .insight-findings, .insight-recommendations {
    margin: 15px 0;

    ul {
      margin-top: 5px;
      padding-left: 20px;
    }
  }

  .insight-source, .insight-logic {
    margin: 15px 0;
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 4px;

    p {
      margin: 5px 0;
      font-size: 12px;
    }
  }
}
</style>