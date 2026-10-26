<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const links = [
  { to: '/reviews', label: '审稿' },
  { to: '/replies', label: '短回答' },
  { to: '/branches', label: '分支' },
  { to: '/auto', label: '自动' },
  { to: '/text', label: '纯文本' },
  { to: '/extract', label: '抽取' },
  { to: '/replan', label: '重规划' },
  { to: '/utility', label: '效用' },
  { to: '/stages', label: '监督' },
  { to: '/reflect', label: '反思' },
  { to: '/logs', label: '日志' },
  { to: '/math', label: '工具组' },
  { to: '/stamps', label: '函数工具' },
  { to: '/unfold', label: '展开' },
  { to: '/gates', label: '门禁' },
  { to: '/roles', label: '角色' },
  { to: '/fallbacks', label: '兜底' },
  { to: '/scatters', label: '分发' },
  { to: '/consensus', label: '共识' },
  { to: '/guide-lines', label: '引用' },
  { to: '/prefetches', label: '预取' },
  { to: '/filters', label: '过滤' },
  { to: '/urtexts', label: '原文' },
  { to: '/memories', label: '记忆' },
  { to: '/simple-lines', label: '简式' },
  { to: '/forms', label: '表单' },
  { to: '/subagents', label: '子代理' },
  { to: '/exports', label: '导出' },
  { to: '/echoes', label: '回声' },
  { to: '/style-guide', label: '规范' },
  { to: '/processes', label: '进程' },
  { to: '/mcp', label: 'MCP' },
]
</script>

<template>
  <div class="shell">
    <header class="top">
      <div class="brand">
        <p class="mark">dream-embabel</p>
        <p class="hint">接口在 8080，这个页面只负责调用。</p>
      </div>
      <nav class="nav" aria-label="功能">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          :aria-current="route.path === link.to ? 'page' : undefined"
        >
          {{ link.label }}
        </RouterLink>
      </nav>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>
