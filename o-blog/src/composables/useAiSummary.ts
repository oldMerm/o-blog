import { ref, onUnmounted } from 'vue';
import { httpInstance } from '@/utils/http';

interface SseBlock {
  event: string;
  data: string;
}

const parseSseBlock = (block: string): SseBlock | null => {
  let event = 'message';
  const dataLines: string[] = [];
  for (const line of block.split('\n')) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim();
    } else if (line.startsWith('data:')) {
      let value = line.slice(5);
      if (value.startsWith(' ')) value = value.slice(1);
      dataLines.push(value);
    }
  }
  if (dataLines.length === 0 && event === 'message') return null;
  return { event, data: dataLines.join('\n') };
};

export function useAiSummary() {
  const text = ref('');
  const generating = ref(false);
  const finished = ref(false);
  const error = ref('');

  let ctrl: AbortController | null = null;
  let drainTimer: ReturnType<typeof setInterval> | null = null;
  let pending = '';
  let doneReceived = false;

  const TICK_MS = 16;

  const clearDrainTimer = () => {
    if (drainTimer !== null) {
      clearInterval(drainTimer);
      drainTimer = null;
    }
  };

  // 统一「打字机」队列：流式分片与缓存全文都经此按稳定速度渲染
  const drainTick = () => {
    if (error.value) {
      clearDrainTimer();
      return;
    }
    if (pending.length > 0) {
      const step = Math.max(1, Math.ceil(pending.length / 12));
      text.value += pending.slice(0, step);
      pending = pending.slice(step);
    }
    if (pending.length === 0 && doneReceived) {
      clearDrainTimer();
      finished.value = true;
      generating.value = false;
    }
  };

  const ensureDrainTimer = () => {
    if (drainTimer === null) {
      drainTimer = setInterval(drainTick, TICK_MS);
    }
  };

  const handleBlock = (block: string) => {
    if (finished.value || error.value) return;
    const evt = parseSseBlock(block);
    if (!evt) return;
    if (evt.event === 'content') {
      if (evt.data) pending += evt.data;
      ensureDrainTimer();
      return;
    }
    if (evt.event === 'done' || evt.data === '[DONE]') {
      doneReceived = true;
      if (pending.length === 0) {
        clearDrainTimer();
        finished.value = true;
        generating.value = false;
      }
      return;
    }
  };

  const generate = async (articleId: string) => {
    if (generating.value) return;
    ctrl?.abort();
    clearDrainTimer();
    ctrl = new AbortController();
    text.value = '';
    error.value = '';
    pending = '';
    doneReceived = false;
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
      const res: any = await httpInstance.post(
        `/chat/completion/${articleId}/digest`,
        null,
        {
          responseType: 'text',
          timeout: 0,
          signal: ctrl.signal,
          onDownloadProgress: (e: any) => {
            const raw = e?.event?.target?.responseText;
            if (typeof raw === 'string') consume(raw);
          },
        }
      );
      if (!finished.value && !error.value && typeof res === 'string') {
        consume(res);
      }
      if (!finished.value && !error.value) {
        if (!doneReceived && (pending.length > 0 || text.value)) {
          // 响应结束但未收到显式 done，视为已完成并排空剩余内容
          doneReceived = true;
          ensureDrainTimer();
        } else if (!doneReceived) {
          error.value = '摘要生成失败，请稍后重试';
          generating.value = false;
        }
      }
    } catch (err: any) {
      const canceled = err?.code === 'ERR_CANCELED' || err?.message === 'canceled';
      if (!canceled) {
        error.value = err?.message || '摘要生成失败';
      }
      clearDrainTimer();
      generating.value = false;
    }
  };

  const stop = () => {
    ctrl?.abort();
    clearDrainTimer();
    generating.value = false;
    if (!finished.value && !error.value && text.value) {
      finished.value = true;
    }
  };

  const reset = () => {
    ctrl?.abort();
    clearDrainTimer();
    ctrl = null;
    pending = '';
    doneReceived = false;
    text.value = '';
    generating.value = false;
    finished.value = false;
    error.value = '';
  };

  onUnmounted(() => {
    ctrl?.abort();
    clearDrainTimer();
    ctrl = null;
  });

  return { text, generating, finished, error, generate, stop, reset };
}
