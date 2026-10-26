<script setup>
import LineForm from '../components/LineForm.vue'
import { roleLine } from '../api.js'

const headings = { writer: '用了写作角色', reviewer: '用了审校角色' }
const reasons = {
  writer: '主题里没有「审」，所以用 writer。',
  reviewer: '主题里有「审」，所以用 reviewer。',
}

function heading(run) {
  return headings[run.role] || run.status
}

const page = {
  title: '角色',
  note: '用 withLlmByRole。代码只写角色名。主题里有「审」走 reviewer，否则走 writer。这两个角色在配置里各自指向一个模型。',
  label: '主题',
  button: '按角色写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="roleLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.role">
          <pre>{{ result.text }}</pre>
          <p>角色：{{ result.role }}</p>
          <p>{{ reasons[result.role] }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
