package io.vedaan.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.github.qtrouper.core.rabbit.RabbitConfiguration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
public class SocialConfiguration extends Configuration {

  @NotEmpty
  private String template;

  @NotEmpty
  private String defaultName = "Stranger";

  @JsonProperty
  @Valid
  private RabbitConfiguration rabbitConfiguration = new RabbitConfiguration();

  @Valid
  @NotNull
  private DataSourceFactory database;
}
