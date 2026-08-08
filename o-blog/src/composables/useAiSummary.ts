import { ref, onUnmounted } from 'vue';
import { httpInstance } from '@/utils/http';

export interface AiSummaryPayload {
  articleId: string;
  articleName: string;
  content: string;
}

interface SseEvent {
  chunk?: string;
  call_id?: number;
  type?: string;
  code?: number;
  message?: string;
  data?: { id?: string; content?: string };
}

const parseEvent = (block: string): SseEvent | null => {
  const dataLines: string[] = [];
  block.split('\n').forEach((line) => {
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart());
    }
  });
  if (dataLines.length === 0) return null;
  try {
    return JSON.parse(dataLines.join('\n'));
  } catch {
    return null;
  }
};

export function useAiSummary() {
  const text = ref('');
  const generating = ref(false);
  const finished = ref(false);
  const error = ref('');
  let ctrl: AbortController | null = null;
  let streamTimer: ReturnType<typeof setInterval> | null = null;
  let streamLen = 0;

  const clearStreamTimer = () => {
    if (streamTimer !== null) {
      clearInterval(streamTimer);
      streamTimer = null;
    }
  };

  // 缓存命中时一次性返回全文，用“打字机”效果模拟流式展示
  const simulateStream = (content: string) => {
    clearStreamTimer();
    text.value = '';
    streamLen = content.length;
    if (streamLen === 0) {
      finished.value = true;
      return;
    }
    generating.value = true;
    finished.value = false;
    let idx = 0;
    const totalTicks = Math.min(Math.max(Math.round(streamLen / 5), 50), 120);
    const step = Math.max(1, Math.ceil(streamLen / totalTicks));
    streamTimer = setInterval(() => {
      if (error.value) {
        clearStreamTimer();
        return;
      }
      idx = Math.min(idx + step, streamLen);
      text.value = content.slice(0, idx);
      if (idx >= streamLen) {
        clearStreamTimer();
        finished.value = true;
        generating.value = false;
      }
    }, 35);
  };

  const handleBlock = (block: string) => {
    if (finished.value || error.value) return;
    const evt = parseEvent(block);
    if (!evt) return;
    if (typeof evt.code === 'number' && evt.code !== 200) {
      clearStreamTimer();
      error.value = evt.message || '摘要生成失败';
      finished.value = true;
      return;
    }
    if (evt.type === 'content' && typeof evt.chunk === 'string') {
      if (streamTimer !== null) return;
      text.value += evt.chunk;
      return;
    }
    if (evt.type === 'end') {
      if (streamTimer !== null) return;
      finished.value = true;
      return;
    }
    if (evt.data && typeof evt.data.content === 'string') {
      simulateStream(evt.data.content);
    }
  };

  const generate = async (payload: AiSummaryPayload) => {
    if (generating.value) return;
    ctrl?.abort();
    clearStreamTimer();
    ctrl = new AbortController();
    text.value = '';
    error.value = '';
    generating.value = true;
    finished.value = false;

    let buffer = '';
    let idx = 0;

    const consume = (raw: string) => {
      buffer = raw;
      let boundary = buffer.indexOf('\n\n', idx);
      while (boundary !== -1) {
        handleBlock(buffer.slice(idx, boundary));
        idx = boundary + 2;
        boundary = buffer.indexOf('\n\n', idx);
      }
    };

    try {
      const res: any = await httpInstance.post('/knowledge-agent', payload, {
        responseType: 'text',
        timeout: 0,
        signal: ctrl.signal,
        onDownloadProgress: (e: any) => {
          const raw = e?.event?.target?.responseText;
          if (typeof raw === 'string') consume(raw);
        },
      });
      if (!finished.value && !error.value && typeof res === 'string') {
        consume(res);
      }
      if (!finished.value && !error.value) {
        if (streamTimer !== null) {
          // 缓存命中，模拟流式渲染进行中，等待 simulateStream 收尾
        } else if (text.value) {
          finished.value = true;
        } else {
          error.value = '摘要生成失败，请稍后重试';
        }
      }
    } catch (err: any) {
      const canceled = err?.code === 'ERR_CANCELED' || err?.message === 'canceled';
      if (!canceled) {
        error.value = err?.message || '摘要生成失败';
      }
    } finally {
      if (streamTimer === null) {
        generating.value = false;
      }
    }
  };

  const stop = () => {
    ctrl?.abort();
    clearStreamTimer();
    generating.value = false;
    if (!finished.value && !error.value && text.value) {
      finished.value = true;
    }
  };

  const reset = () => {
    ctrl?.abort();
    clearStreamTimer();
    ctrl = null;
    text.value = '';
    generating.value = false;
    finished.value = false;
    error.value = '';
  };

  onUnmounted(() => {
    ctrl?.abort();
    clearStreamTimer();
    ctrl = null;
  });

  return { text, generating, finished, error, generate, stop, reset };
}