<script setup>
import { ref } from 'vue'
import { formStart, formSubmit } from '../api.js'

const processId = ref('')
const title = ref('')
const fields = ref([])
const column = ref('专栏')
const point = ref('春草发芽，标题要点题')
const busy = ref(false)
const error = ref('')
const result = ref(null)

async function start() {
  error.value = ''
  result.value = null
  busy.value = true
  try {
    const waiting = await formStart()
    processId.value = waiting.processId
    title.value = waiting.title
    fields.value = waiting.fields || []
  } catch (ex) {
    error.value = ex.message
  } finally {
    busy.value = false
  }
}

async function submit() {
  error.value = ''
  result.value = null
  busy.value = true
  try {
    result.value = await formSubmit(processId.value, column.value, point.value)
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
      <h1>表单</h1>
      <p class="sub">先用 WaitFor.formSubmission 把进程停住。填完栏目和要点再提交，模型才写一句。</p>
      <div class="row">
        <button type="button" :disabled="busy" @click="start">{{ busy && !processId ? '调用中' : '停在表单' }}</button>
      </div>
      <template v-if="processId">
        <p>{{ title }}</p>
        <p class="sub" v-if="fields.length">字段 {{ fields.map(field => field.id + ' ' + field.label).join('，') }}</p>
        <label>
          栏目
          <input v-model="column" />
        </label>
        <label>
          要点
          <input v-model="point" />
        </label>
        <div class="row">
          <button type="submit" :disabled="busy">{{ busy ? '调用中' : '按表单写' }}</button>
        </div>
      </template>
      <p class="status" :class="error ? 'error' : 'ok'">{{ error }}</p>
    </form>
    <article class="panel stack" v-if="result">
      <h1>{{ result.text ? '按表单写了' : result.status }}</h1>
      <pre v-if="result.text">{{ result.text }}</pre>
      <p v-else>{{ result.message }}</p>
      <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
    </article>
  </section>
</template>
