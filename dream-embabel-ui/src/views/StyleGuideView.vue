<script setup>
import { onMounted, ref } from 'vue'
import { searchStyleGuide, styleGuide } from '../api.js'

const status = ref(null)
const query = ref('春草')
const text = ref('')
const error = ref('')
const busy = ref(false)

onMounted(async () => {
  try {
    status.value = await styleGuide()
  } catch (ex) {
    error.value = ex.message
  }
})

async function submit() {
  error.value = ''
  text.value = ''
  busy.value = true
  try {
    const found = await searchStyleGuide(query.value)
    text.value = found.text
  } catch (ex) {
    error.value = ex.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="two">
    <form class="panel stack" @submit.prevent="submit">
      <h1>规范</h1>
      <p class="sub" v-if="status">
        {{ status.chunks }} 段 · {{ status.resource }}
      </p>
      <p class="row" v-if="status">
        <span class="chip" :class="status.milvus ? 'on' : ''">
          {{ status.milvus ? '向量检索走 Milvus' : '向量检索仍在 Lucene' }}
        </span>
      </p>
      <label>
        检索词
        <input v-model="query" />
      </label>
      <div class="row">
        <button type="submit" :disabled="busy">{{ busy ? '检索中' : '检索一次' }}</button>
      </div>
      <p class="status error">{{ error }}</p>
    </form>
    <article class="panel" v-if="text">
      <pre>{{ text }}</pre>
    </article>
  </section>
</template>
