/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */

package org.geocraft.core.repository.specification;


/**
 * An abstract specification class.
 */
public abstract class AbstractSpecification implements ISpecification {

  /**
   * Compose two specifications into an AND composite specification.
   * 
   * @param other
   *          the specification to be composed to this
   * @return the resulted specification
   */
  public ISpecification and(ISpecification other) {
    return new AndSpecification(this, other);
  }

  /**
   * Compose two specifications into an OR composite specification.
   * 
   * @param other
   *          the specification to be composed to this
   * @return the resulted specification
   */
  public ISpecification or(ISpecification other) {
    return new OrSpecification(this, other);
  }

  /**
   * Negate a specification.
   * 
   * @return the resulted specification
   */
  public ISpecification not() {
    return new NotSpecification(this);
  }

  /**
   * Compose two specifications into an AND composite specification.
   */
  public class AndSpecification extends AbstractSpecification {

    ISpecification _one;

    ISpecification _other;

    public AndSpecification(ISpecification one, ISpecification other) {
      _one = one;
      _other = other;
    }

    public boolean isSatisfiedBy(Object candidate) {
      return _one.isSatisfiedBy(candidate) && _other.isSatisfiedBy(candidate);
    }
  }

  /**
   * Compose two specifications into an OR composite specification.
   */
  public class OrSpecification extends AbstractSpecification {

    ISpecification _one;

    ISpecification _other;

    public OrSpecification(ISpecification one, ISpecification other) {
      _one = one;
      _other = other;
    }

    public boolean isSatisfiedBy(Object candidate) {
      return _one.isSatisfiedBy(candidate) || _other.isSatisfiedBy(candidate);
    }
  }

  /**
   * Add a NOT to a specification.
   */
  public class NotSpecification extends AbstractSpecification {

    ISpecification _one;

    public NotSpecification(ISpecification one) {
      _one = one;
    }

    public boolean isSatisfiedBy(Object candidate) {
      return !_one.isSatisfiedBy(candidate);
    }
  }
}
