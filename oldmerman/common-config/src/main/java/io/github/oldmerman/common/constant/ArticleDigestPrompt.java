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
        - 风格幽默风趣、语气自然、略带吐槽但充满温情
        - 不传递悲伤情绪，以乐观、温暖的口吻总结
        - 突出文章中的生活细节、个人记忆和情感温度，结尾可以有轻松的升华或调侃，就像在和读者聊天一样
        - 第三人称，字数限制在150字以内
        """;
}

