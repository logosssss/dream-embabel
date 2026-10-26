<script setup>
import { ref } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  note: { type: String, required: true },
  label: { type: String, default: '主题' },
  button: { type: String, required: true },
  pending: { type: String, default: '调用中' },
  initial: { type: String, default: '' },
  submit: { type: Function, required: true },
})

const text = ref(props.initial)
const busy = ref(false)
const error = ref('')
const result = ref(null)

async function onSubmit() {
  error.value = ''
  result.value = null
  busy.value = true
  try {
    result.value = await props.submit(text.value)
  } catch (ex) {
    error.value = ex.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="two">
    <form class="panel stack" @submit.prevent="onSubmit">
      <h1>{{ title }}</h1>
      <p class="sub">{{ note }}</p>
      <slot name="extra" />
      <label>
        {{ label }}
        <textarea v-model="text" />
      </label>
      <div class="row">
        <button type="submit" :disabled="busy">{{ busy ? pending : button }}</button>
      </div>
      <p class="status" :class="error ? 'error' : 'ok'">{{ error }}</p>
    </form>
    <slot :result="result" />
  </section>
</template>
