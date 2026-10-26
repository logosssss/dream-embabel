<script setup>
import LineForm from '../components/LineForm.vue'
import { utilityChoice } from '../api.js'

const headings = { ask: '选了追问', answer: '选了直接回答' }
const reasons = {
  ask: '主题里有「急」，追问的效用更高。',
  answer: '主题里没有「急」，直接回答的效用更高。',
}

function heading(run) {
  return headings[run.choice] || run.status
}

function reason(run) {
  return reasons[run.choice] || run.message
}

const page = {
  title: '效用',
  note: '用 Utility 规划器。主题里有「急」时追问效用 80，否则直接回答效用 80。选中的那一支再调用默认模型写正文。',
  label: '主题',
  button: '按效用选',
  pending: '选择中',
  initial: '这件事很急',
}
</script>

<template>
  <LineForm v-bind="page" :submit="utilityChoice">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.choice">
          <pre>{{ result.text }}</pre>
          <p>追问效用 {{ result.askValue }}，直接回答效用 {{ result.answerValue }}。</p>
          <p>{{ reason(result) }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
