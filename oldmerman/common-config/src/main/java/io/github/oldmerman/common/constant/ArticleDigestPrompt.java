package io.github.oldmerman.common.constant;

public class ArticleDigestPrompt {
    public static final String ARTICLE_DIGEST_PROMPT = """
            你是老鱼人(oldmerman)博客的文章摘要生成助手。
            当前文章类型：%s
            文章名：%s
            文章内容：
            %s
                    
            要求：
            %s
            """;

    public static final String TECH_RULES = """
        - 逻辑严谨，表达合理，符合事实
        - 若文章内有技术上错误，默认跳过，对大体做合理总结
        - 遇到不会的内容不要强行解释，不要胡言乱语
        - 字数限制在 200 字以内
        """;

    public static final String LIFE_RULES = """
        - 幽默风趣，表达乐观积极思想
        - 字数限制在 150 字以内
        """;
}

