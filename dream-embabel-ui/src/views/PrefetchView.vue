<script setup>
import LineForm from '../components/LineForm.vue'
import { prefetch } from '../api.js'

function heading(run) {
  return run.text ? '先预取再写' : run.status
}

const page = {
  title: '预取',
  note: '用 withEagerSearchAbout。模型开口前先按主题做语义检索，取最接近的两段放进引用。然后再调用模型写一句。',
  label: '主题',
  button: '先预取再写',
  pending: '调用中',
  initial: '写一句关于春草的评论',
}
</script>

<template>
  <LineForm v-bind="page" :submit="prefetch">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.text">
          <p>预取到的资料</p>
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
