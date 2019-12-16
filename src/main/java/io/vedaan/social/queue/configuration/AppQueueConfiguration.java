package io.vedaan.social.queue.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dropwizard.validation.ValidationMethod;
import io.github.qtrouper.core.config.QueueConfiguration;
import io.github.qtrouper.core.config.RetryConfiguration;
import io.github.qtrouper.core.config.SidelineConfiguration;
import io.vedaan.social.queue.MessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.EnumUtils;

@Data
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AppQueueConfiguration {

  //TODO: Change Namespace here
  private static final String DEFAULT_NAMESPACE = "app";

  private List<QueueConfiguration> consumers = new ArrayList<>();

  private static SidelineConfiguration getDefaultConfiguration() {
    return SidelineConfiguration.builder()
        .enabled(true)
        .concurrency(0)
        .build();
  }

  private static RetryConfiguration getDefaultRetryConfiguration() {
    return RetryConfiguration.builder()
        .enabled(true)
        .ttlMs(1000)
        .maxRetries(3)
        .backOffFactor(2)
        .build();
  }

  private static QueueConfiguration getDefaultConfiguration(MessageType queueName) {
    return QueueConfiguration.builder()
        .queueName(queueName.name())
        .concurrency(3)
        .namespace(DEFAULT_NAMESPACE)
        .prefetchCount(1)
        .retry(getDefaultRetryConfiguration())
        .sideline(getDefaultConfiguration())
        .build();
  }

  @ValidationMethod(message = "Queue qconsumers are invalid")
  public boolean validate() {
    return consumers.isEmpty() ||
        consumers.stream()
            .allMatch(each -> EnumUtils.isValidEnum(MessageType.class, each.getQueueName()));
  }

  @JsonIgnore
  public QueueConfiguration getConsumerConfiguration(MessageType queueName) {
    if (consumers.isEmpty()) {
      return getDefaultConfiguration(queueName);
    }

    final Optional<QueueConfiguration> first = consumers.stream()
        .filter(each -> each.getQueueName().equalsIgnoreCase(queueName.name())).findFirst();

    return first.orElseGet(() -> getDefaultConfiguration(queueName));
  }

}

