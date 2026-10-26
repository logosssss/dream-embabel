<script setup>
import LineForm from '../components/LineForm.vue'
import { ref, onMounted } from 'vue'
import { guideLine, guideTools } from '../api.js'

const catalog = ref(null)
const catalogError = ref('')
const descriptions = {
  styleguide_textSearch: '按关键词检索规范',
  styleguide_vectorSearch: '按语义检索规范',
  styleguide_broadenChunk: '取出相邻片段',
  styleguide_zoomOut: '跳到所属章节',
}

function describe(tool) {
  return descriptions[tool.name] || tool.description
}

onMounted(async () => {
  try {
    catalog.value = await guideTools()
  } catch (ex) {
    catalogError.value = '工具名单还读不到。重启 Java 后再打开这一页。'
  }
})

function heading(run) {
  return run.reference === 'styleGuide' ? '按规范写了' : run.status
}

const page = {
  title: '引用',
  note: '用 withReference 挂上栏目规范。模型要先检索，再按查到的内容写一句。规范页只检索，不调用模型。',
  label: '主题',
  button: '按规范写',
  pending: '调用中',
  initial: '写一句关于春草的评论',
}
</script>

<template>
  <LineForm v-bind="page" :submit="guideLine">
    <template #extra>
      <p v-if="catalog">模型先看到：{{ catalog.reference }}</p>
      <p v-if="catalogError">{{ catalogError }}</p>
      <ul class="tools" v-if="catalog">
        <li v-for="tool in catalog.tools" :key="tool.name">
          <code>{{ tool.name }}</code>
          <span>{{ describe(tool) }}</span>
        </li>
      </ul>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.text">
          <pre>{{ result.text }}</pre>
          <p v-if="result.tools">这次调用了：{{ result.tools }}</p>
          <p v-else>这次没有记下工具调用</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
