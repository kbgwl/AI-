<template>
  <div class="dash">
    <!-- 顶部概览横幅 -->
    <div class="hero-banner">
      <div class="hero-bg">
        <div class="hero-circle h1"></div>
        <div class="hero-circle h2"></div>
        <div class="hero-circle h3"></div>
      </div>
      <div class="hero-content">
        <div class="hero-left">
          <h1 class="hero-title">数据概览</h1>
          <p class="hero-sub">实时监控客服系统运行状态</p>
        </div>
        <div class="hero-right">
          <div class="hero-stat">
            <span class="hs-num">{{ data.todaySessions }}</span>
            <span class="hs-label">今日总会话</span>
          </div>
          <div class="hero-divider"></div>
          <div class="hero-stat">
            <span class="hs-num">{{ resolveRate }}%</span>
            <span class="hs-label">机器人解决率</span>
          </div>
          <div class="hero-divider"></div>
          <div class="hero-stat">
            <span class="hs-num">{{ data.onlineAgents }}</span>
            <span class="hs-label">在线坐席</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-row">
      <div class="metric-card" v-for="(m, i) in metrics" :key="i" :style="{ '--mc': m.color }">
        <div class="mc-icon" :style="{ background: m.bg }">
          <span>{{ m.icon }}</span>
        </div>
        <div class="mc-body">
          <div class="mc-value">{{ m.value }}</div>
          <div class="mc-label">{{ m.label }}</div>
        </div>
        <div class="mc-trend" :class="m.up ? 'up' : 'down'">
          {{ m.up ? '↑' : '↓' }} {{ m.trend }}%
        </div>
        <div class="mc-spark">
          <svg viewBox="0 0 80 30" class="spark-line">
            <polyline :points="m.sparkPoints" fill="none" :stroke="m.color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
      </div>
    </div>

    <!-- 图表双列 -->
    <div class="charts-grid">
      <!-- 会话趋势 -->
      <div class="chart-panel span-2">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">📈</span>
            <h3>会话趋势</h3>
          </div>
          <div class="ph-tabs">
            <span :class="['tab', { active: trendRange === '7d' }]" @click="trendRange = '7d'">7天</span>
            <span :class="['tab', { active: trendRange === '30d' }]" @click="trendRange = '30d'">30天</span>
          </div>
        </div>
        <div class="trend-chart">
          <div class="trend-y-axis">
            <span v-for="t in yTicks" :key="t">{{ t }}</span>
          </div>
          <div class="trend-area">
            <svg class="trend-svg" viewBox="0 0 700 200" preserveAspectRatio="none">
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#4F6EF7" stop-opacity="0.3" />
                  <stop offset="100%" stop-color="#4F6EF7" stop-opacity="0" />
                </linearGradient>
                <linearGradient id="lineGrad" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stop-color="#4F6EF7" />
                  <stop offset="100%" stop-color="#A855F7" />
                </linearGradient>
              </defs>
              <path :d="areaPath" fill="url(#areaGrad)" />
              <path :d="linePath" fill="none" stroke="url(#lineGrad)" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
              <circle v-for="(pt, i) in dataPoints" :key="i" :cx="pt.x" :cy="pt.y" r="4" fill="#fff" stroke="#4F6EF7" stroke-width="2" class="data-dot" />
            </svg>
            <div class="x-labels">
              <span v-for="(l, i) in trendLabels" :key="i">{{ l }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 机器人解决率 -->
      <div class="chart-panel">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">🎯</span>
            <h3>解决率</h3>
          </div>
        </div>
        <div class="donut-wrap">
          <svg viewBox="0 0 140 140" class="donut-svg">
            <circle cx="70" cy="70" r="58" fill="none" stroke="#F0F2F5" stroke-width="12" />
            <circle cx="70" cy="70" r="58" fill="none" stroke="url(#donutGrad)" stroke-width="12"
              stroke-linecap="round" :stroke-dasharray="364.4" :stroke-dashoffset="364.4 - 364.4 * resolveRate / 100"
              transform="rotate(-90 70 70)" class="donut-ring" />
            <defs>
              <linearGradient id="donutGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#4F6EF7" />
                <stop offset="100%" stop-color="#A855F7" />
              </linearGradient>
            </defs>
          </svg>
          <div class="donut-center">
            <span class="dc-num">{{ resolveRate }}</span>
            <span class="dc-unit">%</span>
            <span class="dc-label">解决率</span>
          </div>
        </div>
        <div class="donut-legend">
          <div class="dl-item"><span class="dl-dot" style="background:#4F6EF7"></span>机器人解决 {{ data.botResolved }}</div>
          <div class="dl-item"><span class="dl-dot" style="background:#F0F2F5"></span>转人工 {{ data.transferCount }}</div>
        </div>
      </div>

      <!-- 满意度 -->
      <div class="chart-panel">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">⭐</span>
            <h3>满意度</h3>
          </div>
        </div>
        <div class="sat-display">
          <div class="sat-score">{{ data.avgCsatScore || '-' }}</div>
          <div class="sat-stars">
            <span v-for="s in 5" :key="s" :class="['star', { filled: s <= Math.round(data.avgCsatScore || 0) }]">★</span>
          </div>
          <div class="sat-bar-row">
            <div class="sat-bar-item" v-for="(item, i) in satBreakdown" :key="i">
              <span class="sbi-label">{{ item.label }}</span>
              <div class="sbi-track"><div class="sbi-fill" :style="{ width: item.pct + '%', background: item.color }"></div></div>
              <span class="sbi-pct">{{ item.pct }}%</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部双列 -->
    <div class="bottom-grid">
      <!-- 转化漏斗 -->
      <div class="chart-panel">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">🔄</span>
            <h3>用户转化漏斗</h3>
          </div>
        </div>
        <div class="funnel">
          <div class="funnel-step" v-for="(f, i) in funnelItems" :key="i">
            <div class="fs-label">{{ f.label }}</div>
            <div class="fs-bar-track">
              <div class="fs-bar" :style="{ width: f.pct + '%', background: f.color }">
                <span class="fs-val">{{ f.value }}</span>
              </div>
            </div>
            <div class="fs-rate" v-if="i > 0">
              {{ Math.round(f.value / funnelItems[0].value * 100) }}%
            </div>
          </div>
        </div>
      </div>

      <!-- 实时动态 -->
      <div class="chart-panel">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">⚡</span>
            <h3>实时动态</h3>
          </div>
          <div class="live-dot"><span></span>在线</div>
        </div>
        <div class="live-feed">
          <div class="feed-item" v-for="(f, i) in liveFeed" :key="i">
            <div class="fi-avatar" :style="{ background: f.bg }">{{ f.icon }}</div>
            <div class="fi-body">
              <div class="fi-text">{{ f.text }}</div>
              <div class="fi-time">{{ f.time }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 待办事项 -->
      <div class="chart-panel">
        <div class="panel-head">
          <div class="ph-left">
            <span class="ph-icon">📋</span>
            <h3>待办事项</h3>
          </div>
          <span class="badge-num">{{ data.pendingTickets + data.pendingQuestions }}</span>
        </div>
        <div class="todo-list">
          <div class="todo-item" @click="$router.push('/admin/tickets')">
            <div class="ti-left">
              <span class="ti-icon">🎫</span>
              <span class="ti-text">待处理工单</span>
            </div>
            <div class="ti-right">
              <span class="ti-num">{{ data.pendingTickets }}</span>
              <el-icon class="ti-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
          <div class="todo-item" @click="$router.push('/admin/kb')">
            <div class="ti-left">
              <span class="ti-icon">❓</span>
              <span class="ti-text">未知问题待补充</span>
            </div>
            <div class="ti-right">
              <span class="ti-num">{{ data.pendingQuestions }}</span>
              <el-icon class="ti-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import { adminApi } from '../../api'

const data = ref({
  todaySessions: 0, botResolved: 0, transferCount: 0, totalUsers: 0,
  botResolveRate: '0%', onlineAgents: 0, pendingTickets: 0, pendingQuestions: 0,
  avgCsatScore: 0, avgResponseTime: 0, totalSessions: 0,
  trend: [], todayResolveRate: { botResolved: 0, transferCount: 0, rate: '0%' },
  funnel: { visitUsers: 0, startSession: 0, botResolved: 0, transferAgent: 0 }
})

const resolveRate = computed(() => parseFloat(data.value.botResolveRate) || 0)
const trendRange = ref('7d')

const genSpark = (base, variance) => Array.from({length: 7}, (_, i) => `${i * 12},${25 - (base + Math.sin(i) * variance)}`).join(' ')

const metrics = computed(() => [
  { icon: '💬', label: '今日会话', value: data.value.todaySessions, color: '#4F6EF7', bg: 'linear-gradient(135deg,#E8EDFF,#D6DEFF)', up: true, trend: 12, sparkPoints: genSpark(12, 8) },
  { icon: '🤖', label: '机器人解决', value: data.value.botResolved, color: '#00B42A', bg: 'linear-gradient(135deg,#E8FFEA,#D4F5DE)', up: true, trend: 8, sparkPoints: genSpark(10, 6) },
  { icon: '⚡', label: '平均响应', value: data.value.avgResponseTime ? data.value.avgResponseTime + 's' : '-', color: '#F59E0B', bg: 'linear-gradient(135deg,#FFF7E8,#FFEFC7)', up: false, trend: 5, sparkPoints: genSpark(14, 5) },
  { icon: '⭐', label: '满意度', value: data.value.avgCsatScore ? data.value.avgCsatScore + '分' : '-', color: '#F43F5E', bg: 'linear-gradient(135deg,#FFE8EC,#FFD6DD)', up: true, trend: 3, sparkPoints: genSpark(16, 4) },
])

const trendData = computed(() => {
  if (data.value.trend && data.value.trend.length > 0) {
    return data.value.trend.map(t => t.count)
  }
  return [0, 0, 0, 0, 0, 0, 0]
})
const trendLabels = computed(() => {
  if (data.value.trend && data.value.trend.length > 0) {
    return data.value.trend.map(t => t.label)
  }
  return ['一','二','三','四','五','六','日']
})
const yTicks = computed(() => {
  const vals = trendData.value
  const m = vals.length > 0 ? Math.max(...vals, 1) : 1
  return [m, Math.round(m*0.75), Math.round(m*0.5), Math.round(m*0.25), 0]
})

const dataPoints = computed(() => {
  const src = trendData.value
  const m = Math.max(...src, 1)
  return src.map((v, i) => ({ x: i * (700 / Math.max(src.length - 1, 1)), y: 180 - (v / m * 160) }))
})
const linePath = computed(() => {
  if (dataPoints.value.length < 2) return ''
  return 'M' + dataPoints.value.map(p => `${p.x},${p.y}`).join(' L')
})
const areaPath = computed(() => {
  if (dataPoints.value.length < 2) return ''
  const line = dataPoints.value.map(p => `${p.x},${p.y}`).join(' L')
  return `M${line} L${dataPoints.value[dataPoints.value.length-1].x},190 L${dataPoints.value[0].x},190 Z`
})

const funnelItems = computed(() => {
  const f = data.value.funnel || {}
  const total = f.visitUsers || 1
  return [
    { label: '访问用户', value: f.visitUsers || 0, pct: 100, color: 'linear-gradient(90deg,#4F6EF7,#6366F1)' },
    { label: '发起会话', value: f.startSession || 0, pct: Math.round((f.startSession || 0) / total * 100), color: 'linear-gradient(90deg,#7C3AED,#A855F7)' },
    { label: '机器人解决', value: f.botResolved || 0, pct: Math.round((f.botResolved || 0) / total * 100), color: 'linear-gradient(90deg,#06B6D4,#22D3EE)' },
    { label: '转人工', value: f.transferAgent || 0, pct: Math.round((f.transferAgent || 0) / total * 100), color: 'linear-gradient(90deg,#F97316,#FB923C)' },
  ]
})

const liveFeed = ref([
  { icon: '💬', text: '系统运行中', time: '实时', bg: '#E8EDFF' },
])

onMounted(async () => {
  try {
    const result = await adminApi.dashboard()
    if (result) data.value = { ...data.value, ...result }
  } catch (e) {}
})
</script>

<style scoped>
.dash { padding-bottom: 20px; }

/* ===== Hero Banner ===== */
.hero-banner {
  position: relative; border-radius: 20px; overflow: hidden;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #4338ca 100%);
  padding: 36px 40px; margin-bottom: 24px;
  color: #fff;
}
.hero-bg { position: absolute; inset: 0; pointer-events: none; overflow: hidden; }
.hero-circle {
  position: absolute; border-radius: 50%; border: 1px solid rgba(255,255,255,0.08);
}
.h1 { width: 300px; height: 300px; top: -120px; right: -60px; background: rgba(255,255,255,0.03); animation: heroFloat 8s ease-in-out infinite; }
.h2 { width: 200px; height: 200px; bottom: -80px; left: 30%; background: rgba(255,255,255,0.02); animation: heroFloat 6s ease-in-out infinite reverse; }
.h3 { width: 120px; height: 120px; top: 20px; left: -30px; background: rgba(255,255,255,0.02); animation: heroFloat 10s ease-in-out infinite 1s; }
@keyframes heroFloat { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-15px)} }

