<script setup>
import LineForm from '../components/LineForm.vue'
import { unfold } from '../api.js'

function heading(run) {
  return run.entry === 'desk' ? '已展开' : run.status
}

const page = {
  title: '展开',
  note: '用 UnfoldingTool。模型一开始只看得到目录 desk，调用之后才会出现下面两个工具。这次要求展开后用 countChars。',
  label: '主题',
  button: '先展开再写',
  pending: '展开中',
  initial: '用一句话介绍 Java',
}
</script>

<template>
  <LineForm v-bind="page" :submit="unfold">
    <template #extra>
      <p>模型先看到：desk</p>
      <ul class="tools">
        <li>
          <code>countChars</code>
          <span>数出主题有几个字，返回「字数：N」</span>
        </li>
        <li>
          <code>firstMark</code>
          <span>取主题的第一个字，返回「首字：X」</span>
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
