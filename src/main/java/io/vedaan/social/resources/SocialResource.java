package io.vedaan.social.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import io.vedaan.social.SocialConfiguration;
import io.vedaan.social.db.dao.PersonDao;
import io.vedaan.social.db.entity.Person;
import io.vedaan.social.model.Saying;
import io.vedaan.social.queue.util.QueueUtils;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Path("/social")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class SocialResource {

  private final String template;
  private final String defaultName;
  private final PersonDao personDao;

  @Inject
  public SocialResource(SocialConfiguration configuration, PersonDao personDao) {
    this.template = configuration.getTemplate();
    this.defaultName = configuration.getDefaultName();
    this.personDao = personDao;
  }

  @GET
  @Timed
  public Saying sayHello(@QueryParam("name") Optional<String> name) throws Exception {
    final String value = String.format(template, name.orElse(defaultName));

    //checking async processing
    try {
      QueueUtils.queueSampleMessage(name.orElse(defaultName));
    } catch (Exception e) {
      log.error("Some error occured while queueing sample message");
      throw e;
    }

    final Person savedEntity = personDao.save(
        Person.builder()
            .name(name.orElse(defaultName))
            .build()
    );
    return new Saying(savedEntity.getId(), value);
  }


}
