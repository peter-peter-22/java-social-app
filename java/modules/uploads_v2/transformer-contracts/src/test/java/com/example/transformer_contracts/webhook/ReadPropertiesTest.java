package com.example.transformer_contracts.webhook;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ReadPropertiesTest.Configuration.class)
@TestPropertySource(locations = "classpath:image-transformation-test.properties")
public class ReadPropertiesTest {
    @Autowired
    private WebhookProperties webhookProperties;

    @Test
    void test() {
        assertThat(webhookProperties.webhookUrl()).isNotNull();
    }

    /*
    Simply using @SpringBootTest(classes=WebhookProperties.class) doesn't work because
    it only scans the bean without enabling configuration reading.
    */
    @SpringBootConfiguration
    @EnableConfigurationProperties(WebhookProperties.class)
    static class Configuration {
    }
}
