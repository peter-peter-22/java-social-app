package transformations;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "transformations.blocking")
class BlockingTransformationProperties {
    @NotNull URI endpoint;
}
