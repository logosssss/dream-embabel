<script setup>
import LineForm from '../components/LineForm.vue'
import { reflect } from '../api.js'

function heading(run) {
  if (run.attempts === 2 && run.score >= 0.8) {
    return '已反思'
  }
  return run.status
}

const page = {
  title: '反思',
  note: '用 RepeatUntilAcceptable。两轮都按主题调用默认模型。第一轮固定 0.40，低于 0.80 就按意见再写；第二轮固定 0.90。最多 2 轮。',
  label: '主题',
  button: '跑反思循环',
  pending: '反思中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="reflect">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.attempts">
          <p>第一稿，{{ result.discardedScore }} 分，未通过</p>
          <pre>{{ result.discardedText }}</pre>
          <p>第二稿，{{ result.score }} 分</p>
          <pre>{{ result.text }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
