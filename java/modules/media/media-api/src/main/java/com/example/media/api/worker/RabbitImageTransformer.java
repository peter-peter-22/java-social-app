package com.example.media.api.worker;

import com.example.media.contract.port.ImageTransformer;
import com.example.media.contract.worker.ImageTransformCommand;
import com.example.media.contract.worker.ImageTransformResult;
import org.jspecify.annotations.NullMarked;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * The async {@link ImageTransformer} port over RabbitMQ: it publishes the command to the worker
 * queue in the contract's JSON shape and returns immediately. The worker runs the same
 * transformation as the blocking call and calls the API completion endpoint with the result.
 */
@NullMarked
public final class RabbitImageTransformer implements ImageTransformer {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public RabbitImageTransformer(RabbitTemplate rabbitTemplate, String exchange, String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void transformAsync(ImageTransformCommand command) {
        rabbitTemplate.convertAndSend(exchange, routingKey, command);
    }

    @Override
    public ImageTransformResult transform(ImageTransformCommand command) {
        throw new UnsupportedOperationException("the async adapter only publishes commands");
    }
}
