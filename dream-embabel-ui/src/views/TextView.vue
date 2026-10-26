<script setup>
import LineForm from '../components/LineForm.vue'
import { textLine } from '../api.js'

const page = {
  title: '纯文本',
  note: '这一页用 generateText，只要一段文字。短回答和自动页用的是 creating。',
  label: '内容',
  button: '生成纯文本',
  pending: '生成中',
  initial: '写春天的河边',
}
</script>

<template>
  <LineForm v-bind="page" :submit="textLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>纯文本</h1>
        <p>这次返回的是一段文字，没有标题、正文这类字段。</p>
        <pre>{{ result.text || result.message }}</pre>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
