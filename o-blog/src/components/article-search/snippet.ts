export interface SnippetPart {
    text: string;
    match: boolean;
}

/**
 * 截取包含关键词的摘要片段，并把关键词拆成可高亮的片段。
 *
 * 规则与设计稿保持一致：
 * - 从命中关键词所在句子的开头开始展示；
 * - 片段过长时保留完整关键词，并在后方追加省略号；
 * - 当前字段未命中时，只截取固定长度的开头；
 * - 返回纯文本片段，由模板渲染 <mark>，避免直接拼接 HTML。
 */
export function buildSnippetParts(
    text: string | null | undefined,
    keyword: string,
    maxLen = 75
): SnippetPart[] {
    const raw = String(text ?? '');
    if (!raw) return [];

    const query = keyword.trim();
    if (!query) {
        return [
            {
                text: raw.length > maxLen ? `${raw.slice(0, maxLen)}...` : raw,
                match: false
            }
        ];
    }

    const lowerRaw = raw.toLowerCase();
    const lowerQuery = query.toLowerCase();
    const matchIndex = lowerRaw.indexOf(lowerQuery);

    let snippet: string;

    if (matchIndex === -1) {
        // 当前字段没有命中（例如命中在标题，这里处理摘要）
        snippet = raw.length > maxLen ? `${raw.slice(0, maxLen)}...` : raw;
    } else {
        // 向前找到所在句子的开头
        const sentenceDelimiters = /[。！？.!?\n；;]/;
        let sentenceStart = 0;
        for (let i = matchIndex - 1; i >= 0; i--) {
            if (sentenceDelimiters.test(raw.charAt(i))) {
                sentenceStart = i + 1;
                break;
            }
        }

        // 去掉句首空白
        while (sentenceStart < matchIndex && /\s/.test(raw.charAt(sentenceStart))) {
            sentenceStart++;
        }

        const fromSentence = raw.slice(sentenceStart);
        if (fromSentence.length > maxLen) {
            // 保证至少能看到完整关键词
            const minEnd = Math.max(maxLen, matchIndex - sentenceStart + query.length + 10);
            snippet = `${fromSentence.slice(0, minEnd)}...`;
        } else {
            snippet = fromSentence;
        }
    }

    return splitByKeyword(snippet, query);
}

function splitByKeyword(text: string, keyword: string): SnippetPart[] {
    const query = keyword.trim();
    if (!query) return [{ text, match: false }];

    const regex = new RegExp(escapeRegExp(query), 'gi');
    const parts: SnippetPart[] = [];
    let lastIndex = 0;
    let match: RegExpExecArray | null;

    while ((match = regex.exec(text)) !== null) {
        if (match[0].length === 0) {
            regex.lastIndex++;
            continue;
        }
        if (match.index > lastIndex) {
            parts.push({ text: text.slice(lastIndex, match.index), match: false });
        }
        parts.push({ text: match[0], match: true });
        lastIndex = match.index + match[0].length;
    }

    if (lastIndex < text.length) {
        parts.push({ text: text.slice(lastIndex), match: false });
    }

    return parts.length ? parts : [{ text, match: false }];
}

function escapeRegExp(value: string): string {
    return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
