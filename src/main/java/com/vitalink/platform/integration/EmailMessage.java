package com.vitalink.platform.integration;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class EmailMessage {
    private final String to;
    private final String subject;
    private final String body;

    @Builder.Default
    private final boolean html = false;
}
