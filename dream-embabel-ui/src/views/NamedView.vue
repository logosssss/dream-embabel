<script setup>
import LineForm from '../components/LineForm.vue'
import { namedLine } from '../api.js'

function heading(run) {
  return run.callId ? '已命名调用' : run.status
}

const page = {
  title: '日志',
  note: '用 withDefaultLlm().withId("named_line") 写一句。日志人格来自 embabel.agent.logging.personality，当前是 starwars，控制台会用星战语气。',
  label: '主题',
  button: '带 id 调用',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="namedLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.callId">
          <pre>{{ result.text }}</pre>
          <p>调用 id：{{ result.callId }}</p>
          <p v-if="result.personality">日志人格：{{ result.personality }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
