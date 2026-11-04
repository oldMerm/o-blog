import { onUnmounted, ref } from 'vue'
import { httpInstance } from '@/utils/http'
import type { AgentChatStatus, AgentTurn, AgentTurnStatus } from './types'

interface SseEvent {
  event: string
  data: string
}

const decodeEscapedNewlines = (value: string) =>
  value.replace(/\\r/g, '\r').replace(/\\n/g, '\n')

const parseSseBlock = (block: string): SseEvent | null => {
  let event = 'message'
  const dataLines: string[] = []

  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      // 注意：后端使用 Spring 的 SseEmitter，写出格式是 "data:" + 内容，
      // 冒号后并没有分隔空格。这里如果按 SSE 规范移除一个前导空格，
      // 会把正文/思考内容自带的空格（如 " are"）一并吃掉，
      // 导致 "you are handsome" 变成 "youarehandsome"。
      dataLines.push(line.slice(5))
    }
  }

  if (dataLines.length === 0 && event === 'message') return null
  return { event, data: decodeEscapedNewlines(dataLines.join('\n')) }
}

const extractErrorMessage = (raw: unknown, allowPlainText = true): string => {
  if (typeof raw === 'string') {
    const text = raw.trim()
    if (!text) return ''
    if (!text.startsWith('{') && !text.startsWith('[')) {
      return allowPlainText ? text : ''
    }
    try {
      return extractErrorMessage(JSON.parse(text), allowPlainText)
    } catch {
      return allowPlainText ? text : ''
    }
  }

  if (raw && typeof raw === 'object') {
    const obj = raw as Record<string, unknown>
    const message = obj.message ?? obj.msg ?? obj.error
    if (typeof message === 'string' && message.trim()) return message
    if (obj.code === 401 || obj.code === '401') return '登录状态已失效，请重新登录'
    if (typeof obj.code === 'number') return `请求失败（${obj.code}）`
  }

  return ''
}

