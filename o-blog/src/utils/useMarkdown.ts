import MarkdownIt from 'markdown-it';
import prism from 'markdown-it-prism';
import DOMPurify from 'dompurify';

// 创建 MarkdownIt 实例并配置选项
const mdi = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
});

// 添加代码高亮插件
mdi.use(prism);

/**
 * 提供 Markdown 渲染功能的 Hook
 */
export function useMarkdown() {
  /**
   * 将 Markdown 源码渲染为安全的 HTML 字符串
   * @param src - Markdown 格式的源文本
   * @returns 经过净化的安全 HTML 字符串
   */
  function render(src: string): string {
    const dirty = mdi.render(src);
    // 对渲染结果进行安全处理，允许 target 属性（常用于链接）
    return DOMPurify.sanitize(dirty, { ADD_ATTR: ['target'] });
  }

  return { render };
}