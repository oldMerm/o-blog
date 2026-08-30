package io.github.oldmerman.web.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class LogProperties {

    @Value("${oldmerman.log.path}")
    private String path;
}
