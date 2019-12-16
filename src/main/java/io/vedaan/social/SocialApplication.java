package io.vedaan.social;

import com.google.inject.Stage;
import io.dropwizard.Application;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.github.qtrouper.TrouperBundle;
import io.github.qtrouper.core.rabbit.RabbitConfiguration;
import io.vedaan.social.db.dao.DaoModule;
import io.vedaan.social.db.entity.Person;
import io.vedaan.social.health.TemplateHealthCheck;
import java.util.Arrays;
import java.util.List;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class SocialApplication extends Application<SocialConfiguration> {

  private final List<String> PACKAGE_NAME_LIST = Arrays.asList(
      "io.vedaan.social"
  );

  private final HibernateBundle<SocialConfiguration> hibernate = new HibernateBundle<SocialConfiguration>(
      Person.class) {

    @Override
    protected String name() {
      return "Social";
    }

    @Override
    public PooledDataSourceFactory getDataSourceFactory(SocialConfiguration configuration) {
      return configuration.getDatabase();
    }
  };
  public static void main(final String[] args) throws Exception {
    new SocialApplication().run(args);
  }

  @Override
  public String getName() {
    return "Social";
  }

  @Override
  public void initialize(final Bootstrap<SocialConfiguration> bootstrap) {

    TrouperBundle<SocialConfiguration> trouperBundle = new TrouperBundle<SocialConfiguration>() {

      @Override
      public RabbitConfiguration getRabbitConfiguration(SocialConfiguration configuration) {
        return configuration.getRabbitConfiguration();
      }
    };

    bootstrap.addBundle(trouperBundle);
    bootstrap.addBundle(hibernate);

    GuiceBundle<SocialConfiguration> guiceBundle = GuiceBundle.<SocialConfiguration>builder()
        .enableAutoConfig(getClass().getPackage().getName())
        .modules(
            new SocialModule(trouperBundle),
            new DaoModule(hibernate)
        )
        .build(Stage.DEVELOPMENT);

    bootstrap.addBundle(guiceBundle);
  }

  @Override
  public void run(final SocialConfiguration configuration,
      final Environment environment) {

    final TemplateHealthCheck healthCheck =
        new TemplateHealthCheck(configuration.getTemplate());
    environment.healthChecks().register("template", healthCheck);

  }

}
