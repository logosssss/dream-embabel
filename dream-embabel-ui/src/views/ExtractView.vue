<script setup>
import LineForm from '../components/LineForm.vue'
import { maybeNote } from '../api.js'

function heading(run) {
  if (run.found === true) {
    return '抽到了'
  }
  if (run.found === false) {
    return '没抽到'
  }
  return run.status
}

const page = {
  title: '抽取',
  note: '这一页用 createObjectIfPossible。抽到就给出标题和正文，抽不到就明确说没抽到。',
  label: '内容',
  button: '尝试抽取',
  pending: '抽取中',
  initial: '写春天的河边',
}
</script>

<template>
  <LineForm v-bind="page" :submit="maybeNote">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.found">
          <p>{{ result.title }}</p>
          <pre>{{ result.body }}</pre>
        </template>
        <p v-else-if="result.found === false">这次没有抽出标题和正文。</p>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
