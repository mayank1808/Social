package io.vedaan.social.queue.consumers;

import io.github.qtrouper.core.models.QAccessInfo;
import io.github.qtrouper.core.models.QueueContext;
import io.vedaan.social.queue.MessageType;
import io.vedaan.social.queue.QueueConsumer;
import io.vedaan.social.queue.annotation.MessageConsumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageConsumer(messageType = MessageType.SAMPLE)
public class SampleConsumer extends QueueConsumer {

  @Override
  public boolean consume(QueueContext queueContext, QAccessInfo accessInfo) throws Exception {
    log.info("Consuming Message {}", queueContext.getServiceReference());
    return true;
  }

  @Override
  public boolean consumeSideline(QueueContext queueContext, QAccessInfo accessInfo)
      throws Exception {
    log.info("Consuming Sideline Message {}", queueContext.getServiceReference());
    return true;
  }
}
