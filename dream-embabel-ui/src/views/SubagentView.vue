<script setup>
import LineForm from '../components/LineForm.vue'
import { subagentLine } from '../api.js'

const page = {
  title: '子代理',
  note: '用 Subagent.ofClass(SimpleLineAgent).consuming(UserInput) 把简式代理挂成工具。模型必须先调用它，再转述一句。',
  label: '主题',
  button: '交给子代理',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="subagentLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '子代理转述了' : result.status }}</h1>
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
