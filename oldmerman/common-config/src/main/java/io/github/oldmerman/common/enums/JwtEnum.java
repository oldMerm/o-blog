package io.github.oldmerman.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum JwtEnum implements EnumVal{

    AUTHORIZATION("Authorization"),
    AUTH_PREFIX("Bearer ")
    ;

    private final String value;

    public static Set<String> valuesSet() {
        return Arrays.stream(values())
                .map(JwtEnum::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }
}
