/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Instances of the class <code>Gatherer</code> represent an AST visitor whose methods are expected
 * to add elements to a result collection.
 * <p>
 *
 * @author Brian Wilkerson
 * @version $Revision: 1.5 $
 * @coverage core.util.ast
 */
public abstract class Gatherer<T> extends ASTVisitor {
  /**
   * A collection of the values that were gathered.
   */
  private final Collection<T> results;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initialize a newly created gatherer.
   */
  public Gatherer() {
    results = createCollection();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return <code>true</code> if one or more values have been gathered.
   *
   * @return <code>true</code> if one or more values have been gathered
   */
  public final boolean hasResults() {
    return !results.isEmpty();
  }

  /**
   * Return the one result value that was gathered, or <code>null</code> if the were either more or
   * fewer values gathered.
   *
   * @return the one result value that was gathered
   */
  public final T getUniqueResult() {
    if (results.size() == 1) {
      return results.iterator().next();
    }
    return null;
  }

  /**
   * Return a list of the values that were gathered.
   *
   * @return a list of the values that were gathered
   */
  public final List<T> getResultList() {
    if (results instanceof List<?>) {
      return (List<T>) results;
    }
    return new ArrayList<T>(results);
  }

  /**
   * Return a set of the values that were gathered.
   *
   * @return a set of the values that were gathered
   */
  public final Set<T> getResultSet() {
    if (results instanceof Set<?>) {
      return (Set<T>) results;
    }
    return new HashSet<T>(results);
  }

  /**
   * Assuming that all of the values that were gathered were {@link VariableDeclaration}'s, return
   * an array of the {@link VariableDeclaration}'s that were gathered.
   *
   * @return an array of the variable declarations that were gathered
   */
  @SuppressWarnings("unchecked")
  public final T[] toArray(Class<T> clazz) {
    T[] array = (T[]) Array.newInstance(clazz, results.size());
    return results.toArray(array);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessing -- internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add the given result value to the collection of result values.
   *
   * @param value
   *          the result value to be added
   */
  protected final void addResult(T value) {
    results.add(value);
  }

  /**
   * Create the collection to which result values will be added.
   *
   * @return the collection that was created
   */
  protected abstract Collection<T> createCollection();
}