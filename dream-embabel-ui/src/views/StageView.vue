<script setup>
import LineForm from '../components/LineForm.vue'
import { stageRun } from '../api.js'

function heading(run) {
  return run.stage === 'prepare' ? '监督已分派' : run.status
}

const page = {
  title: '监督',
  note: '用 Supervisor 规划器。监督者调用模型，先让准备写一句，再收成结果。准备用默认模型，成稿不再写新内容。',
  label: '主题',
  button: '让监督者分派',
  pending: '分派中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="stageRun">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.stage">
          <pre>{{ result.text }}</pre>
          <p>监督者先调用了准备，再把这一句收成结果。</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
