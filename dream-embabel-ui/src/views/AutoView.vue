<script setup>
import LineForm from '../components/LineForm.vue'
import { autoReply } from '../api.js'

const page = {
  title: '自动',
  note: '这一档用 withAutoLlm，由框架按这次任务挑模型。短回答用的是默认模型。',
  label: '内容',
  button: '自动选模型',
  pending: '调用中',
  initial: '帮我看一下这段话',
}
</script>

<template>
  <LineForm v-bind="page" :submit="autoReply">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.selection === 'auto' ? '自动选模型' : result.status }}</h1>
        <p v-if="result.selection === 'auto'">这次没有指定默认模型，也没有指定 writer 或 reviewer。</p>
        <pre>{{ result.text || result.message }}</pre>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
