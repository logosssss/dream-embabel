<script setup>
import LineForm from '../components/LineForm.vue'
import { echoLine } from '../api.js'

const page = {
  title: '回声',
  note: '用已经连上的本机 echo MCP 工具。模型必须先调用它，只传入一个四个字以内的短词，再写一句用到返回值。主题原句不作为正文。',
  label: '主题',
  button: '先回声再写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="echoLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '先回声再写了' : result.status }}</h1>
        <template v-if="result.text">
          <p>挂上的工具 {{ result.tool }}</p>
          <pre>{{ result.text }}</pre>
          <p>{{ result.tools ? '调用了 ' + result.tools : '这次没有记下工具调用' }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
