<script setup>
import LineForm from '../components/LineForm.vue'
import { consensus } from '../api.js'

function heading(run) {
  if (run.verdict === '达成共识') {
    return '达成共识：' + run.agreed
  }
  return run.verdict || run.status
}

const page = {
  title: '共识',
  note: '用 ConsensusBuilder。两路都调用默认模型，温度分别是 0 和 0.8。代码只比较第一行的「是」或「否」。两行理由都留在页面上。',
  label: '主题',
  button: '两路比较',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="consensus">
    <template #extra>
      <ul class="tools">
        <li>
          <code>低温</code>
          <span>温度 0。第一行回答是或否，第二行写理由</span>
        </li>
        <li>
          <code>高温</code>
          <span>温度 0.8。问的是同一个问题</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.cool">
          <p>低温</p>
          <pre>{{ result.cool }}</pre>
          <p>高温</p>
          <pre>{{ result.warm }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
