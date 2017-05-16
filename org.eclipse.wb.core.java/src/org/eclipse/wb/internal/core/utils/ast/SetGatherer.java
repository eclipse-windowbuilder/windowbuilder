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

import com.google.common.collect.Sets;

import java.util.Collection;

/**
 * Instances of the class <code>SetGatherer</code> implement a gatherer that ensures that there are
 * no duplications among the values that are found.
 * <p>
 *
 * @author Brian Wilkerson
 * @version $Revision: 1.3 $
 * @coverage core.util.ast
 */
public abstract class SetGatherer<T> extends Gatherer<T> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessing -- internal
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Collection<T> createCollection() {
    return Sets.newHashSet();
  }
}