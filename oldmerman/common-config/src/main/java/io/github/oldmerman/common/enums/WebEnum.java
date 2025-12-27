package io.github.oldmerman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum WebEnum implements EnumVal{

    AUTHORIZATION("Authorization"),
    BASE64_IMAGE_PREFIX("data:image/png;base64,"),
    AUTH_PREFIX("Bearer "),

    USER_PREFIX("usr")
    ;

    private final String value;

    public static Set<String> valuesSet() {
        return Arrays.stream(values())
                .map(WebEnum::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }
}
