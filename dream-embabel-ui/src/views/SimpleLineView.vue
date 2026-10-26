<script setup>
import LineForm from '../components/LineForm.vue'
import { simpleLine } from '../api.js'

const page = {
  title: '简式',
  note: '生成这一步用 SimpleAgentBuilder.returning().running().asSubProcess()，不写在外层动作里。模型只写一句。',
  label: '主题',
  button: '简式写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="simpleLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '简式写了' : result.status }}</h1>
        <pre v-if="result.text">{{ result.text }}</pre>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
