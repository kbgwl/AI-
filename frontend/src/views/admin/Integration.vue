<template>
  <div class="integration-page">
    <div class="page-header">
      <h2>系统接入</h2>
      <p class="page-desc">将AI客服嵌入到您的网站或应用中</p>
    </div>

    <!-- 接入方式选择 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 网页接入 -->
      <el-tab-pane label="网页接入" name="web">
        <div class="integration-section">
          <h3>方式一：iframe 嵌入（推荐）</h3>
          <p class="desc">最简单的接入方式，复制以下代码粘贴到您网页的任意位置：</p>
          <div class="code-box">
            <pre><code>{{ iframeCode }}</code></pre>
            <el-button class="copy-btn" size="small" @click="copyCode(iframeCode)">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>

          <h3>方式二：JavaScript SDK</h3>
          <p class="desc">更灵活的接入方式，支持自定义配置：</p>
          <div class="code-box">
            <pre><code>{{ sdkCode }}</code></pre>
            <el-button class="copy-btn" size="small" @click="copyCode(sdkCode)">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>

          <h3>配置参数</h3>
          <el-table :data="configParams" border style="width: 100%">
            <el-table-column prop="param" label="参数" width="180" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="required" label="必填" width="80" />
            <el-table-column prop="desc" label="说明" />
          </el-table>
        </div>
      </el-tab-pane>

      <!-- API接入 -->
      <el-tab-pane label="API 接入" name="api">
        <div class="integration-section">
          <h3>REST API 文档</h3>
          <p class="desc">通过API接口与AI客服进行交互</p>

          <el-collapse v-model="activeApi">
            <el-collapse-item title="创建会话" name="createSession">
              <div class="api-doc">
                <el-tag type="success">POST</el-tag>
                <code>/api/chat/session</code>
                <div class="api-desc">
                  <p><strong>请求参数：</strong></p>
                  <el-table :data="[
                    { param: 'channel', type: 'string', required: '是', desc: '渠道标识，如 web、wechat、app' },
                    { param: 'userId', type: 'string', required: '否', desc: '用户标识，用于关联用户' }
                  ]" border size="small">
                    <el-table-column prop="param" label="参数" width="120" />
                    <el-table-column prop="type" label="类型" width="80" />
                    <el-table-column prop="required" label="必填" width="60" />
                    <el-table-column prop="desc" label="说明" />
                  </el-table>
                  <p style="margin-top: 12px"><strong>响应示例：</strong></p>
                  <pre><code>{
  "code": 200,
  "data": {
    "sessionId": "ws-xxxxxxxxxxxx",
    "channel": "web"
  }
}</code></pre>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="发送消息" name="sendMessage">
              <div class="api-doc">
                <el-tag type="success">POST</el-tag>
                <code>/api/chat/send</code>
                <div class="api-desc">
                  <p><strong>请求参数：</strong></p>
                  <el-table :data="[
                    { param: 'sessionId', type: 'string', required: '是', desc: '会话ID' },
                    { param: 'content', type: 'string', required: '是', desc: '消息内容' },
                    { param: 'msgType', type: 'string', required: '否', desc: '消息类型：text(默认)、image、file' }
                  ]" border size="small">
                    <el-table-column prop="param" label="参数" width="120" />
                    <el-table-column prop="type" label="类型" width="80" />
                    <el-table-column prop="required" label="必填" width="60" />
                    <el-table-column prop="desc" label="说明" />
                  </el-table>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="获取会话历史" name="getHistory">
              <div class="api-doc">
                <el-tag>GET</el-tag>
                <code>/api/chat/history/{sessionId}</code>
                <div class="api-desc">
                  <p><strong>响应：</strong>返回该会话的所有历史消息列表</p>
                </div>
              </div>
            </el-collapse-item>

            <el-collapse-item title="WebSocket 实时通信" name="websocket">
              <div class="api-doc">
                <el-tag type="warning">WS</el-tag>
                <code>ws://your-domain/ws/chat</code>
                <div class="api-desc">
                  <p><strong>连接后发送：</strong></p>
                  <pre><code>{
  "type": "join",
  "sessionId": "ws-xxxxxxxxxxxx"
}</code></pre>
                  <p><strong>接收消息格式：</strong></p>
                  <pre><code>{
  "type": "message",
  "data": {
    "senderType": "ai",
    "content": "回复内容",
    "createTime": "2026-01-01T00:00:00"
  }
}</code></pre>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </el-tab-pane>

      <!-- 微信接入 -->
      <el-tab-pane label="微信接入" name="wechat">
        <div class="integration-section">
          <h3>微信公众号接入</h3>
          <div class="step-list">
            <div class="step">
              <div class="step-num">1</div>
              <div class="step-content">
                <h4>登录微信公众平台</h4>
                <p>进入 <strong>开发 → 基本配置</strong>，获取 AppID 和 AppSecret</p>
              </div>
            </div>
            <div class="step">
              <div class="step-num">2</div>
              <div class="step-content">
                <h4>配置服务器</h4>
                <p>在 <strong>开发 → 基本配置 → 服务器配置</strong> 中填写：</p>
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="URL">{{ serverUrl }}/wechat/callback</el-descriptions-item>
                  <el-descriptions-item label="Token">自定义Token（与后台一致）</el-descriptions-item>
                  <el-descriptions-item label="EncodingAESKey">随机生成</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>
            <div class="step">
              <div class="step-num">3</div>
              <div class="step-content">
                <h4>后台配置</h4>
                <p>进入管理后台 → 渠道管理 → 微信公众号，填入 AppID、AppSecret、Token</p>
              </div>
            </div>
            <div class="step">
              <div class="step-num">4</div>
              <div class="step-content">
                <h4>启用服务</h4>
                <p>点击「验证并启用」，公众号即可自动回复用户消息</p>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 小程序接入 -->
      <el-tab-pane label="小程序接入" name="miniapp">
        <div class="integration-section">
          <h3>微信小程序接入</h3>
          <p class="desc">在小程序中集成AI客服组件</p>
          <div class="code-box">
            <pre><code>{{ miniappCode }}</code></pre>
            <el-button class="copy-btn" size="small" @click="copyCode(miniappCode)">
              <el-icon><CopyDocument /></el-icon> 复制
            </el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('web')
