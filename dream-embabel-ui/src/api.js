async function fail(res) {
  let detail = ''
  try {
    const body = await res.json()
    detail = body.message || body.text || body.error || ''
  } catch {
    detail = ''
  }
  const error = new Error(detail || `${res.status} ${res.statusText}`)
  error.status = res.status
  throw error
}

async function send(path, options) {
  const res = await fetch(path, options)
  if (!res.ok) {
    await fail(res)
  }
  const text = await res.text()
  return text ? JSON.parse(text) : null
}

function post(path, body) {
  return send(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export function startReview(topic) {
  return post('/api/reviews/runs', { topic })
}

export function confirmReview(runId) {
  return post('/api/reviews/runs/' + encodeURIComponent(runId) + '/confirmations', {})
}

export function reviewRun(runId) {
  return send('/api/reviews/runs/' + encodeURIComponent(runId))
}

export function reply(message) {
  return post('/api/replies', { message })
}

export function branch(text) {
  return post('/api/branches', { text })
}

export function autoReply(message) {
  return post('/api/auto-replies', { message })
}

export function textLine(message) {
  return post('/api/text-lines', { message })
}

export function maybeNote(message) {
  return post('/api/maybe-notes', { message })
}

export function replan(text) {
  return post('/api/replans', { text })
}

export function utilityChoice(text) {
  return post('/api/utilities', { text })
}

export function stageRun(text) {
  return post('/api/stages', { text })
}

export function reflect(text) {
  return post('/api/reflections', { text })
}

export function namedLine(text) {
  return post('/api/named-lines', { text })
}

export function mathSum(text) {
  return post('/api/math-sums', { text })
}

export function mathTools() {
  return send('/api/math-sums/tools')
}

export function stamp(text) {
  return post('/api/stamps', { text })
}

export function unfold(text) {
  return post('/api/unfolds', { text })
}

export function gate(text) {
  return post('/api/gates', { text })
}

export function roleLine(text) {
  return post('/api/roles', { text })
}

export function fallbackLine(text) {
  return post('/api/fallbacks', { text })
}

export function scatter(text) {
  return post('/api/scatters', { text })
}

export function consensus(text) {
  return post('/api/consensus', { text })
}

export function guideTools() {
  return send('/api/guide-lines/tools')
}

export function guideLine(text) {
  return post('/api/guide-lines', { text })
}

export function prefetch(text) {
  return post('/api/prefetches', { text })
}

export function filterLine(text) {
  return post('/api/filters', { text })
}

export function urtextLine(text) {
  return post('/api/urtexts', { text })
}

export function memoryLine(text) {
  return post('/api/memories', { text })
}

export function simpleLine(text) {
  return post('/api/simple-lines', { text })
}

export function formStart() {
  return post('/api/forms', {})
}

export function formSubmit(processId, column, point) {
  return post('/api/forms/submit', { processId, column, point })
}

export function subagentLine(text) {
  return post('/api/subagents', { text })
}

export function exportLine(text) {
  return post('/api/exports', { text })
}

export function echoLine(text) {
  return post('/api/echoes', { text })
}

export function styleGuide() {
  return send('/api/style-guide')
}

export function searchStyleGuide(query) {
  return post('/api/style-guide/searches', { query })
}

export function processes() {
  return send('/api/processes')
}

export function mcpStatus() {
  return send('/api/mcp')
}

export function mcpTools() {
  return post('/api/mcp/tools', {})
}
