<script setup>
import LineForm from '../components/LineForm.vue'
import { gate } from '../api.js'

function heading(run) {
  return run.entry === 'gate' ? '已按剧本' : run.status
}

const page = {
  title: '门禁',
  note: '用 PlaybookTool。countChars 一开始就能用。firstMark 在它被调用之前是锁着的，直接调用会被框架拒绝。最多 4 轮。',
  label: '主题',
  button: '按剧本写',
  pending: '执行中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="gate">
    <template #extra>
      <p>模型先看到：gate</p>
      <ul class="tools">
        <li>
          <code>countChars</code>
          <span>一开始就能用。数出主题有几个字</span>
        </li>
        <li>
          <code>firstMark</code>
          <span>锁着。必须先调用 countChars，再取第一个字</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.entry">
          <pre>{{ result.text }}</pre>
          <p>{{ result.tools ? '调用了 ' + result.tools : '这次没有记下工具调用' }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
