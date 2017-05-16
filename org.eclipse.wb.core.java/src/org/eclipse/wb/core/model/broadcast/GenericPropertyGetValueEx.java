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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Listener for {@link GenericProperty} events.
 *
 * Subscribers can use this interface to provide {@link Expression} value during
 * {@link GenericPropertyImpl#getValue()}. This is useful for case when we did not evaluate this
 * {@link Expression} using normal execution flow.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface GenericPropertyGetValueEx {
  /**
   * Subscribers can use this method to provide {@link Expression} value during
   * {@link GenericPropertyImpl#getValue()}. This is useful for case when we did not evaluate this
   * {@link Expression} using normal execution flow.
   *
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   * @param expression
   *          the {@link Expression} to be evaluated.
   * @param value
   *          the single element array with value. Initially it has default (evaluated) value.
   */
  void invoke(GenericPropertyImpl property, Expression expression, Object[] value) throws Exception;
}