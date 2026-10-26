<script setup>
import LineForm from '../components/LineForm.vue'
import { memoryLine } from '../api.js'

const page = {
  title: '记忆',
  note: '两条事实是 NamedEntityData，按标签放在内存仓库里。主题里有「评论」用 findByLabel 取「忌讳」，否则取「术语」。不走向量检索。模型只看取出来的那一条。',
  label: '主题',
  button: '按记忆写',
  pending: '调用中',
  initial: '用记忆写一句关于春草',
}
</script>

<template>
  <LineForm v-bind="page" :submit="memoryLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '按记忆写了' : result.status }}</h1>
        <template v-if="result.text">
          <p>标签 {{ result.label }} · {{ result.name }}</p>
          <pre>{{ result.fact }}</pre>
          <p>模型写的</p>
          <pre>{{ result.text }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
