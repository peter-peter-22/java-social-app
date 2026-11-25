package com.example.media.imageworker.rabbit;

import com.example.media.contract.worker.FailureClassifier;
import com.example.media.contract.worker.MediaQueue;
import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The worker's queue side: the command queue and the shared JSON message converter. The listener
 * retries only the failures the shared {@link FailureClassifier} calls recoverable; an
 * unrecoverable failure cancels the retries and the message is rejected to the dead-letter queue,
 * where the API marks the attempt failed.
 */
@Configuration
@NullMarked
public class MediaRabbitConfiguration {

    /** The command queue the API also declares; dead-lettered commands are the given-up attempts. */
    @Bean
    Queue imageTransformQueue() {
        return QueueBuilder.durable(MediaQueue.IMAGE_TRANSFORM)
                .withArgument("x-dead-letter-exchange", MediaQueue.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MediaQueue.FAILED_ROUTING_KEY)
                .build();
    }

    @Bean
    MessageConverter mediaJsonMessageConverter() {
        // The queue speaks the contract's JSON shape, the same shape the web and the registry use.
        return new JacksonJsonMessageConverter(MediaJson.create(), "com.example.media.contract.*");
    }

    @Bean
    RabbitListenerRetrySettingsCustomizer workerListenerRetry() {
        return settings -> settings.setExceptionPredicate(FailureClassifier::isRecoverable);
    }
}
