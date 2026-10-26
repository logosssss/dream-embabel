<script setup>
import { onMounted, ref } from 'vue'
import { processes } from '../api.js'

const list = ref(null)
const error = ref('')
const busy = ref(false)

async function load() {
  error.value = ''
  busy.value = true
  try {
    list.value = await processes()
  } catch (ex) {
    error.value = ex.message
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="panel stack">
    <div class="row">
      <h1>进程</h1>
      <button type="button" :disabled="busy" @click="load">{{ busy ? '读取中' : '刷新' }}</button>
      <span v-if="list" class="chip" :class="list.persisted ? 'on' : ''">
        {{ list.persisted ? '已写入 MySQL' : '留在内存' }}
      </span>
    </div>
    <p class="sub">这里只列出 Embabel 已保存的进程快照，最近 20 条。</p>
    <p class="status" :class="error ? 'error' : ''">{{ error || list?.message }}</p>
    <table v-if="list && list.processes.length">
      <thead>
        <tr>
          <th>进程</th>
          <th>Agent</th>
          <th>状态</th>
          <th>版本</th>
          <th>更新时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in list.processes" :key="row.processId">
          <td>{{ row.processId }}</td>
          <td>{{ row.agentName }}</td>
          <td>{{ row.status }}</td>
          <td>{{ row.version }}</td>
          <td>{{ row.updatedAt }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
