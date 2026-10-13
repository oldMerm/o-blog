package io.github.oldmerman.web.config;

import io.github.oldmerman.core.DeepseekClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class DeepseekConfig {

    @Value("${app.oldmerman-api-key}")
    private String key;

    @Bean
    public DeepseekClient deepseekClient(){
        return new DeepseekClient(key);
    }
}