export function useAgentChatStream() {
  const turns = ref<AgentTurn[]>([])
  const threadId = ref<string | null>(null)
  const streaming = ref(false)
  const status = ref<AgentChatStatus>('idle')
  const error = ref('')

  let controller: AbortController | null = null
  let activeTurn: AgentTurn | null = null
  let abortedByUser = false
  let requestSeq = 0

  const createTurn = (question: string): AgentTurn => ({
    id: `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`,
    question,
    content: '',
    reasoning: '',
    tools: [],
    status: 'pending',
    error: '',
    expanded: true,
    finishReason: '',
  })

  const finishTurn = (
    turn: AgentTurn,
    nextStatus: Exclude<AgentTurnStatus, 'pending'>,
    message = ''
  ) => {
    turn.status = nextStatus
    turn.error = message
    if (activeTurn === turn) {
      activeTurn = null
      streaming.value = false
      status.value = nextStatus
      error.value = message
    }
  }

  const handleEvent = (turn: AgentTurn, event: SseEvent) => {
    switch (event.event) {
      case 'content':
        if (!event.data) return
        turn.content += event.data
        if (turn.status !== 'aborted') {
          turn.status = 'streaming'
          status.value = 'streaming'
        }
        break
      case 'reasoning':
        if (!event.data) return
        if (!turn.reasoning) turn.expanded = true
        turn.reasoning += event.data
        if (turn.status !== 'aborted') {
          turn.status = 'streaming'
          status.value = 'streaming'
        }
        break
      case 'tool':
        if (!event.data) return
        if (!turn.tools.includes(event.data)) turn.tools.push(event.data)
        turn.expanded = true
        if (turn.status !== 'aborted') {
          turn.status = 'streaming'
          status.value = 'streaming'
        }
        break
      case 'thread':
        if (event.data.trim()) threadId.value = event.data.trim()
        break
      case 'end':
        turn.finishReason = event.data
        if (event.data === 'ABORTED') {
          turn.status = 'aborted'
          status.value = 'aborted'
        }
        break
      case 'done':
        finishTurn(turn, 'done')
        break
      case 'error':
        finishTurn(turn, 'error', event.data || '生成失败，请稍后重试')
        break
      default:
        if (event.data === '[DONE]') finishTurn(turn, 'done')
        break
    }
  }

  const send = async (question: string) => {
    const text = question.trim()
    if (!text || streaming.value) return

    abortedByUser = false
    error.value = ''

    const turn = createTurn(text)
    turns.value.push(turn)
    activeTurn = turns.value[turns.value.length - 1] ?? turn
    streaming.value = true
    status.value = 'connecting'

    const abortController = new AbortController()
    controller = abortController
    const seq = ++requestSeq

    let rawText = ''
    let cursor = 0
    let doneReceived = false
    let sawSse = false

    const consume = (raw: string) => {
      if (seq !== requestSeq) return
      rawText = raw
      const normalized = raw.replace(/\r\n/g, '\n')
      let boundary = normalized.indexOf('\n\n', cursor)
      while (boundary !== -1) {
        const block = normalized.slice(cursor, boundary)
        cursor = boundary + 2
        if (block.trim()) {
          sawSse = true
          const event = parseSseBlock(block)
          if (event) {
            handleEvent(activeTurn ?? turn, event)
            if (event.event === 'done' || event.event === 'error') doneReceived = true
          }
        }
        boundary = normalized.indexOf('\n\n', cursor)
      }
    }

    try {
      const res: any = await httpInstance.post(
        '/chat/completion/v1',
        { threadId: threadId.value, question: text },
        {
          responseType: 'text',
          timeout: 0,
          signal: abortController.signal,
          onDownloadProgress: (e: any) => {
            const raw = e?.event?.target?.responseText
            if (typeof raw === 'string') consume(raw)
          },
        }
      )

      if (seq !== requestSeq) return

      if (!doneReceived) {
        if (typeof res === 'string' && res) consume(res)
        if (seq !== requestSeq) return

        if (!doneReceived && activeTurn) {
          const message = extractErrorMessage(rawText || res, !sawSse)
          if (message) {
            finishTurn(activeTurn, 'error', message)
            doneReceived = true
          }
        }

        if (!doneReceived && activeTurn) {
          if (activeTurn.finishReason === 'ABORTED') {
            finishTurn(activeTurn, 'aborted')
          } else if (activeTurn.content || activeTurn.reasoning || activeTurn.tools.length > 0) {
            finishTurn(activeTurn, 'done')
          } else {
            finishTurn(activeTurn, 'error', '连接已结束，但没有收到回复')
          }
          doneReceived = true
        }
      }
    } catch (err: any) {
      if (seq !== requestSeq) return

      const canceled =
        err?.code === 'ERR_CANCELED' ||
        err?.name === 'CanceledError' ||
        err?.name === 'AbortError' ||
        err?.message === 'canceled'

      if (canceled) {
        if (!abortedByUser && activeTurn) finishTurn(activeTurn, 'aborted')
      } else if (activeTurn) {
        const statusCode = err?.response?.status
        const message =
          extractErrorMessage(err?.response?.data) ||
          (statusCode === 401 ? '登录状态已失效，请重新登录' : '') ||
          err?.message ||
          '请求失败，请稍后重试'
        finishTurn(activeTurn, 'error', message)
      }
    } finally {
      if (seq === requestSeq) {
        controller = null
        if (activeTurn) {
          finishTurn(activeTurn, activeTurn.finishReason === 'ABORTED' ? 'aborted' : 'done')
        }
      }
    }
  }

  const abort = () => {
    if (!streaming.value) return
    abortedByUser = true
    controller?.abort()
    if (activeTurn) finishTurn(activeTurn, 'aborted')
    streaming.value = false
    status.value = 'aborted'
  }

  const reset = () => {
    requestSeq += 1
    if (streaming.value) {
      abortedByUser = true
      controller?.abort()
    }
    controller = null
    activeTurn = null
    turns.value = []
    threadId.value = null
    streaming.value = false
    status.value = 'idle'
    error.value = ''
    abortedByUser = false
  }

  onUnmounted(() => {
    requestSeq += 1
    abortedByUser = true
    controller?.abort()
    controller = null
  })

  return { turns, threadId, streaming, status, error, send, abort, reset }
}
