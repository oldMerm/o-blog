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

  const handleBlock = (block: string) => {
    if (finished.value || error.value) return;
    const evt = parseEvent(block);
    if (!evt) return;
    if (typeof evt.code === 'number' && evt.code !== 200) {
      error.value = evt.message || '摘要生成失败';
      finished.value = true;
      return;
    }
    if (evt.type === 'content' && typeof evt.chunk === 'string') {
      text.value += evt.chunk;
      return;
    }
    if (evt.type === 'end') {
      finished.value = true;
      return;
    }
    if (evt.data && typeof evt.data.content === 'string') {
      text.value = evt.data.content;
      finished.value = true;
    }
  };

  const generate = async (payload: AiSummaryPayload) => {
    if (generating.value) return;
    ctrl?.abort();
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
        if (text.value) {
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
      generating.value = false;
    }
  };

  const stop = () => {
    ctrl?.abort();
    generating.value = false;
    if (!finished.value && !error.value && text.value) {
      finished.value = true;
    }
  };

  const reset = () => {
    ctrl?.abort();
    ctrl = null;
    text.value = '';
    generating.value = false;
    finished.value = false;
    error.value = '';
  };

  onUnmounted(() => {
    ctrl?.abort();
    ctrl = null;
  });

  return { text, generating, finished, error, generate, stop, reset };
}