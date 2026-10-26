<script setup>
import LineForm from '../components/LineForm.vue'
import { reply } from '../api.js'

const page = {
  title: '短回答',
  note: '只调用一次模型。',
  label: '问题',
  button: '发送',
  pending: '回答中',
  initial: '用一句话介绍这个项目',
}
</script>

<template>
  <LineForm v-bind="page" :submit="reply">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <p class="sub">{{ result.status }} · {{ result.processId }}</p>
        <pre>{{ result.reply || result.message }}</pre>
      </article>
    </template>
  </LineForm>
</template>
