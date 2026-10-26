<script setup>
import { onUnmounted, ref } from 'vue'
import { confirmReview, reviewRun, startReview } from '../api.js'

const topic = ref('写春天的河边')
const busy = ref(false)
const error = ref('')
const steps = ref([])
const result = ref(null)
let timer = 0

const labels = {
  pending: '未开始',
  running: '进行中',
  done: '完成',
  failed: '失败',
}

function label(state) {
  return labels[state] || state
}

function stop() {
  window.clearTimeout(timer)
  timer = 0
}

async function follow(runId) {
  const run = await reviewRun(runId)
  steps.value = run.steps || []
  if (run.status === 'RUNNING') {
    timer = window.setTimeout(() => {
      follow(runId).catch(fail)
    }, 700)
    return
  }
  busy.value = false
  result.value = run
  if (run.status === 'FAILED' || run.status === 'NOT_FOUND') {
    error.value = run.message || '审稿失败'
  }
}

function fail(ex) {
  stop()
  busy.value = false
  error.value = ex.message
}

async function confirm() {
  if (!result.value || !result.value.runId) {
    return
  }
  busy.value = true
  error.value = ''
  try {
    await confirmReview(result.value.runId)
    await follow(result.value.runId)
  } catch (ex) {
    fail(ex)
  }
}
async function submit() {
  stop()
  error.value = ''
  result.value = null
  steps.value = []
  busy.value = true
  try {
    const run = await startReview(topic.value)
    steps.value = run.steps || []
    await follow(run.runId)
  } catch (ex) {
    fail(ex)
  }
}

onUnmounted(stop)
</script>

<template>
  <section class="two">
    <form class="panel stack" @submit.prevent="submit">
      <h1>审稿</h1>
      <p class="sub">等待时会按这次规划的动作名往前亮。通常要一两分钟。</p>
      <label>
        主题
        <textarea v-model="topic" />
      </label>
      <div class="row">
        <button type="submit" :disabled="busy">{{ busy ? '审稿中' : '开始审稿' }}</button>
      </div>
      <p class="status" :class="error ? 'error' : 'ok'">{{ error }}</p>
    </form>
    <article class="panel stack" v-if="busy || steps.length || result">
      <p class="sub" v-if="busy && !steps.length">正在规划</p>
      <ol class="plan" v-if="steps.length">
        <li v-for="(step, index) in steps" :key="index" :class="step.state">
          <code>{{ step.name }}</code>
          <span>{{ label(step.state) }}</span>
          <span v-if="step.message">{{ step.message }}</span>
        </li>
      </ol>
      <template v-if="result && result.status === 'WAITING'">
        <p class="sub">{{ result.message }}</p>
        <h1>{{ result.title || '初稿' }}</h1>
        <pre>{{ result.body || '初稿还没有正文。' }}</pre>
        <div class="row">
          <button type="button" :disabled="busy" @click="confirm">采用这篇初稿，继续审校</button>
        </div>
      </template>
      <template v-else-if="result && result.title">
        <p class="sub">{{ result.status }}<template v-if="result.processId"> · {{ result.processId }}</template></p>
        <h1>{{ result.title }}</h1>
        <pre>{{ result.content || result.body || result.message }}</pre>
      </template>
      <p class="sub" v-else-if="result && result.status !== 'RUNNING' && result.status !== 'FAILED'">{{ result.status }} · {{ result.message }}</p>
    </article>
  </section>
</template>
