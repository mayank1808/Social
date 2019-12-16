package io.vedaan.social.db.dao;

import io.vedaan.social.db.entity.Person;
import org.hibernate.SessionFactory;

public class PersonDao extends EntityBaseDao<Person> {

  public PersonDao(SessionFactory sessionFactory) {
    super("id", sessionFactory, Person.class);
  }

}
