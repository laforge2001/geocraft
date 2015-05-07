/*
 * Copyright (C) ConocoPhillips 2006 All Rights Reserved.
 */

package org.geocraft.core.repository.specification;


public interface ISpecification {

  /**
   * Does the supplied object match the specification.
   * 
   * @param obj
   *          to be tested.
   * @return true if the object passes the test.
   */
  boolean isSatisfiedBy(Object obj);

  /**
   * Compose two specifications into an AND composite specification.
   * 
   * @param other
   *          the specification to be composed to this
   * @return the resulted specification
   */
  ISpecification and(ISpecification other);

  /**
   * Compose two specifications into an OR composite specification.
   * 
   * @param other
   *          the specification to be composed to this
   * @return the resulted specification
   */
  ISpecification or(ISpecification other);

  /**
   * Negate a specification.
   * 
   * @return the resulted specification
   */
  ISpecification not();
}