<script setup>
import LineForm from '../components/LineForm.vue'
import { replan } from '../api.js'

function heading(run) {
  if (run.attempt === 2 && run.score >= 60) {
    return '已重规划'
  }
  return run.status
}

const page = {
  title: '重规划',
  note: '两次都按主题调用默认模型。第一稿故意只写半句，固定 40 分；低于 60 就丢掉，再写成完整的一句，记 90 分。',
  label: '主题',
  button: '跑一次',
  pending: '重规划中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="replan">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.attempt">
          <p>第一稿，{{ result.discardedScore }} 分，已丢掉</p>
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
