package com.example.image_transformer;

import com.example.image_transformer.storage.LocalStorageConfiguration;
import com.example.image_transformer.webhook.WebhookProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestApplication.class)
@Import(LocalStorageConfiguration.class)
@TestPropertySource(locations = "classpath:image-transformation-test.properties")
public class ReadPropertiesTest {
    @Autowired
    private WebhookProperties webhookProperties;

    @Test
    void test() {
        assertThat(webhookProperties.webhookUrl()).isNotNull();
    }
}
