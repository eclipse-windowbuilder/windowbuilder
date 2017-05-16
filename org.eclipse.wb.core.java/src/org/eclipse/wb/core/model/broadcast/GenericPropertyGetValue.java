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

/**
 * Listener for {@link GenericProperty} events.
 * <p>
 * Subscribers can use this interface to unconditionally return the property value. This is
 * applicable for cases when property should unconditionally return certain value without using any
 * expression evaluating related to this property. For example, the property may return value of
 * another property.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface GenericPropertyGetValue {
  /**
   * Subscribers can use this method to unconditionally return the property value. This is
   * applicable for cases when property should unconditionally return certain value without using
   * any expression evaluating related to this property. For example, the property may return value
   * of another property.
   *
   * @param property
   *          the {@link GenericPropertyImpl} that sends this event.
   * @param value
   *          the single element array to return property value. Initially it has <code>null</code>
   *          value.
   */
  void invoke(GenericPropertyImpl property, Object[] value) throws Exception;
}