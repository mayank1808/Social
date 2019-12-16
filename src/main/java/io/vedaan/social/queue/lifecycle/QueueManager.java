package io.vedaan.social.queue.lifecycle;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import io.github.qtrouper.Trouper;
import io.github.qtrouper.core.config.QueueConfiguration;
import io.github.qtrouper.core.models.QAccessInfo;
import io.github.qtrouper.core.models.QueueContext;
import io.github.qtrouper.core.rabbit.RabbitConnection;
import io.vedaan.social.queue.MessageType;
import io.vedaan.social.queue.QueueConsumer;
import io.vedaan.social.queue.annotation.MessageConsumer;
import io.vedaan.social.queue.configuration.AppQueueConfiguration;
import io.vedaan.social.queue.util.QueueUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;

@Slf4j
@Singleton
public class QueueManager implements Managed {

  //TODO: Change the package here if you play with package name
  private final static String HANDLER_PACKAGE = "io.github.sample";
  private static Map<MessageType, QueueActor> actors = new HashMap<>();
  private final RabbitConnection rabbitConnection;

  private final AppQueueConfiguration appQueueConfiguration;
  private Map<MessageType, QueueConsumer> qProcessors = Maps.newConcurrentMap();


  @Inject
  public QueueManager(Injector injector, RabbitConnection rabbitConnection,
      AppQueueConfiguration configuration) {
    this.rabbitConnection = rabbitConnection;
    this.appQueueConfiguration = configuration;

    Reflections reflections = new Reflections(HANDLER_PACKAGE);
    final Set<Class<?>> annotatedClasses = reflections
        .getTypesAnnotatedWith(MessageConsumer.class);

    annotatedClasses.forEach(annotatedType -> {
      if (QueueConsumer.class.isAssignableFrom(annotatedType)) {
        MessageConsumer annotation = annotatedType.getAnnotation(MessageConsumer.class);
        final QueueConsumer instance = QueueConsumer.class
            .cast(injector.getInstance(annotatedType));
        qProcessors.put(annotation.messageType(), instance);
      }
    });
  }

  private static QueueActor getActor(MessageType messageType) {
    if (!actors.containsKey(messageType)) {
      throw new RuntimeException(
          "Can't find a queue actor with the queuename : " + messageType);
    }

    return actors.get(messageType);
  }

  private QueueConsumer getProcessor(MessageType messageType) {
    if (!qProcessors.containsKey(messageType)) {
      throw new RuntimeException(
          "Can't find a queue processor with the queuename : " + messageType);
    }

    return qProcessors.get(messageType);
  }

  @Override
  public void start() throws Exception {
    log.info("Starting the app Q registrar");

    for (MessageType messageType : MessageType.values()) {

      QueueActor actor = QueueActor.builder()
          .messageType(messageType)
          .consumerConfiguration(
              appQueueConfiguration.getConsumerConfiguration(messageType)
          )
          .rabbitConnection(rabbitConnection)
          .processor(getProcessor(messageType))
          .build();

      actor.start();

      actors.put(messageType, actor);
    }

    QueueUtils.initialize(this);

    log.info("Started all app queues");
  }

  @Override
  public void stop() throws Exception {
    log.info("Stopping all app actors");
    actors.values().forEach(Trouper::stop);
    log.info("Stopped all app actors");
  }

  public Optional<Boolean> publish(MessageType messageType, QueueContext queueContext)
      throws Exception {
    QueueActor actor = getActor(messageType);

    actor.publish(queueContext);

    return Optional.of(true);
  }

  public Optional<Boolean> publishWithExpiry(MessageType messageType,
      QueueContext queueContext, long expiresAt, boolean expiresAtEnabled)
      throws Exception {
    QueueActor actor = getActor(messageType);

    actor.publishWithExpiry(queueContext, expiresAt, expiresAtEnabled);

    return Optional.of(true);
  }

  public Optional<Boolean> ttlPublish(MessageType messageType, QueueContext queueContext,
      int retryCount, long expirationMs) throws Exception {
    QueueActor actor = getActor(messageType);

    actor.retryPublish(queueContext, retryCount, expirationMs);

    return Optional.of(true);
  }

  public Optional<Boolean> ttlPublishWithExpiry(MessageType messageType,
      QueueContext queueContext,
      int retryCount, long expirationMs,
      long expiresAt, boolean expiresAtEnabled) throws Exception {
    QueueActor actor = getActor(messageType);

    actor.retryPublishWithExpiry(queueContext, retryCount, expirationMs, expiresAt,
        expiresAtEnabled);

    return Optional.of(true);
  }


  @Slf4j
  static class QueueActor extends Trouper<QueueContext> {

    private final QueueConsumer processor;

    @Builder
    public QueueActor(MessageType messageType,
        QueueConfiguration consumerConfiguration,
        RabbitConnection rabbitConnection,
        QueueConsumer processor) {
      super(messageType.name(),
          consumerConfiguration,
          rabbitConnection,
          QueueContext.class,
          Collections.emptySet());

      this.processor = processor;
    }

    @Override
    public boolean process(QueueContext queueContext, QAccessInfo qAccessInfo) {
      try {
        return processor.consume(queueContext, qAccessInfo);
      } catch (Exception e) {
        log.error("Error processing a main queue message for reference Id {}",
            queueContext.getServiceReference(), e);
        return false;
      }
    }

    @Override
    public boolean processSideline(QueueContext queueContext, QAccessInfo qAccessInfo) {
      try {
        return processor.consumeSideline(queueContext, qAccessInfo);
      } catch (Exception e) {
        log.error("Error processing a sideline queue message for reference Id {}",
            queueContext.getServiceReference(), e);
        return false;
      }
    }
  }
}
