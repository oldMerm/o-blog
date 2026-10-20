package io.github.oldmerman.web.config;

import io.github.oldmerman.core.DeepSeekClient;
import io.github.oldmerman.core.api.chat.ChatClient;
import io.github.oldmerman.core.api.search.SearchClient;
import io.github.oldmerman.core.config.DeepSeekConfig;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DeepseekConfig {

    @Value("${app.oldmerman-api-key}")
    private String key;

    private DeepSeekConfig config;

    @PostConstruct
    public void init(){
        this.config = new DeepSeekConfig(key);
    }

    @Bean
    public ChatClient deepseekClient(){
        return DeepSeekClient.chat(config);
    }

    @Bean
    public SearchClient searchClient(){
        return DeepSeekClient.search(config);
    }
}
