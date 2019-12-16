package io.vedaan.social.db.dao;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

@Getter
@Setter
public abstract class EntityBaseDao<E> extends AbstractDAO<E> {

  private String idName;

  private Class<E> entityClazz;

  public EntityBaseDao(String idName, SessionFactory sessionFactory, Class<E> entityClazz) {
    super(sessionFactory);
    this.idName = idName;
    this.entityClazz = entityClazz;
  }

  @UnitOfWork
  public E get(String id) {
    CriteriaBuilder builder = super.currentSession().getCriteriaBuilder();
    CriteriaQuery<E> criteria = builder.createQuery(this.getEntityClazz());
    Root<E> root = criteria.from(this.getEntityClazz());
    criteria
        .where(builder.equal(root.get(this.idName), id));
    Query<E> query = super.currentSession().createQuery(criteria);
    return query.uniqueResult();
  }

  @UnitOfWork
  public E save(E e) {
    return super.persist(e);
  }

  @UnitOfWork
  public List<E> save(List<E> entities) {
    return entities.stream()
        .map(e -> super.persist(e))
        .collect(Collectors.toList());
  }
}

