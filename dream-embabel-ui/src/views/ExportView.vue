<script setup>
import LineForm from '../components/LineForm.vue'
import { exportLine } from '../api.js'

const page = {
  title: '导出',
  note: '目标标了 @Export(remote = true, name = "oneLine")，起点是 UserInput。本机没有 MCP server starter，所以没有另开传输。模型仍写一句。',
  label: '主题',
  button: '按导出写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="exportLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '按导出写了' : result.status }}</h1>
        <template v-if="result.text">
          <p>导出名 {{ result.name }} · remote {{ result.remote }}</p>
          <pre>{{ result.text }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
