package com.vitalink.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {
    private boolean enabled = false;
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;

    private final S3 s3 = new S3();
    private final Ses ses = new Ses();
    private final Sns sns = new Sns();
    private final Sqs sqs = new Sqs();

    @Getter
    @Setter
    public static class S3 {
        private String bucket = "vitalink-documents";
        private int presignedUrlExpirationMinutes = 15;
    }

    @Getter
    @Setter
    public static class Ses {
        private String from = "no-reply@vitalink.com";
    }

    @Getter
    @Setter
    public static class Sns {
        private String appointmentTopicArn;
    }

    @Getter
    @Setter
    public static class Sqs {
        private boolean consumerEnabled = false;
        private String appointmentQueueUrl;
        private int maxMessages = 10;
        private int waitTimeSeconds = 10;
    }
}
