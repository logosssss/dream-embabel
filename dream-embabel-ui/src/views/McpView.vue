<script setup>
import { onMounted, ref } from 'vue'
import { mcpStatus, mcpTools } from '../api.js'

const status = ref(null)
const tools = ref([])
const error = ref('')
const busy = ref(false)

onMounted(async () => {
  try {
    status.value = await mcpStatus()
  } catch (ex) {
    error.value = ex.message
  }
})

async function listTools() {
  error.value = ''
  tools.value = []
  busy.value = true
  try {
    tools.value = await mcpTools()
  } catch (ex) {
    error.value = ex.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="two">
    <div class="panel stack">
      <h1>MCP</h1>
      <p class="sub">{{ status?.message || '已有客户端会列在下面。' }}</p>
      <p class="row">
        <span
          v-for="client in status?.clients || []"
          :key="client.name"
          class="chip"
          :class="client.connected ? 'on' : ''"
        >
          {{ client.name }}
        </span>
      </p>
      <div class="row">
        <button type="button" :disabled="busy" @click="listTools">
          {{ busy ? '读取中' : '列出工具' }}
        </button>
      </div>
      <p class="status error">{{ error }}</p>
    </div>
    <article class="panel stack" v-if="tools.length">
      <div v-for="group in tools" :key="group.name || 'none'">
        <p class="sub">{{ group.name || '未连接' }}</p>
        <pre>{{ group.message || group.tools.join('\n') || '没有工具' }}</pre>
      </div>
    </article>
  </section>
</template>
