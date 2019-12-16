package io.vedaan.social;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.github.qtrouper.TrouperBundle;
import io.github.qtrouper.core.rabbit.RabbitConnection;
import io.vedaan.social.queue.lifecycle.QueueManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class SocialModule extends AbstractModule {


  private final TrouperBundle trouperBundle;

  @Override
  protected void configure() {
    log.info("Configuring");
    bind(QueueManager.class).in(Scopes.SINGLETON);
  }

  @Provides
  @Singleton
  public RabbitConnection provideRabbitConnection() {
    return trouperBundle.getRabbitConnection();
  }

}
