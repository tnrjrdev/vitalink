package com.vitalink.platform.integration;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class DomainEvent {
    private final String type;

    @Singular
    private final Map<String, Object> attributes;

    public static DomainEvent of(String type, Map<String, Object> attributes) {
        return DomainEvent.builder().type(type).attributes(attributes).build();
    }
}