.hero-content { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 24px; }
.hero-title { font-size: 28px; font-weight: 700; margin: 0 0 6px; }
.hero-sub { font-size: 14px; opacity: 0.7; margin: 0; }
.hero-right { display: flex; align-items: center; gap: 32px; }
.hero-stat { text-align: center; }
.hs-num { display: block; font-size: 32px; font-weight: 700; line-height: 1.2; }
.hs-label { font-size: 12px; opacity: 0.6; margin-top: 4px; display: block; }
.hero-divider { width: 1px; height: 40px; background: rgba(255,255,255,0.15); }

/* ===== Metrics Row ===== */
.metrics-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.metric-card {
  background: #fff; border-radius: 16px; padding: 20px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
  display: flex; align-items: center; gap: 14px;
  position: relative; overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4,0,0.2,1);
}
.metric-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.mc-icon {
  width: 48px; height: 48px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px; flex-shrink: 0;
}
.mc-body { flex: 1; min-width: 0; }
.mc-value { font-size: 24px; font-weight: 700; color: #1e1b4b; }
.mc-label { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.mc-trend {
  position: absolute; top: 12px; right: 12px;
  font-size: 12px; font-weight: 600; padding: 2px 8px; border-radius: 20px;
}
.mc-trend.up { color: #00B42A; background: #E8FFEA; }
.mc-trend.down { color: #F43F5E; background: #FFE8EC; }
.mc-spark { position: absolute; bottom: 0; right: 0; width: 80px; height: 30px; opacity: 0.4; }
.spark-line { width: 100%; height: 100%; }

/* ===== Charts Grid ===== */
.charts-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.chart-panel {
  background: #fff; border-radius: 16px; padding: 24px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.04);
}
.chart-panel.span-2 { grid-column: span 2; }
.panel-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.ph-left { display: flex; align-items: center; gap: 8px; }
.ph-icon { font-size: 18px; }
.panel-head h3 { font-size: 15px; font-weight: 600; color: #1e1b4b; margin: 0; }
.ph-tabs { display: flex; gap: 4px; background: #f1f5f9; border-radius: 8px; padding: 3px; }
.ph-tabs .tab {
  padding: 4px 12px; border-radius: 6px; font-size: 12px; cursor: pointer;
  color: #64748b; transition: all 0.2s; font-weight: 500;
}
.ph-tabs .tab.active { background: #fff; color: #1e1b4b; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }

/* ===== Trend Chart ===== */
.trend-chart { display: flex; gap: 12px; }
.trend-y-axis {
  display: flex; flex-direction: column; justify-content: space-between;
  font-size: 11px; color: #94a3b8; padding: 0 4px; min-width: 30px; text-align: right;
}
.trend-area { flex: 1; position: relative; }
.trend-svg { width: 100%; height: 180px; }
.data-dot { transition: r 0.2s; }
.data-dot:hover { r: 6; }
.x-labels {
  display: flex; justify-content: space-between; margin-top: 8px;
  font-size: 12px; color: #94a3b8;
}

/* ===== Donut ===== */
.donut-wrap { position: relative; display: flex; justify-content: center; margin-bottom: 16px; }
.donut-svg { width: 140px; height: 140px; }
.donut-ring { transition: stroke-dashoffset 1.2s cubic-bezier(0.4,0,0.2,1); }
.donut-center {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  text-align: center;
}
.dc-num { font-size: 32px; font-weight: 700; color: #1e1b4b; }
.dc-unit { font-size: 16px; color: #1e1b4b; }
.dc-label { display: block; font-size: 12px; color: #94a3b8; }
.donut-legend { display: flex; justify-content: center; gap: 20px; }
.dl-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; }
.dl-dot { width: 8px; height: 8px; border-radius: 50%; }

/* ===== Satisfaction ===== */
.sat-display { text-align: center; }
.sat-score { font-size: 48px; font-weight: 700; color: #1e1b4b; line-height: 1; }
.sat-stars { margin: 8px 0 20px; }
.sat-stars .star { font-size: 24px; color: #e2e8f0; transition: color 0.3s; }
.sat-stars .star.filled { color: #F59E0B; }
.sat-bar-row { text-align: left; }
.sat-bar-item { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.sbi-label { font-size: 11px; color: #94a3b8; width: 56px; flex-shrink: 0; }
.sbi-track { flex: 1; height: 6px; background: #f1f5f9; border-radius: 3px; overflow: hidden; }
.sbi-fill { height: 100%; border-radius: 3px; transition: width 0.8s cubic-bezier(0.4,0,0.2,1); }
.sbi-pct { font-size: 11px; color: #64748b; width: 32px; text-align: right; }

/* ===== Bottom Grid ===== */
.bottom-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }

/* ===== Funnel ===== */
.funnel { display: flex; flex-direction: column; gap: 12px; }
.funnel-step { display: flex; align-items: center; gap: 12px; }
.fs-label { font-size: 12px; color: #64748b; width: 70px; flex-shrink: 0; text-align: right; }
.fs-bar-track { flex: 1; height: 32px; background: #f8fafc; border-radius: 8px; overflow: hidden; }
.fs-bar {
  height: 100%; border-radius: 8px;
  display: flex; align-items: center; justify-content: flex-end; padding-right: 10px;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}
.fs-val { font-size: 12px; font-weight: 600; color: #fff; }
.fs-rate { font-size: 12px; color: #94a3b8; width: 36px; }

/* ===== Live Feed ===== */
.live-dot { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #00B42A; font-weight: 500; }
.live-dot span { width: 6px; height: 6px; border-radius: 50%; background: #00B42A; animation: pulse 2s infinite; }
@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.4} }

.live-feed { display: flex; flex-direction: column; gap: 12px; max-height: 320px; overflow-y: auto; }
.feed-item { display: flex; gap: 10px; align-items: flex-start; }
.fi-avatar {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px; flex-shrink: 0;
}
.fi-body { flex: 1; min-width: 0; }
.fi-text { font-size: 13px; color: #334155; line-height: 1.4; }
.fi-time { font-size: 11px; color: #94a3b8; margin-top: 2px; }

/* ===== Todo List ===== */
.badge-num {
  background: #F43F5E; color: #fff; font-size: 12px; font-weight: 600;
  padding: 2px 10px; border-radius: 20px;
}
.todo-list { display: flex; flex-direction: column; gap: 8px; }
.todo-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-radius: 12px; cursor: pointer;
  background: #f8fafc; transition: all 0.2s;
}
.todo-item:hover { background: #eef2ff; transform: translateX(4px); }
.ti-left { display: flex; align-items: center; gap: 10px; }
.ti-icon { font-size: 18px; }
.ti-text { font-size: 13px; color: #334155; font-weight: 500; }
.ti-right { display: flex; align-items: center; gap: 6px; }
.ti-num { font-size: 18px; font-weight: 700; color: #1e1b4b; }
.ti-arrow { color: #cbd5e1; }

/* ===== Responsive ===== */
@media (max-width: 1200px) {
  .charts-grid { grid-template-columns: repeat(2, 1fr); }
  .bottom-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .hero-banner { padding: 24px 20px; }
  .hero-content { flex-direction: column; align-items: flex-start; }
  .hero-right { gap: 20px; }
  .hs-num { font-size: 24px; }
  .metrics-row { grid-template-columns: repeat(2, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
  .chart-panel.span-2 { grid-column: span 1; }
  .metric-card { padding: 16px; }
  .mc-value { font-size: 20px; }
}
@media (max-width: 480px) {
  .metrics-row { grid-template-columns: 1fr; }
  .hero-right { flex-wrap: wrap; }
}
</style>
