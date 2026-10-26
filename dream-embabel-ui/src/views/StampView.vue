<script setup>
import LineForm from '../components/LineForm.vue'
import { stamp } from '../api.js'

function heading(run) {
  return run.tool === 'stamp' ? '已盖章' : run.status
}

const page = {
  title: '函数工具',
  note: '用 Tool.fromInstance 把一个普通方法收成工具。模型必须先调用它，把返回的章放在第一行，再按主题写一句。',
  label: '主题',
  button: '盖章再写',
  pending: '盖章中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="stamp">
    <template #extra>
      <ul class="tools">
        <li>
          <code>stamp</code>
          <span>数出主题有几个字，返回「章：N字」</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.tool">
          <pre>{{ result.text }}</pre>
          <p>{{ result.tools ? '调用了 ' + result.tools : '这次没有记下工具调用' }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
