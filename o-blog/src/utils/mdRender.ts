import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css'; 

export class MarkdownRenderer {
  private md: MarkdownIt;

  constructor() {
    this.md = new MarkdownIt({
      html: true,        // 允许解析 HTML 标签
      linkify: true,     // 自动将 URL 识别为链接
      typographer: true, // 启用引号美化等排版替换
    });

    this.md.set({
      highlight: (str: string, lang: string): string => {
        // 如果指定了语言，且 highlight.js 支持该语言
        if (lang && hljs.getLanguage(lang)) {
          try {
            const highlightedCode = hljs.highlight(str, {
              language: lang,
              ignoreIllegals: true,
            }).value;
            return `<pre><code class="hljs">${highlightedCode}</code></pre>`;
          } catch (error) {
            console.warn('代码高亮解析失败:', error);
          }
        }
        // 回退方案：未指定语言或解析失败时，进行安全的 HTML 转义
        return `<pre><code class="hljs">${this.md.utils.escapeHtml(str)}</code></pre>`;
      },
    });
  }

  /**
   * 渲染 Markdown 字符串为 HTML
   * @param rawMarkdown 原始 Markdown 文本
   * @returns 渲染后的 HTML 字符串
   */
  public render(rawMarkdown: string): string {
    if (!rawMarkdown) return '';
    return this.md.render(rawMarkdown);
  }

  /**
   * 提供挂载插件的接口
   * @param plugin markdown-it 插件
   * @param params 插件参数
   */
  public usePlugin(plugin: any, ...params: any[]): this {
    this.md.use(plugin, ...params);
    return this; // 支持链式调用
  }
}

// ================= 使用示例 =================

const renderer = new MarkdownRenderer();

const markdownStr = `
### TypeScript 高亮测试

下面是一段 TS 代码：

\`\`\`typescript
interface User {
  name: string;
  age: number;
}

const greet = (user: User): string => {
  return \`Hello, \${user.name}!\`;
};
\`\`\`
`;

const htmlResult = renderer.render(markdownStr);
console.log(htmlResult);