const activeApi = ref(['createSession'])
const serverUrl = computed(() => window.location.origin)

const iframeCode = computed(() => `<!-- 空白格AI客服 - iframe嵌入 -->
<iframe 
  src="${window.location.origin}/chat" 
  width="400" 
  height="600" 
  frameborder="0"
  style="position: fixed; bottom: 20px; right: 20px; z-index: 9999; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.15);"
></iframe>`)

const sdkCode = computed(() => `<!-- 空白格AI客服 - JavaScript SDK -->
<div id="ai-cs-widget"></div>
<script src="${window.location.origin}/ai-cs-sdk.js"><\/script>
<script>
  AI_CS.init({
    container: '#ai-cs-widget',
    serverUrl: '${window.location.origin}',
    position: 'bottom-right',  // bottom-right | bottom-left
    theme: 'purple',           // purple | blue | green
    title: 'AI智能客服',
    greeting: '您好！请问有什么可以帮您？'
  });
<\/script>`)

const miniappCode = computed(() => `// 小程序客服组件使用示例
// 在 wxml 中添加：
<web-view src="${window.location.origin}/chat?channel=miniapp"></web-view>

// 或使用客服消息接口：
<button open-type="contact" bindcontact="onContact">
  联系客服
</button>`)

const configParams = [
  { param: 'container', type: 'String', required: '是', desc: '容器CSS选择器，如 #ai-cs-widget' },
  { param: 'serverUrl', type: 'String', required: '是', desc: '客服系统服务器地址' },
  { param: 'position', type: 'String', required: '否', desc: '显示位置：bottom-right（默认）| bottom-left' },
  { param: 'theme', type: 'String', required: '否', desc: '主题颜色：purple（默认）| blue | green' },
  { param: 'title', type: 'String', required: '否', desc: '客服窗口标题' },
  { param: 'greeting', type: 'String', required: '否', desc: '欢迎语' },
  { param: 'userId', type: 'String', required: '否', desc: '用户唯一标识，用于关联用户信息' },
]

function copyCode(code) {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('代码已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}
</script>

<style scoped>
.integration-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  color: #303133;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.integration-section {
  padding: 16px 0;
}

.integration-section h3 {
  margin: 24px 0 12px;
  font-size: 16px;
  color: #303133;
}

.integration-section h3:first-child {
  margin-top: 0;
}

.desc {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
}

.code-box {
  position: relative;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.code-box pre {
  margin: 0;
  overflow-x: auto;
}

.code-box code {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}

.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
}

.api-doc {
  padding: 8px 0;
}

.api-doc code {
  font-size: 14px;
  color: #409eff;
  margin-left: 8px;
}

.api-desc {
  margin-top: 12px;
  padding-left: 16px;
}

.api-desc pre {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin: 8px 0;
}

.step-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.step {
  display: flex;
  gap: 16px;
}

.step-num {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #303133;
}

.step-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}
</style>
