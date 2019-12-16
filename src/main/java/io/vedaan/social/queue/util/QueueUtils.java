package io.vedaan.social.queue.util;

import com.google.inject.Singleton;
import io.github.qtrouper.core.models.QueueContext;
import io.vedaan.social.queue.MessageType;
import io.vedaan.social.queue.lifecycle.QueueManager;

@Singleton
public class QueueUtils {

  protected static QueueManager qManager;

  public static void initialize(QueueManager manager) {
    qManager = manager;
  }

  public static void queueSampleMessage(String name) throws Exception {

    QueueContext queueContext = QueueContext.builder().serviceReference(name).build();

    qManager.publish(MessageType.SAMPLE, queueContext);
  }
}
