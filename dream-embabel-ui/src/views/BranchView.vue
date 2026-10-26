<script setup>
import LineForm from '../components/LineForm.vue'
import { branch } from '../api.js'

const headings = { code: '走了代码支', plain: '走了普通支' }
const reasons = {
  code: '主题里有「代码」或 code，规划只放行代码支。',
  plain: '主题里没有「代码」或 code，规划只放行普通支。',
}

function heading(run) {
  return headings[run.branch] || run.status
}

function reason(run) {
  return reasons[run.branch] || run.message
}

const page = {
  title: '分支',
  note: '主题里出现「代码」或 code 走代码支，否则走普通支。规划只放行一支，这一支会调用模型，按主题写一句。',
  label: '主题',
  button: '选择分支',
  pending: '规划中',
  initial: '请审这段代码',
}
</script>

<template>
  <LineForm v-bind="page" :submit="branch">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <pre v-if="result.text">{{ result.text }}</pre>
        <p>{{ reason(result) }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
