package com.example.media.api.rabbit;

import com.example.media.json.MediaJson;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The queue topology and the message converter of the API. The commands travel in the contract's
 * JSON shape through {@link MediaJson}, the same shape the web and the registry use.
 */
@Configuration
@NullMarked
public class MediaRabbitConfiguration {

    @Bean
    DirectExchange mediaWorkerExchange() {
        return ExchangeBuilder.directExchange(MediaQueue.EXCHANGE).durable(true).build();
    }

    /** The command queue; dead-lettered commands are the attempts the worker gave up on. */
    @Bean
    Queue imageTransformQueue() {
        return QueueBuilder.durable(MediaQueue.IMAGE_TRANSFORM)
                .withArgument("x-dead-letter-exchange", MediaQueue.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", MediaQueue.FAILED_ROUTING_KEY)
                .build();
    }

    @Bean
    Binding imageTransformBinding(Queue imageTransformQueue, DirectExchange mediaWorkerExchange) {
        return BindingBuilder.bind(imageTransformQueue).to(mediaWorkerExchange).with(MediaQueue.COMMAND_ROUTING_KEY);
    }

    @Bean
    DirectExchange mediaWorkerDeadLetterExchange() {
        return ExchangeBuilder.directExchange(MediaQueue.DEAD_LETTER_EXCHANGE).durable(true).build();
    }

    @Bean
    Queue imageTransformFailedQueue() {
        return QueueBuilder.durable(MediaQueue.FAILED).build();
    }

    @Bean
    Binding imageTransformFailedBinding(
            Queue imageTransformFailedQueue, DirectExchange mediaWorkerDeadLetterExchange) {
        return BindingBuilder.bind(imageTransformFailedQueue)
                .to(mediaWorkerDeadLetterExchange)
                .with(MediaQueue.FAILED_ROUTING_KEY);
    }

    @Bean
    MessageConverter mediaJsonMessageConverter() {
        // The trusted package lets the converter deserialize the dead-lettered commands back into
        // the contract types the failed-queue listener takes.
        return new JacksonJsonMessageConverter(MediaJson.create(), "com.example.media.contract.*");
    }
}
