<script setup>
import LineForm from '../components/LineForm.vue'
import { ref, onMounted } from 'vue'
import { mathSum, mathTools } from '../api.js'

const catalog = ref(null)
const catalogError = ref('')
const descriptions = {
  add: '两个数相加',
  subtract: '第一个数减去第二个数',
  multiply: '两个数相乘',
  divide: '第一个数除以第二个数',
  mean: '求一组数的平均数',
  min: '取一组数里的最小值',
  max: '取一组数里的最大值',
  floor: '向下取整',
  ceiling: '向上取整',
  round: '四舍五入到整数',
}

function listed(run) {
  const order = Object.keys(descriptions)
  return [...run.tools].sort((left, right) => {
    const leftAt = order.indexOf(left.name)
    const rightAt = order.indexOf(right.name)
    return (leftAt < 0 ? order.length : leftAt) - (rightAt < 0 ? order.length : rightAt)
  })
}

function describe(tool) {
  return descriptions[tool.name] || tool.description
}

onMounted(async () => {
  try {
    catalog.value = await mathTools()
  } catch (ex) {
    catalogError.value = '工具名单还读不到。重启 Java 后再打开这一页。'
  }
})

function heading(run) {
  return run.group === 'math' ? '用了 math 工具组' : run.status
}

const page = {
  title: '工具组',
  note: '用 withToolGroup("math")。模型一开始只看得到入口工具，展开之后才能用下面这些运算。',
  label: '题目',
  button: '用工具组算',
  pending: '计算中',
  initial: '把 17 和 25 相加',
}
</script>

<template>
  <LineForm v-bind="page" :submit="mathSum">
    <template #extra>
      <p v-if="catalog">模型先看到：{{ catalog.visible }}</p>
      <ul class="tools" v-if="catalog">
        <li v-for="tool in listed(catalog)" :key="tool.name">
          <code>{{ tool.name }}</code>
          <span>{{ describe(tool) }}</span>
        </li>
      </ul>
      <p v-else-if="catalogError" class="sub">{{ catalogError }}</p>
    </template>
    <template #default="{ result }">
      <article class="panel stack" v-if="result">
        <h1>{{ heading(result) }}</h1>
        <template v-if="result.group">
          <pre>{{ result.text }}</pre>
          <p>工具组：{{ result.group }}</p>
          <p>{{ result.tools ? '调用了 ' + result.tools : '这次没有记下工具调用' }}</p>
        </template>
        <p v-else>{{ result.message }}</p>
        <p class="sub" v-if="result.processId">进程 {{ result.processId }}</p>
      </article>
    </template>
  </LineForm>
</template>
