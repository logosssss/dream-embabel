<script setup>
import LineForm from '../components/LineForm.vue'
import { filterLine } from '../api.js'

function heading(run) {
  return run.section ? '只留下' + run.section : run.status
}

const page = {
  title: '过滤',
  note: '用 withMetadataFilter。按章节标题 leaf_section_title 过滤，模型看不到这条条件。主题里有「通讯」只留通讯，有「评论」只留评论，否则只留专栏。然后再写一句。',
  label: '主题',
  button: '过滤后再写',
  pending: '调用中',
  initial: '按评论的规范写一句关于春草',
}
</script>

<template>
  <LineForm v-bind="page" :submit="filterLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.text">
          <p>留下来的资料</p>
          <pre>{{ result.hint }}</pre>
          <p>模型写的</p>
          <pre>{{ result.text }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
