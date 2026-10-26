<script setup>
import LineForm from '../components/LineForm.vue'
import { fallbackLine } from '../api.js'

function heading(run) {
  return run.second ? '用了后面那个模型' : run.status
}

const page = {
  title: '兜底',
  note: '用 withFirstAvailableLlmOf。按名字从左到右找，只用第一个已经登记的模型。同一次调用不会把同一句发给两个模型。',
  label: '主题',
  button: '按顺序写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="fallbackLine">
    <template #extra>
      <ul class="tools">
        <li>
          <code>missing-model</code>
          <span>先找这个。项目里没有登记，会跳过</span>
        </li>
        <li>
          <code>deepseek-flash</code>
          <span>后面这个。DeepSeek 配置里有它，会用它写</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.text">
          <pre>{{ result.text }}</pre>
          <p>顺序：{{ result.first }}，然后 {{ result.second }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
