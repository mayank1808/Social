package io.vedaan.social.queue;

import io.github.qtrouper.core.models.QAccessInfo;
import io.github.qtrouper.core.models.QueueContext;

public abstract class QueueConsumer {


  public abstract boolean consume(QueueContext queueContext, QAccessInfo accessInfo)
      throws Exception;


  public abstract boolean consumeSideline(QueueContext queueContext, QAccessInfo accessInfo)
      throws Exception;

}