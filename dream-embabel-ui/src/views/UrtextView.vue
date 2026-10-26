<script setup>
import LineForm from '../components/LineForm.vue'
import { urtextLine } from '../api.js'

const page = {
  title: '原文',
  note: '入库时 AddTitlesChunkTransformer 把标题写进片段正文。模型只看 urtext，也就是去掉标题前缀的原文。主题里有「评论」对评论，有「通讯」对通讯，否则对春草。',
  label: '主题',
  button: '按原文写',
  pending: '调用中',
  initial: '用原文写一句关于春草',
}
</script>

<template>
  <LineForm v-bind="page" :submit="urtextLine">
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ result.text ? '按原文写了' : result.status }}</h1>
        <template v-if="result.text">
          <p>增强后的片段</p>
          <pre>{{ result.enhanced }}</pre>
          <p>交给模型的原文</p>
          <pre>{{ result.urtext }}</pre>
          <p>模型写的</p>
          <pre>{{ result.text }}</pre>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
