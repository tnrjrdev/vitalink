package com.vitalink.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@EnableScheduling
@EnableConfigurationProperties(AwsProperties.class)
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "true")
public class AwsClientConfig {
    private final AwsProperties properties;

    public AwsClientConfig(AwsProperties properties) {
        this.properties = properties;
        log.info("Integracao AWS HABILITADA (region={}). Clientes S3/SES/SNS/SQS serao criados.",
                properties.getRegion());
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (StringUtils.hasText(properties.getAccessKey()) && StringUtils.hasText(properties.getSecretKey())) {
            log.info("Usando credenciais AWS estaticas fornecidas via configuracao.");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey()));
        }
        log.info("Usando DefaultCredentialsProvider (variaveis de ambiente / perfil / IAM role).");
        return DefaultCredentialsProvider.create();
    }

    private Region region() {
        return Region.of(properties.getRegion());
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentialsProvider) {
        return S3Client.builder()
                .region(region())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentialsProvider) {
        return S3Presigner.builder()
                .region(region())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public SesV2Client sesV2Client(AwsCredentialsProvider credentialsProvider) {
        return SesV2Client.builder()
                .region(region())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public SnsClient snsClient(AwsCredentialsProvider credentialsProvider) {
        return SnsClient.builder()
                .region(region())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    public SqsClient sqsClient(AwsCredentialsProvider credentialsProvider) {
        return SqsClient.builder()
                .region(region())
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
