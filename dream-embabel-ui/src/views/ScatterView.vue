<script setup>
import LineForm from '../components/LineForm.vue'
import { scatter } from '../api.js'

function heading(run) {
  return run.benefit && run.cost ? '两边都写了' : run.status
}

const page = {
  title: '分发',
  note: '用 ScatterGatherBuilder。同一主题分两路，各调用一次模型。合成在代码里完成，不再问模型。',
  label: '主题',
  button: '分两路写',
  pending: '调用中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="scatter">
    <template #extra>
      <ul class="tools">
        <li>
          <code>好处</code>
          <span>只写这个主题的一个好处</span>
        </li>
        <li>
          <code>代价</code>
          <span>只写这个主题的一个代价</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.benefit">
          <p>好处</p>
          <pre>{{ result.benefit }}</pre>
          <p>代价</p>
          <pre>{{ result.cost }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
