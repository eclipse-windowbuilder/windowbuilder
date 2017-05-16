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
package org.eclipse.wb.internal.core.model.util.predicate;

import com.google.common.base.Predicate;

/**
 * {@link Predicate} always returns same value.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class AlwaysPredicate implements Predicate<Object> {
  private final boolean m_value;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AlwaysPredicate(boolean value) {
    m_value = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Predicate
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean apply(Object t) {
    return m_value;
  }
}
