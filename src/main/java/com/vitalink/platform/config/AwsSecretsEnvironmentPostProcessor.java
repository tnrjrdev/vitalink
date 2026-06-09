package com.vitalink.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Carrega segredos da AWS (Secrets Manager e/ou SSM Parameter Store) e os injeta
 * como uma PropertySource de alta prioridade ANTES da inicializacao do contexto.
 *
 * Assim, placeholders como ${APP_JWT_SECRET} e ${SPRING_DATASOURCE_PASSWORD}
 * resolvem a partir do cofre da AWS em producao, sem nada sensivel versionado.
 *
 * Por rodar muito cedo (sem logger Spring / sem beans), e estritamente "gated"
 * por variaveis de ambiente e totalmente defensivo: qualquer falha apenas registra
 * no stderr e deixa a aplicacao seguir com os defaults locais.
 */
public class AwsSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {
    private static final String PROPERTY_SOURCE_NAME = "aws-secrets";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> resolved = new HashMap<>();
        String region = firstNonBlank(environment.getProperty("APP_AWS_REGION"), "us-east-1");

        if (isEnabled(environment, "APP_AWS_SECRETS_ENABLED")) {
            loadFromSecretsManager(environment, region, resolved);
        }
        if (isEnabled(environment, "APP_AWS_SSM_ENABLED")) {
            loadFromParameterStore(environment, region, resolved);
        }

        if (!resolved.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, resolved));
            System.out.println("[AWS] " + resolved.size() + " segredo(s)/parametro(s) carregado(s) da AWS.");
        }
    }

    private void loadFromSecretsManager(ConfigurableEnvironment env, String region, Map<String, Object> target) {
        String secretName = env.getProperty("APP_AWS_SECRET_NAME");
        if (!StringUtils.hasText(secretName)) {
            return;
        }
        try (SecretsManagerClient client = SecretsManagerClient.builder().region(Region.of(region)).build()) {
            GetSecretValueResponse response = client.getSecretValue(r -> r.secretId(secretName));
            String payload = response.secretString();
            if (StringUtils.hasText(payload)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> entries = objectMapper.readValue(payload, Map.class);
                target.putAll(entries);
            }
        } catch (Exception ex) {
            System.err.println("[AWS] Falha ao carregar do Secrets Manager (" + secretName
                    + "): " + ex.getMessage() + ". Seguindo com defaults locais.");
        }
    }

    private void loadFromParameterStore(ConfigurableEnvironment env, String region, Map<String, Object> target) {
        String path = env.getProperty("APP_AWS_SSM_PARAMETER_PATH");
        if (!StringUtils.hasText(path)) {
            return;
        }
        try (SsmClient client = SsmClient.builder().region(Region.of(region)).build()) {
            String nextToken = null;
            do {
                GetParametersByPathResponse response = client.getParametersByPath(
                        GetParametersByPathRequest.builder()
                                .path(path)
                                .recursive(true)
                                .withDecryption(true)
                                .nextToken(nextToken)
                                .build());
                for (Parameter parameter : response.parameters()) {
                    target.put(stripPath(parameter.name(), path), parameter.value());
                }
                nextToken = response.nextToken();
            } while (nextToken != null);
        } catch (Exception ex) {
            System.err.println("[AWS] Falha ao carregar do SSM Parameter Store (" + path
                    + "): " + ex.getMessage() + ". Seguindo com defaults locais.");
        }
    }

    private String stripPath(String name, String path) {
        String normalized = path.endsWith("/") ? path : path + "/";
        return name.startsWith(normalized) ? name.substring(normalized.length()) : name;
    }

    private boolean isEnabled(ConfigurableEnvironment environment, String key) {
        return Boolean.parseBoolean(environment.getProperty(key, "false"));
    }

    private String firstNonBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }
}
